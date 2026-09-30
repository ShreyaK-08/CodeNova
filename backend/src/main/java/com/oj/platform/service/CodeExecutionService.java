package com.oj.platform.service;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Executes user-submitted code for real, for a small set of languages
 * (Java, Python, C++, JavaScript), by shelling out to the language's own
 * compiler/interpreter on the host machine.
 *
 * IMPORTANT / HONEST LIMITATION:
 * This runs submitted code as a normal OS process with the same privileges
 * as the backend itself. There is NO sandboxing (no container, no seccomp,
 * no restricted user). That is acceptable for a local student/demo project
 * where you trust the code being run, but it must never be exposed to
 * untrusted public users as-is.
 *
 * Each language requires its own tool to be installed and on the system PATH:
 *   JAVA        -> javac / java   (already required to run this Spring Boot app)
 *   PYTHON      -> python or python3
 *   CPP         -> g++
 *   JAVASCRIPT  -> node
 * If a required tool is missing, the submission is marked RUNTIME_ERROR with
 * a clear message instead of silently pretending it worked.
 */
@Service
public class CodeExecutionService {

    private static final Pattern JAVA_PUBLIC_CLASS = Pattern.compile("public\\s+class\\s+(\\w+)");

    public static class CompileOutcome {
        public boolean success;
        public String errorOutput;
        public Path workDir;
        public List<String> runCommand;
    }

    public static class RunOutcome {
        public boolean timedOut;
        public boolean toolMissing;
        public int exitCode;
        public String stdout;
        public String stderr;
        public long executionTimeMs;
    }

    /** Writes the submitted source to disk and compiles it if the language requires compilation. */
    public CompileOutcome compile(String language, String code) throws IOException {
        Path workDir = Files.createTempDirectory("submission_");
        CompileOutcome outcome = new CompileOutcome();
        outcome.workDir = workDir;

        String lang = language == null ? "" : language.trim().toUpperCase();

        switch (lang) {
            case "JAVA": {
                String className = detectJavaClassName(code);
                Files.writeString(workDir.resolve(className + ".java"), code, StandardCharsets.UTF_8);
                RunOutcome compileResult = runProcess(workDir, 15000, null,
                        List.of("javac", className + ".java"));
                if (compileResult.toolMissing) {
                    outcome.success = false;
                    outcome.errorOutput = "javac was not found. Make sure a JDK is installed and on PATH.";
                    return outcome;
                }
                if (compileResult.exitCode != 0) {
                    outcome.success = false;
                    outcome.errorOutput = firstNonBlank(compileResult.stderr, compileResult.stdout);
                    return outcome;
                }
                outcome.success = true;
                outcome.runCommand = List.of("java", "-Xmx256m", className);
                return outcome;
            }
            case "PYTHON":
            case "PYTHON3": {
                Files.writeString(workDir.resolve("solution.py"), code, StandardCharsets.UTF_8);
                outcome.success = true;
                outcome.runCommand = List.of(pythonExecutable(), "solution.py");
                return outcome;
            }
            case "CPP":
            case "C++": {
                Files.writeString(workDir.resolve("solution.cpp"), code, StandardCharsets.UTF_8);
                boolean windows = isWindows();
                String exeName = windows ? "solution.exe" : "solution";
                RunOutcome compileResult = runProcess(workDir, 15000, null,
                        List.of("g++", "-O2", "-o", exeName, "solution.cpp"));
                if (compileResult.toolMissing) {
                    outcome.success = false;
                    outcome.errorOutput = "g++ was not found. Install a C++ compiler (e.g. MinGW) and add it to PATH.";
                    return outcome;
                }
                if (compileResult.exitCode != 0) {
                    outcome.success = false;
                    outcome.errorOutput = firstNonBlank(compileResult.stderr, compileResult.stdout);
                    return outcome;
                }
                outcome.success = true;
                outcome.runCommand = windows ? List.of(exeName) : List.of("./" + exeName);
                return outcome;
            }
            case "JAVASCRIPT":
            case "JS": {
                Files.writeString(workDir.resolve("solution.js"), code, StandardCharsets.UTF_8);
                outcome.success = true;
                outcome.runCommand = List.of("node", "solution.js");
                return outcome;
            }
            default: {
                outcome.success = false;
                outcome.errorOutput = "Unsupported language: " + language;
                return outcome;
            }
        }
    }

    /** Runs the compiled/interpreted program once, feeding it stdin and enforcing a timeout. */
    public RunOutcome run(Path workDir, List<String> command, String stdin, int timeLimitMs) {
        return runProcess(workDir, timeLimitMs, stdin, command);
    }

    /**
     * Compiles a user-submitted "Solution" class together with a backend-generated
     * "Main" driver class (see JavaDriverGenerator) so the user never has to write
     * their own main() method. Both files are compiled together in one javac call
     * so Main.java can reference the Solution class directly.
     */
    public CompileOutcome compileJavaWithDriver(String solutionCode, String mainJavaSource) throws IOException {
        Path workDir = Files.createTempDirectory("submission_");
        CompileOutcome outcome = new CompileOutcome();
        outcome.workDir = workDir;

        Files.writeString(workDir.resolve("Solution.java"), solutionCode, StandardCharsets.UTF_8);
        Files.writeString(workDir.resolve("Main.java"), mainJavaSource, StandardCharsets.UTF_8);

        RunOutcome compileResult = runProcess(workDir, 15000, null,
                List.of("javac", "Solution.java", "Main.java"));
        if (compileResult.toolMissing) {
            outcome.success = false;
            outcome.errorOutput = "javac was not found. Make sure a JDK is installed and on PATH.";
            return outcome;
        }
        if (compileResult.exitCode != 0) {
            outcome.success = false;
            outcome.errorOutput = firstNonBlank(compileResult.stderr, compileResult.stdout);
            return outcome;
        }
        outcome.success = true;
        outcome.runCommand = List.of("java", "-Xmx256m", "Main");
        return outcome;
    }

    public void cleanup(Path workDir) {
        if (workDir == null) return;
        try {
            Files.walk(workDir)
                    .sorted(Comparator.reverseOrder())
                    .forEach(p -> {
                        try {
                            Files.deleteIfExists(p);
                        } catch (IOException ignored) {
                        }
                    });
        } catch (IOException ignored) {
        }
    }

    private RunOutcome runProcess(Path workDir, long timeoutMs, String stdin, List<String> command) {
        RunOutcome result = new RunOutcome();
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(workDir.toFile());

        Process process;
        long start = System.currentTimeMillis();
        try {
            process = pb.start();
        } catch (IOException e) {
            result.toolMissing = true;
            result.exitCode = -1;
            result.stderr = "Required tool not found: " + command.get(0);
            return result;
        }

        ExecutorService pool = Executors.newFixedThreadPool(3);
        try {
            if (stdin != null) {
                pool.submit(() -> writeAndClose(process.getOutputStream(), stdin));
            } else {
                pool.submit(() -> writeAndClose(process.getOutputStream(), ""));
            }
            Future<String> stdoutFuture = pool.submit(() -> readStream(process.getInputStream()));
            Future<String> stderrFuture = pool.submit(() -> readStream(process.getErrorStream()));

            boolean finished;
            try {
                finished = process.waitFor(timeoutMs, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                finished = false;
            }

            result.executionTimeMs = System.currentTimeMillis() - start;

            if (!finished) {
                process.destroyForcibly();
                result.timedOut = true;
                result.exitCode = -1;
                return result;
            }

            result.exitCode = process.exitValue();
            try {
                result.stdout = stdoutFuture.get(2, TimeUnit.SECONDS);
            } catch (Exception e) {
                result.stdout = "";
            }
            try {
                result.stderr = stderrFuture.get(2, TimeUnit.SECONDS);
            } catch (Exception e) {
                result.stderr = "";
            }
            return result;
        } finally {
            pool.shutdownNow();
        }
    }

    private void writeAndClose(OutputStream out, String content) {
        try (OutputStream os = out) {
            if (content != null && !content.isEmpty()) {
                os.write(content.getBytes(StandardCharsets.UTF_8));
            }
        } catch (IOException ignored) {
        }
    }

    private String readStream(InputStream in) {
        try {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return "";
        }
    }

    private String detectJavaClassName(String code) {
        Matcher matcher = JAVA_PUBLIC_CLASS.matcher(code == null ? "" : code);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "Main";
    }

    private String pythonExecutable() {
        // "python3" is standard on Linux/Mac; Windows installs usually expose "python".
        // We try python3 first, and CodeExecutionService.run's toolMissing flag will
        // surface a clear error if neither is actually on PATH.
        return isWindows() ? "python" : "python3";
    }

    private boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase().contains("win");
    }

    private String firstNonBlank(String a, String b) {
        if (a != null && !a.isBlank()) return a;
        if (b != null && !b.isBlank()) return b;
        return "Unknown error";
    }
}
