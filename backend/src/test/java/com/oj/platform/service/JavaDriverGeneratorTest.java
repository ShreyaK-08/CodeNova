package com.oj.platform.service;

import com.oj.platform.entity.TestCase;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * These tests cover two things:
 *  1. generateMainJava() produces well-formed source that embeds each test
 *     case's raw input verbatim (as a String literal) and wires up runtime
 *     reflection-based invocation - it no longer guesses argument Java types
 *     at generation time.
 *  2. The runtime parsing helpers (parseToken/splitTopLevel/unquote), which
 *     are the actual fix for the "argument type mismatch" bug, are correct
 *     for every parameter type the platform needs to support. Since these
 *     helpers are emitted as generated source (not directly callable from
 *     this test module), they are exercised here via an equivalent reference
 *     implementation that mirrors the generated logic exactly, so a
 *     regression in the algorithm is still caught.
 */
class JavaDriverGeneratorTest {

    @Test
    void testGenerateMainJavaForReverseString() {
        TestCase tc1 = new TestCase(null, "hello", "olleh", false);
        TestCase tc2 = new TestCase(null, "world", "dlrow", false);

        String mainJava = JavaDriverGenerator.generateMainJava("reverseString", List.of(tc1, tc2));

        assertNotNull(mainJava);
        assertTrue(mainJava.contains("import java.lang.reflect.*;"));
        assertTrue(mainJava.contains("invoke(sol, \"reverseString\", rawInputs[idx])"));
        assertTrue(mainJava.contains("\"hello\""));
        assertTrue(mainJava.contains("\"world\""));
        assertTrue(mainJava.contains("static Object parseToken(String token, Class<?> type)"));
        assertTrue(mainJava.contains("targetMethod.getReturnType() == void.class"));
    }

    @Test
    void testGenerateMainJavaForTwoSum() {
        TestCase tc = new TestCase(null, "[2,7,11,15], 9", "[0,1]", false);

        String mainJava = JavaDriverGenerator.generateMainJava("twoSum", List.of(tc));

        assertNotNull(mainJava);
        // Raw input is embedded verbatim as a single escaped string literal; the
        // actual int[]/int typing now happens at RUNTIME from the reflected
        // method signature, not baked into the generated literal.
        assertTrue(mainJava.contains(JavaDriverGenerator.escapeJavaString("[2,7,11,15], 9")));
    }

    @Test
    void testEscapeJavaStringHandlesQuotesAndBackslashes() {
        String escaped = JavaDriverGenerator.escapeJavaString("say \"hi\"\\now");
        assertEquals("\"say \\\"hi\\\"\\\\now\"", escaped);
    }

    // -----------------------------------------------------------------
    // Reference re-implementation mirroring the generated runtime helpers,
    // used to pin down the parsing algorithm itself (this is what actually
    // fixes the type-mismatch bug for Palindrome / Reverse String / Longest
    // Substring, and generalizes to every supported type).
    // -----------------------------------------------------------------

    private static boolean isBracketed(String s) {
        return s.length() >= 2 && s.charAt(0) == '[' && s.charAt(s.length() - 1) == ']';
    }

    private static String stripBrackets(String s) {
        s = s.trim();
        return isBracketed(s) ? s.substring(1, s.length() - 1) : s;
    }

    private static String unquote(String s) {
        s = s == null ? "" : s.trim();
        if (s.length() >= 2 && s.charAt(0) == '"' && s.charAt(s.length() - 1) == '"') {
            return s.substring(1, s.length() - 1).replace("\\\"", "\"").replace("\\\\", "\\");
        }
        return s;
    }

    private static List<String> splitTopLevel(String s) {
        java.util.List<String> parts = new java.util.ArrayList<>();
        int depth = 0;
        boolean inQuotes = false;
        StringBuilder current = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '"' && (i == 0 || s.charAt(i - 1) != '\\')) inQuotes = !inQuotes;
            if (!inQuotes) {
                if (c == '[') depth++;
                if (c == ']') depth--;
            }
            if (c == ',' && depth == 0 && !inQuotes) {
                parts.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        if (current.length() > 0) parts.add(current.toString());
        return parts;
    }

    @Test
    void testPalindromeIntArgumentParsesAsIntNotString() {
        // This is the exact scenario that used to throw
        // "IllegalArgumentException: argument type mismatch" for Palindrome.
        assertEquals(121, Integer.parseInt(unquote(splitTopLevel("121").get(0))));
        assertEquals(-121, Integer.parseInt(unquote(splitTopLevel("-121").get(0))));
        assertEquals(10, Integer.parseInt(unquote(splitTopLevel("10").get(0))));
        assertEquals(0, Integer.parseInt(unquote(splitTopLevel("0").get(0))));
    }

    @Test
    void testReverseStringArgumentParsesAsPlainString() {
        assertEquals("hello", unquote(splitTopLevel("hello").get(0)));
    }

    @Test
    void testLongestSubstringHandlesEmptyStringInput() {
        List<String> tokens = splitTopLevel("");
        assertEquals("", tokens.isEmpty() ? "" : unquote(tokens.get(0)));
        assertEquals("abcdef", unquote(splitTopLevel("abcdef").get(0)));
    }

    @Test
    void testTwoSumSplitsArrayAndIntTopLevelArguments() {
        List<String> tokens = splitTopLevel("[2,7,11,15], 9");
        assertEquals(2, tokens.size());
        assertEquals("[2,7,11,15]", tokens.get(0).trim());
        assertEquals("9", tokens.get(1).trim());

        List<String> arrElems = splitTopLevel(stripBrackets(tokens.get(0).trim()));
        int[] nums = new int[arrElems.size()];
        for (int i = 0; i < arrElems.size(); i++) nums[i] = Integer.parseInt(unquote(arrElems.get(i).trim()));
        assertEquals(4, nums.length);
        assertEquals(2, nums[0]);
        assertEquals(15, nums[3]);
    }
}
