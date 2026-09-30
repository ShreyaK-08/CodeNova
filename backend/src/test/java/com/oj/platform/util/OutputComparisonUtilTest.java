package com.oj.platform.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OutputComparisonUtilTest {

    @Test
    void testExactMatch() {
        assertTrue(OutputComparisonUtil.matches("hello", "hello"));
    }

    @Test
    void testWindowsVsLinuxNewlines() {
        assertTrue(OutputComparisonUtil.matches("line1\r\nline2\r\n", "line1\nline2\n"));
    }

    @Test
    void testTrailingWhitespaceAndBlankLines() {
        assertTrue(OutputComparisonUtil.matches("  42   \n\n  \n", "42"));
    }

    @Test
    void testMultipleHorizontalSpaces() {
        assertTrue(OutputComparisonUtil.matches("1    2    3", "1 2 3"));
    }

    @Test
    void testArrayBracketNormalization() {
        assertTrue(OutputComparisonUtil.matches("[0, 1]", "[0,1]"));
        assertTrue(OutputComparisonUtil.matches("[ 0 , 1 ]", "[0,1]"));
    }

    @Test
    void testMeaningfulDifferenceFails() {
        assertFalse(OutputComparisonUtil.matches("42", "43"));
        assertFalse(OutputComparisonUtil.matches("[0,1]", "[1,0]"));
        assertFalse(OutputComparisonUtil.matches("hello world", "hello earth"));
    }
}
