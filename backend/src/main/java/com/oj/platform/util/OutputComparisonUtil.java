package com.oj.platform.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Reusable utility for comparing user program stdout against expected test case output
 * using robust whitespace normalization.
 *
 * Normalizes:
 * - Windows (\r\n), Mac (\r), and Linux (\n) line endings
 * - Leading and trailing whitespace on the entire output
 * - Trailing spaces on individual lines
 * - Trailing blank lines
 * - Consecutive horizontal whitespace between tokens where appropriate
 * - Bracketed array spacing differences (e.g. "[0, 1]" vs "[0,1]")
 *
 * Safely preserves:
 * - Meaningful differences in output values, strings, numbers, and order.
 */
public final class OutputComparisonUtil {

    private static final Pattern MULTIPLE_SPACES = Pattern.compile("[ \\t]+");

    private OutputComparisonUtil() {
    }

    public static class ComparisonResult {
        private final boolean matches;
        private final String normalizedActual;
        private final String normalizedExpected;
        private final String diffDetails;

        public ComparisonResult(boolean matches, String normalizedActual, String normalizedExpected, String diffDetails) {
            this.matches = matches;
            this.normalizedActual = normalizedActual;
            this.normalizedExpected = normalizedExpected;
            this.diffDetails = diffDetails;
        }

        public boolean isMatches() {
            return matches;
        }

        public String getNormalizedActual() {
            return normalizedActual;
        }

        public String getNormalizedExpected() {
            return normalizedExpected;
        }

        public String getDiffDetails() {
            return diffDetails;
        }
    }

    /**
     * Compares actual stdout against expected output.
     * Returns true if normalized forms match.
     */
    public static boolean matches(String actual, String expected) {
        return compare(actual, expected).isMatches();
    }

    /**
     * Performs detailed comparison and returns ComparisonResult.
     */
    public static ComparisonResult compare(String actual, String expected) {
        String normActual = normalize(actual);
        String normExpected = normalize(expected);

        if (Objects.equals(normActual, normExpected)) {
            return new ComparisonResult(true, normActual, normExpected, null);
        }

        // Also check with array bracket spacing normalization: e.g. "[0, 1]" vs "[0,1]"
        String canonActual = canonicalizeArrays(normActual);
        String canonExpected = canonicalizeArrays(normExpected);

        if (Objects.equals(canonActual, canonExpected)) {
            return new ComparisonResult(true, normActual, normExpected, null);
        }

        // Detailed line-by-line mismatch analysis
        String diff = findDiff(normActual, normExpected);
        return new ComparisonResult(false, normActual, normExpected, diff);
    }

    /**
     * Normalizes output by:
     * 1. Converting all newlines to \n
     * 2. Splitting into lines
     * 3. Trimming trailing horizontal spaces from each line
     * 4. Collapsing consecutive horizontal spaces within lines
     * 5. Removing leading and trailing empty lines
     */
    public static String normalize(String s) {
        if (s == null) {
            return "";
        }

        // Unify newlines
        String unified = s.replace("\r\n", "\n").replace('\r', '\n');

        String[] rawLines = unified.split("\n", -1);
        List<String> cleanedLines = new ArrayList<>();

        for (String line : rawLines) {
            // Trim leading/trailing whitespace of line, collapse internal multi-spaces
            String trimmed = line.trim();
            if (!trimmed.isEmpty()) {
                String collapsed = MULTIPLE_SPACES.matcher(trimmed).replaceAll(" ");
                cleanedLines.add(collapsed);
            } else if (!cleanedLines.isEmpty()) {
                // Keep empty lines between content, but will clean trailing later
                cleanedLines.add("");
            }
        }

        // Remove trailing empty lines
        while (!cleanedLines.isEmpty() && cleanedLines.get(cleanedLines.size() - 1).isEmpty()) {
            cleanedLines.remove(cleanedLines.size() - 1);
        }

        return String.join("\n", cleanedLines);
    }

    /**
     * Canonicalizes bracketed formatting such that "[ 0 , 1 ]" becomes "[0,1]".
     */
    public static String canonicalizeArrays(String s) {
        if (s == null) return "";
        return s.replaceAll("\\s*\\[\\s*", "[")
                .replaceAll("\\s*\\]\\s*", "]")
                .replaceAll("\\s*,\\s*", ",");
    }

    private static String findDiff(String actual, String expected) {
        String[] actualLines = actual.split("\n", -1);
        String[] expectedLines = expected.split("\n", -1);

        int max = Math.max(actualLines.length, expectedLines.length);
        for (int i = 0; i < max; i++) {
            String actLine = i < actualLines.length ? actualLines[i] : "<EOF>";
            String expLine = i < expectedLines.length ? expectedLines[i] : "<EOF>";

            if (!Objects.equals(actLine, expLine)) {
                return String.format("Line %d mismatch: expected '%s', but got '%s'", i + 1, expLine, actLine);
            }
        }
        return "Outputs differ.";
    }
}
