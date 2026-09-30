package com.oj.platform.service;

import com.oj.platform.entity.TestCase;

import java.util.List;

/**
 * Generates a Main.java test driver that calls a specific method on the user's
 * Solution class for each test case, so the user never has to write a main() method.
 *
 * DESIGN (root-cause fix for "argument type mismatch"):
 * Earlier versions of this generator tried to *guess* the Java type of each
 * argument (int vs String vs array...) purely from the surface syntax of the
 * stored test-case input text at CODE-GENERATION time, then baked that guessed
 * type into a Java literal in the generated source. That guess did not always
 * agree with the Solution method's ACTUAL declared parameter type, which is
 * what causes java.lang.reflect.InvocationTargetException /
 * IllegalArgumentException: argument type mismatch when they disagree.
 *
 * Instead, the generated Main.java now:
 *   1. Embeds each test case's RAW input text as-is (a single Java String
 *      literal - always unambiguous, no type guessing at generation time).
 *   2. At RUNTIME, reflects on the compiled Solution class to find the target
 *      method and its real parameter types.
 *   3. Splits the raw input into top-level comma-separated tokens and parses
 *      each token according to the ACTUAL declared parameter type of that
 *      position (int, long, double, boolean, char, String, and 1D/2D arrays
 *      of those), so the value passed to invoke() is always exactly the type
 *      the method expects.
 *
 * This is generic - it is not special-cased for any one problem (e.g.
 * Palindrome). It supports at minimum: int, long, double, boolean, char,
 * String, int[], long[], double[], boolean[], char[], String[], int[][].
 */
public final class JavaDriverGenerator {

    private JavaDriverGenerator() {
    }

    public static String generateMainJava(String methodName, List<TestCase> testCases) {
        StringBuilder sb = new StringBuilder();

        sb.append("import java.util.*;\n");
        sb.append("import java.lang.reflect.*;\n\n");

        sb.append("public class Main {\n");
        sb.append("    public static void main(String[] args) throws Exception {\n");
        sb.append("        int idx = Integer.parseInt(args[0]);\n");
        sb.append("        Solution sol = new Solution();\n");
        sb.append("        String[] rawInputs = new String[]{\n");

        for (int i = 0; i < testCases.size(); i++) {
            String rawInput = testCases.get(i).getInput() == null ? "" : testCases.get(i).getInput();
            sb.append("            ").append(escapeJavaString(rawInput));
            if (i < testCases.size() - 1) sb.append(",");
            sb.append("\n");
        }

        sb.append("        };\n");
        sb.append("        if (idx < 0 || idx >= rawInputs.length) {\n");
        sb.append("            throw new IllegalArgumentException(\"Invalid test case index: \" + idx);\n");
        sb.append("        }\n");

        sb.append("        Object result = invoke(sol, \"")
                .append(methodName)
                .append("\", rawInputs[idx]);\n");

        sb.append("        System.out.println(format(result));\n");
        sb.append("    }\n\n");

        sb.append(RUNTIME_HELPERS);

        sb.append("}\n");

        return sb.toString();
    }

    /** Escapes a raw string into a valid, safe Java string literal (including the surrounding quotes). */
    static String escapeJavaString(String s) {
        StringBuilder out = new StringBuilder("\"");
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '\\': out.append("\\\\"); break;
                case '"': out.append("\\\""); break;
                case '\n': out.append("\\n"); break;
                case '\r': out.append("\\r"); break;
                case '\t': out.append("\\t"); break;
                default:
                    if (c < 0x20) {
                        out.append(String.format("\\u%04x", (int) c));
                    } else {
                        out.append(c);
                    }
            }
        }
        out.append("\"");
        return out.toString();
    }

    private static final String RUNTIME_HELPERS =
            // ---------------------------------------------------------------
            // Method resolution + invocation
            // ---------------------------------------------------------------
            "    static Object invoke(Object target, String methodName, String rawInput) throws Exception {\n" +
            "        Method targetMethod = null;\n" +
            "        for (Method m : target.getClass().getDeclaredMethods()) {\n" +
            "            if (m.getName().equals(methodName) && !m.isSynthetic() && !m.isBridge()) {\n" +
            "                targetMethod = m;\n" +
            "                break;\n" +
            "            }\n" +
            "        }\n" +
            "        if (targetMethod == null) {\n" +
            "            throw new NoSuchMethodException(\"Method '\" + methodName + \"' not found in Solution class.\");\n" +
            "        }\n" +
            "        targetMethod.setAccessible(true);\n" +
            "        Class<?>[] paramTypes = targetMethod.getParameterTypes();\n" +
            "        List<String> tokens = splitTopLevel(rawInput == null ? \"\" : rawInput.trim());\n" +
            "        Object[] converted = new Object[paramTypes.length];\n" +
            "        for (int i = 0; i < paramTypes.length; i++) {\n" +
            "            String tok = (i < tokens.size()) ? tokens.get(i).trim() : \"\";\n" +
            "            converted[i] = parseToken(tok, paramTypes[i]);\n" +
            "        }\n" +
            "        try {\n" +
            "            Object res = targetMethod.invoke(target, converted);\n" +
            "            if (targetMethod.getReturnType() == void.class) {\n" +
            // Void methods (e.g. void reverseString(char[] s)) mutate an array argument
            // in-place; report back whichever converted argument is an array.
            "                for (Object c : converted) {\n" +
            "                    if (c != null && c.getClass().isArray()) return c;\n" +
            "                }\n" +
            "                return null;\n" +
            "            }\n" +
            "            return res;\n" +
            "        } catch (InvocationTargetException ite) {\n" +
            "            Throwable cause = ite.getCause();\n" +
            "            if (cause instanceof Exception) throw (Exception) cause;\n" +
            "            if (cause instanceof Error) throw (Error) cause;\n" +
            "            throw ite;\n" +
            "        }\n" +
            "    }\n\n" +

            // ---------------------------------------------------------------
            // Token -> exact declared parameter type parsing (the actual fix)
            // ---------------------------------------------------------------
            "    static Object parseToken(String token, Class<?> type) {\n" +
            "        token = token == null ? \"\" : token.trim();\n" +
            "        if (type == int.class || type == Integer.class) {\n" +
            "            return Integer.parseInt(unquote(token));\n" +
            "        }\n" +
            "        if (type == long.class || type == Long.class) {\n" +
            "            String v = unquote(token);\n" +
            "            if (v.endsWith(\"L\") || v.endsWith(\"l\")) v = v.substring(0, v.length() - 1);\n" +
            "            return Long.parseLong(v);\n" +
            "        }\n" +
            "        if (type == double.class || type == Double.class) {\n" +
            "            return Double.parseDouble(unquote(token));\n" +
            "        }\n" +
            "        if (type == float.class || type == Float.class) {\n" +
            "            return Float.parseFloat(unquote(token));\n" +
            "        }\n" +
            "        if (type == boolean.class || type == Boolean.class) {\n" +
            "            return Boolean.parseBoolean(unquote(token));\n" +
            "        }\n" +
            "        if (type == char.class || type == Character.class) {\n" +
            "            String v = unquote(token);\n" +
            "            return v.isEmpty() ? '\\u0000' : v.charAt(0);\n" +
            "        }\n" +
            "        if (type == String.class) {\n" +
            "            return unquote(token);\n" +
            "        }\n" +
            "        if (type == char[].class) {\n" +
            "            if (isBracketed(token)) {\n" +
            "                List<String> elems = splitTopLevel(stripBrackets(token));\n" +
            "                char[] arr = new char[elems.size()];\n" +
            "                for (int i = 0; i < elems.size(); i++) {\n" +
            "                    String e = unquote(elems.get(i).trim());\n" +
            "                    arr[i] = e.isEmpty() ? '\\u0000' : e.charAt(0);\n" +
            "                }\n" +
            "                return arr;\n" +
            "            }\n" +
            "            return unquote(token).toCharArray();\n" +
            "        }\n" +
            "        if (type == int[].class) {\n" +
            "            List<String> elems = splitTopLevel(stripBrackets(token));\n" +
            "            int[] arr = new int[elems.size()];\n" +
            "            for (int i = 0; i < elems.size(); i++) arr[i] = Integer.parseInt(unquote(elems.get(i).trim()));\n" +
            "            return arr;\n" +
            "        }\n" +
            "        if (type == long[].class) {\n" +
            "            List<String> elems = splitTopLevel(stripBrackets(token));\n" +
            "            long[] arr = new long[elems.size()];\n" +
            "            for (int i = 0; i < elems.size(); i++) arr[i] = Long.parseLong(unquote(elems.get(i).trim()));\n" +
            "            return arr;\n" +
            "        }\n" +
            "        if (type == double[].class) {\n" +
            "            List<String> elems = splitTopLevel(stripBrackets(token));\n" +
            "            double[] arr = new double[elems.size()];\n" +
            "            for (int i = 0; i < elems.size(); i++) arr[i] = Double.parseDouble(unquote(elems.get(i).trim()));\n" +
            "            return arr;\n" +
            "        }\n" +
            "        if (type == boolean[].class) {\n" +
            "            List<String> elems = splitTopLevel(stripBrackets(token));\n" +
            "            boolean[] arr = new boolean[elems.size()];\n" +
            "            for (int i = 0; i < elems.size(); i++) arr[i] = Boolean.parseBoolean(unquote(elems.get(i).trim()));\n" +
            "            return arr;\n" +
            "        }\n" +
            "        if (type == String[].class) {\n" +
            "            List<String> elems = splitTopLevel(stripBrackets(token));\n" +
            "            String[] arr = new String[elems.size()];\n" +
            "            for (int i = 0; i < elems.size(); i++) arr[i] = unquote(elems.get(i).trim());\n" +
            "            return arr;\n" +
            "        }\n" +
            "        if (type == int[][].class) {\n" +
            "            List<String> rows = splitTopLevel(stripBrackets(token));\n" +
            "            int[][] arr = new int[rows.size()][];\n" +
            "            for (int i = 0; i < rows.size(); i++) arr[i] = (int[]) parseToken(rows.get(i).trim(), int[].class);\n" +
            "            return arr;\n" +
            "        }\n" +
            // Fallback: best effort as raw text (keeps behavior graceful for any
            // future/unsupported type rather than throwing before invoke() runs).
            "        return unquote(token);\n" +
            "    }\n\n" +

            "    static boolean isBracketed(String s) {\n" +
            "        return s.length() >= 2 && s.charAt(0) == '[' && s.charAt(s.length() - 1) == ']';\n" +
            "    }\n\n" +

            "    static String stripBrackets(String s) {\n" +
            "        s = s.trim();\n" +
            "        if (isBracketed(s)) return s.substring(1, s.length() - 1);\n" +
            "        return s;\n" +
            "    }\n\n" +

            "    static String unquote(String s) {\n" +
            "        s = s == null ? \"\" : s.trim();\n" +
            "        if (s.length() >= 2 && s.charAt(0) == '\"' && s.charAt(s.length() - 1) == '\"') {\n" +
            "            String inner = s.substring(1, s.length() - 1);\n" +
            "            return inner.replace(\"\\\\\\\"\", \"\\\"\").replace(\"\\\\\\\\\", \"\\\\\");\n" +
            "        }\n" +
            "        return s;\n" +
            "    }\n\n" +

            // ---------------------------------------------------------------
            // Splits on top-level commas only, respecting [ ] nesting and "..." quoting.
            // (Same algorithm used to split e.g. "[2,7,11,15], 9" into two
            // top-level arguments, and "[1,2],[3,4]" into two array rows.)
            // ---------------------------------------------------------------
            "    static List<String> splitTopLevel(String s) {\n" +
            "        List<String> parts = new ArrayList<>();\n" +
            "        int depth = 0;\n" +
            "        boolean inQuotes = false;\n" +
            "        StringBuilder current = new StringBuilder();\n" +
            "        for (int i = 0; i < s.length(); i++) {\n" +
            "            char c = s.charAt(i);\n" +
            "            if (c == '\"' && (i == 0 || s.charAt(i - 1) != '\\\\')) {\n" +
            "                inQuotes = !inQuotes;\n" +
            "            }\n" +
            "            if (!inQuotes) {\n" +
            "                if (c == '[') depth++;\n" +
            "                if (c == ']') depth--;\n" +
            "            }\n" +
            "            if (c == ',' && depth == 0 && !inQuotes) {\n" +
            "                parts.add(current.toString());\n" +
            "                current.setLength(0);\n" +
            "            } else {\n" +
            "                current.append(c);\n" +
            "            }\n" +
            "        }\n" +
            "        if (current.length() > 0) {\n" +
            "            parts.add(current.toString());\n" +
            "        }\n" +
            "        return parts;\n" +
            "    }\n\n" +

            // ---------------------------------------------------------------
            // Generic result -> text formatter (unchanged behavior)
            // ---------------------------------------------------------------
            "    static String format(Object result) {\n" +
            "        if (result == null) return \"null\";\n" +
            "        if (result instanceof int[]) return arrToString((int[]) result);\n" +
            "        if (result instanceof long[]) return arrToString((long[]) result);\n" +
            "        if (result instanceof double[]) return arrToString((double[]) result);\n" +
            "        if (result instanceof boolean[]) return arrToString((boolean[]) result);\n" +
            "        if (result instanceof char[]) return new String((char[]) result);\n" +
            "        if (result instanceof int[][]) return arr2dToString((int[][]) result);\n" +
            "        if (result instanceof Object[]) return objArrToString((Object[]) result);\n" +
            "        if (result instanceof List) return listToString((List<?>) result);\n" +
            "        return String.valueOf(result);\n" +
            "    }\n\n" +

            "    static String arrToString(int[] a) {\n" +
            "        StringBuilder sb = new StringBuilder(\"[\");\n" +
            "        for (int i = 0; i < a.length; i++) { if (i > 0) sb.append(\",\"); sb.append(a[i]); }\n" +
            "        return sb.append(\"]\").toString();\n" +
            "    }\n\n" +

            "    static String arrToString(long[] a) {\n" +
            "        StringBuilder sb = new StringBuilder(\"[\");\n" +
            "        for (int i = 0; i < a.length; i++) { if (i > 0) sb.append(\",\"); sb.append(a[i]); }\n" +
            "        return sb.append(\"]\").toString();\n" +
            "    }\n\n" +

            "    static String arrToString(double[] a) {\n" +
            "        StringBuilder sb = new StringBuilder(\"[\");\n" +
            "        for (int i = 0; i < a.length; i++) { if (i > 0) sb.append(\",\"); sb.append(a[i]); }\n" +
            "        return sb.append(\"]\").toString();\n" +
            "    }\n\n" +

            "    static String arrToString(boolean[] a) {\n" +
            "        StringBuilder sb = new StringBuilder(\"[\");\n" +
            "        for (int i = 0; i < a.length; i++) { if (i > 0) sb.append(\",\"); sb.append(a[i]); }\n" +
            "        return sb.append(\"]\").toString();\n" +
            "    }\n\n" +

            "    static String arr2dToString(int[][] a) {\n" +
            "        StringBuilder sb = new StringBuilder(\"[\");\n" +
            "        for (int i = 0; i < a.length; i++) { if (i > 0) sb.append(\",\"); sb.append(arrToString(a[i])); }\n" +
            "        return sb.append(\"]\").toString();\n" +
            "    }\n\n" +

            "    static String objArrToString(Object[] a) {\n" +
            "        StringBuilder sb = new StringBuilder(\"[\");\n" +
            "        for (int i = 0; i < a.length; i++) { if (i > 0) sb.append(\",\"); sb.append(format(a[i])); }\n" +
            "        return sb.append(\"]\").toString();\n" +
            "    }\n\n" +

            "    static String listToString(List<?> l) {\n" +
            "        StringBuilder sb = new StringBuilder(\"[\");\n" +
            "        for (int i = 0; i < l.size(); i++) { if (i > 0) sb.append(\",\"); sb.append(format(l.get(i))); }\n" +
            "        return sb.append(\"]\").toString();\n" +
            "    }\n";
}
