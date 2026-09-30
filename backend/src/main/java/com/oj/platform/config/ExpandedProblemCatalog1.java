package com.oj.platform.config;

import com.oj.platform.entity.Difficulty;

import java.util.ArrayList;
import java.util.List;

/**
 * ExpandedProblemCatalog1 - Curated algorithmic and system coding problems.
 */
public class ExpandedProblemCatalog1 {

    private static String[] tc(String input, String expected, boolean hidden) {
        return new String[]{input, expected, hidden ? "true" : "false"};
    }

    public static List<Object[]> getSpecs() {
        List<Object[]> specs = new ArrayList<>();

        specs.add(new Object[]{
                "Counting Bits",
                "Given an integer n, return an array ans of length n + 1 such that for each i (0 <= i <= n), ans[i] is the number of 1's in the binary representation of i.",
                Difficulty.EASY, "Dynamic Programming, Bit Manipulation",
                "0 <= n <= 10^5",
                "countBits",
                "public class Solution {\n"
                        + "    public int[] countBits(int n) {\n"
                        + "        return new int[0];\n"
                        + "    }\n"
                        + "}",
                new String[][]{
                        tc("2", "[0,1,1]", false),
                        tc("5", "[0,1,1,2,1,2]", false),
                        tc("0", "[0]", false),
                        tc("1", "[0,1]", false),
                        tc("4", "[0,1,1,2,1]", false),
                        tc("7", "[0,1,1,2,1,2,2,3]", true),
                        tc("3", "[0,1,1,2]", true),
                        tc("8", "[0,1,1,2,1,2,2,3,1]", true),
                        tc("6", "[0,1,1,2,1,2,2]", true),
                        tc("10", "[0,1,1,2,1,2,2,3,1,2,2]", true),
                },
                new Object[][]{
                        {1, "ans[i] = ans[i >> 1] + (i & 1).", 1},
                        {2, "Bits of i is bits of i/2 plus 1 if i is odd.", 1},
                },
                "Counting Bits: Dynamic Programming",
                "Use previous results for i >> 1.",
                "1. ans = new int[n + 1].\n2. For i from 1 to n: ans[i] = ans[i >> 1] + (i & 1).",
                "Time: O(n), Space: O(1)",
                "```java\n"
                        + "public class Solution {\n"
                        + "    public int[] countBits(int n) {\n"
                        + "        int[] ans = new int[n + 1];\n"
                        + "        for (int i = 1; i <= n; i++) ans[i] = ans[i >> 1] + (i & 1);\n"
                        + "        return ans;\n"
                        + "    }\n"
                        + "}\n"
                        + "```",
                2
        });

        specs.add(new Object[]{
                "Evaluate Reverse Polish Notation",
                "Evaluate the value of an arithmetic expression in Reverse Polish Notation. Valid operators are +, -, *, and /.",
                Difficulty.MEDIUM, "Stack, Arrays, Math",
                "1 <= tokens.length <= 10^4",
                "evalRPN",
                "public class Solution {\n"
                        + "    public int evalRPN(String[] tokens) {\n"
                        + "        return 0;\n"
                        + "    }\n"
                        + "}",
                new String[][]{
                        tc("[\"2\",\"1\",\"+\",\"3\",\"*\"]", "9", false),
                        tc("[\"4\",\"13\",\"5\",\"/\",\"+\"]", "6", false),
                        tc("[\"10\",\"6\",\"9\",\"3\",\"+\",\"-11\",\"*\",\"/\",\"*\",\"17\",\"+\",\"5\",\"+\"]", "22", false),
                        tc("[\"3\",\"-4\",\"+\"]", "-1", false),
                        tc("[\"1\"]", "1", false),
                        tc("[\"18\"]", "18", true),
                        tc("[\"3\",\"3\",\"*\"]", "9", true),
                        tc("[\"5\",\"1\",\"-\"]", "4", true),
                        tc("[\"20\",\"4\",\"/\"]", "5", true),
                        tc("[\"4\",\"2\",\"/\",\"1\",\"-\"]", "1", true),
                },
                new Object[][]{
                        {1, "Push numbers to stack.", 1},
                        {2, "Pop two numbers when operator encountered.", 1},
                },
                "Evaluate RPN: Stack Evaluation",
                "Operands pushed to stack, operators pop 2 values.",
                "1. Stack<Integer> stack.\n2. Apply operation and push back.",
                "Time: O(n), Space: O(n)",
                "```java\n"
                        + "public class Solution {\n"
                        + "    public int evalRPN(String[] tokens) {\n"
                        + "        java.util.Deque<Integer> st = new java.util.ArrayDeque<>();\n"
                        + "        for (String t : tokens) {\n"
                        + "            if (t.equals(\"+\")) st.push(st.pop() + st.pop());\n"
                        + "            else if (t.equals(\"*\")) st.push(st.pop() * st.pop());\n"
                        + "            else if (t.equals(\"-\")) { int b = st.pop(), a = st.pop(); st.push(a - b); }\n"
                        + "            else if (t.equals(\"/\")) { int b = st.pop(), a = st.pop(); st.push(a / b); }\n"
                        + "            else st.push(Integer.parseInt(t));\n"
                        + "        }\n"
                        + "        return st.pop();\n"
                        + "    }\n"
                        + "}\n"
                        + "```",
                2
        });

        specs.add(new Object[]{
                "Generate Parentheses",
                "Given n pairs of parentheses, write a function to generate all combinations of well-formed parentheses.",
                Difficulty.MEDIUM, "Backtracking, String",
                "1 <= n <= 8",
                "generateParenthesis",
                "public class Solution {\n"
                        + "    public int generateParenthesis(int n) {\n"
                        + "        return 0;\n"
                        + "    }\n"
                        + "}",
                new String[][]{
                        tc("1", "1", false),
                        tc("2", "2", false),
                        tc("3", "5", false),
                        tc("4", "14", false),
                        tc("5", "42", false),
                        tc("6", "132", true),
                        tc("7", "429", true),
                        tc("8", "1430", true),
                        tc("1", "1", true),
                        tc("2", "2", true),
                },
                new Object[][]{
                        {1, "Backtracking with open and close counts.", 1},
                        {2, "Can add '(' if open < n, ')' if close < open.", 1},
                },
                "Generate Parentheses: Catalan Number Count",
                "Number of valid combinations equals n-th Catalan number.",
                "1. Count ways to place parentheses validly.",
                "Time: O(4^n / sqrt(n)), Space: O(n)",
                "```java\n"
                        + "public class Solution {\n"
                        + "    public int generateParenthesis(int n) {\n"
                        + "        long c = 1;\n"
                        + "        for (int i = 0; i < n; i++) c = c * 2 * (2 * i + 1) / (i + 2);\n"
                        + "        return (int) c;\n"
                        + "    }\n"
                        + "}\n"
                        + "```",
                2
        });

        specs.add(new Object[]{
                "Maximum Product Subarray",
                "Given an integer array nums, find a subarray that has the largest product, and return the product.",
                Difficulty.MEDIUM, "Arrays, Dynamic Programming",
                "1 <= nums.length <= 2 * 10^4",
                "maxProductSubarray",
                "public class Solution {\n"
                        + "    public int maxProductSubarray(int[] nums) {\n"
                        + "        return 0;\n"
                        + "    }\n"
                        + "}",
                new String[][]{
                        tc("[2,3,-2,4]", "6", false),
                        tc("[-2,0,-1]", "0", false),
                        tc("[-2]", "-2", false),
                        tc("[0,2]", "2", false),
                        tc("[-2,3,-4]", "24", false),
                        tc("[2,-5,-2,-4,3]", "24", true),
                        tc("[1,2,3,4]", "24", true),
                        tc("[-1,-2,-3]", "6", true),
                        tc("[0,0,0]", "0", true),
                        tc("[3,-1,4]", "4", true),
                },
                new Object[][]{
                        {1, "Track both max and min product so far.", 1},
                        {2, "Negative number swaps max and min.", 1},
                },
                "Maximum Product Subarray: Track Max and Min",
                "Multiplying by a negative number flips sign.",
                "1. maxP = nums[0], minP = nums[0], best = nums[0].\n2. Swap when num is negative.",
                "Time: O(n), Space: O(1)",
                "```java\n"
                        + "public class Solution {\n"
                        + "    public int maxProductSubarray(int[] nums) {\n"
                        + "        int res = nums[0], maxP = nums[0], minP = nums[0];\n"
                        + "        for (int i = 1; i < nums.length; i++) {\n"
                        + "            int x = nums[i];\n"
                        + "            if (x < 0) { int t = maxP; maxP = minP; minP = t; }\n"
                        + "            maxP = Math.max(x, maxP * x);\n"
                        + "            minP = Math.min(x, minP * x);\n"
                        + "            res = Math.max(res, maxP);\n"
                        + "        }\n"
                        + "        return res;\n"
                        + "    }\n"
                        + "}\n"
                        + "```",
                2
        });

        specs.add(new Object[]{
                "Decode Ways",
                "A message containing letters from A-Z can be encoded into numbers using the mapping 'A' -> '1' to 'Z' -> '26'. Determine the total number of ways to decode it.",
                Difficulty.MEDIUM, "String, Dynamic Programming",
                "1 <= s.length <= 100",
                "numDecodings",
                "public class Solution {\n"
                        + "    public int numDecodings(String s) {\n"
                        + "        return 0;\n"
                        + "    }\n"
                        + "}",
                new String[][]{
                        tc("12", "2", false),
                        tc("226", "3", false),
                        tc("06", "0", false),
                        tc("10", "1", false),
                        tc("27", "1", false),
                        tc("2101", "1", true),
                        tc("11106", "2", true),
                        tc("0", "0", true),
                        tc("1", "1", true),
                        tc("12345", "3", true),
                },
                new Object[][]{
                        {1, "Check 1-digit decode (s[i] != '0').", 1},
                        {2, "Check 2-digit decode (10 to 26).", 1},
                },
                "Decode Ways: Dynamic Programming",
                "dp[i] = dp[i-1] (if single digit valid) + dp[i-2] (if two digits valid).",
                "1. DP array of size n + 1.\n2. Check validity of substrings.",
                "Time: O(n), Space: O(1)",
                "```java\n"
                        + "public class Solution {\n"
                        + "    public int numDecodings(String s) {\n"
                        + "        if (s == null || s.length() == 0 || s.charAt(0) == '0') return 0;\n"
                        + "        int n = s.length(), p1 = 1, p2 = 1;\n"
                        + "        for (int i = 1; i < n; i++) {\n"
                        + "            int cur = 0;\n"
                        + "            int one = s.charAt(i) - '0';\n"
                        + "            int two = Integer.parseInt(s.substring(i - 1, i + 1));\n"
                        + "            if (one >= 1 && one <= 9) cur += p1;\n"
                        + "            if (two >= 10 && two <= 26) cur += p2;\n"
                        + "            p2 = p1;\n"
                        + "            p1 = cur;\n"
                        + "        }\n"
                        + "        return p1;\n"
                        + "    }\n"
                        + "}\n"
                        + "```",
                2
        });

        specs.add(new Object[]{
                "Longest Consecutive Sequence",
                "Given an unsorted array of integers nums, return the length of the longest consecutive elements sequence. Must run in O(n) time.",
                Difficulty.MEDIUM, "Arrays, Hash Table",
                "0 <= nums.length <= 10^5",
                "longestConsecutive",
                "public class Solution {\n"
                        + "    public int longestConsecutive(int[] nums) {\n"
                        + "        return 0;\n"
                        + "    }\n"
                        + "}",
                new String[][]{
                        tc("[100,4,200,1,3,2]", "4", false),
                        tc("[0,3,7,2,5,8,4,6,0,1]", "9", false),
                        tc("[]", "0", false),
                        tc("[1]", "1", false),
                        tc("[1,2,0,1]", "3", false),
                        tc("[9,1,4,7,3,-1,0,5,8,-1,6]", "7", true),
                        tc("[10,5,12,3]", "1", true),
                        tc("[1,2,3,4,5]", "5", true),
                        tc("[5,4,3,2,1]", "5", true),
                        tc("[1,9,3,10,4,20,2]", "4", true),
                },
                new Object[][]{
                        {1, "Add all elements into a HashSet.", 1},
                        {2, "Only start counting from numbers x where x-1 is NOT in the set.", 1},
                },
                "Longest Consecutive Sequence: HashSet Sequence Heads",
                "Only count starting from sequence roots.",
                "1. Set<Integer> set.\n2. If !set.contains(x - 1), count consecutive.",
                "Time: O(n), Space: O(n)",
                "```java\n"
                        + "public class Solution {\n"
                        + "    public int longestConsecutive(int[] nums) {\n"
                        + "        java.util.Set<Integer> set = new java.util.HashSet<>();\n"
                        + "        for (int x : nums) set.add(x);\n"
                        + "        int best = 0;\n"
                        + "        for (int x : set) {\n"
                        + "            if (!set.contains(x - 1)) {\n"
                        + "                int cur = x, streak = 1;\n"
                        + "                while (set.contains(cur + 1)) { cur++; streak++; }\n"
                        + "                best = Math.max(best, streak);\n"
                        + "            }\n"
                        + "        }\n"
                        + "        return best;\n"
                        + "    }\n"
                        + "}\n"
                        + "```",
                2
        });

        specs.add(new Object[]{
                "Sort Colors",
                "Given an array nums with n objects colored red, white, or blue, sort them in-place so that objects of the same color are adjacent, with colors in order red (0), white (1), blue (2).",
                Difficulty.MEDIUM, "Arrays, Two Pointers, Sorting",
                "1 <= nums.length <= 300",
                "sortColors",
                "public class Solution {\n"
                        + "    public int[] sortColors(int[] nums) {\n"
                        + "        return nums;\n"
                        + "    }\n"
                        + "}",
                new String[][]{
                        tc("[2,0,2,1,1,0]", "[0,0,1,1,2,2]", false),
                        tc("[2,0,1]", "[0,1,2]", false),
                        tc("[0]", "[0]", false),
                        tc("[1]", "[1]", false),
                        tc("[2]", "[2]", false),
                        tc("[1,2,0]", "[0,1,2]", true),
                        tc("[2,2,2,0,0,0,1,1]", "[0,0,0,1,1,2,2,2]", true),
                        tc("[0,1,2,0,1,2]", "[0,0,1,1,2,2]", true),
                        tc("[1,0]", "[0,1]", true),
                        tc("[2,1]", "[1,2]", true),
                },
                new Object[][]{
                        {1, "Dutch National Flag problem.", 1},
                        {2, "Three pointers: low, mid, high.", 1},
                },
                "Sort Colors: Dutch National Flag",
                "Partition array into three segments with 3 pointers.",
                "1. low = 0, mid = 0, high = len - 1.\n2. Swap based on nums[mid].",
                "Time: O(n), Space: O(1)",
                "```java\n"
                        + "public class Solution {\n"
                        + "    public int[] sortColors(int[] nums) {\n"
                        + "        int l = 0, m = 0, r = nums.length - 1;\n"
                        + "        while (m <= r) {\n"
                        + "            if (nums[m] == 0) { int t = nums[l]; nums[l++] = nums[m]; nums[m++] = t; }\n"
                        + "            else if (nums[m] == 1) { m++; }\n"
                        + "            else { int t = nums[r]; nums[r--] = nums[m]; nums[m] = t; }\n"
                        + "        }\n"
                        + "        return nums;\n"
                        + "    }\n"
                        + "}\n"
                        + "```",
                2
        });

        specs.add(new Object[]{
                "Gas Station",
                "There are n gas stations along a circular route. You have a car with an unlimited gas tank and it costs cost[i] to travel from station i to i+1. Return starting gas station index or -1.",
                Difficulty.MEDIUM, "Greedy, Arrays",
                "n == gas.length == cost.length",
                "canCompleteCircuit",
                "public class Solution {\n"
                        + "    public int canCompleteCircuit(int[] gas, int[] cost) {\n"
                        + "        return -1;\n"
                        + "    }\n"
                        + "}",
                new String[][]{
                        tc("[1,2,3,4,5], [3,4,5,1,2]", "3", false),
                        tc("[2,3,4], [3,4,3]", "-1", false),
                        tc("[2], [2]", "0", false),
                        tc("[3,1,1], [1,2,2]", "0", false),
                        tc("[5,1,2,3,4], [4,4,1,5,1]", "4", false),
                        tc("[1,2], [2,1]", "1", true),
                        tc("[4], [5]", "-1", true),
                        tc("[3,3,4], [3,4,4]", "-1", true),
                        tc("[5,8,2,8], [6,5,6,6]", "3", true),
                        tc("[1,1,3], [2,2,1]", "-1", true),
                },
                new Object[][]{
                        {1, "If total gas < total cost, it's impossible.", 1},
                        {2, "If tank drops below 0 at station i, next candidate start is i+1.", 1},
                },
                "Gas Station: Greedy One-Pass",
                "Total balance determines feasibility, local tank resets starting point.",
                "1. total = 0, cur = 0, start = 0.\n2. If cur < 0, start = i + 1, cur = 0.",
                "Time: O(n), Space: O(1)",
                "```java\n"
                        + "public class Solution {\n"
                        + "    public int canCompleteCircuit(int[] gas, int[] cost) {\n"
                        + "        int total = 0, cur = 0, start = 0;\n"
                        + "        for (int i = 0; i < gas.length; i++) {\n"
                        + "            int diff = gas[i] - cost[i];\n"
                        + "            total += diff;\n"
                        + "            cur += diff;\n"
                        + "            if (cur < 0) { start = i + 1; cur = 0; }\n"
                        + "        }\n"
                        + "        return total >= 0 ? start : -1;\n"
                        + "    }\n"
                        + "}\n"
                        + "```",
                2
        });

        specs.add(new Object[]{
                "Minimum Path Sum",
                "Given a m x n grid filled with non-negative numbers, find a path from top left to bottom right, which minimizes the sum of all numbers along its path. You can only move either down or right.",
                Difficulty.MEDIUM, "Dynamic Programming, Matrix",
                "1 <= m, n <= 200",
                "minPathSum",
                "public class Solution {\n"
                        + "    public int minPathSum(int[][] grid) {\n"
                        + "        return 0;\n"
                        + "    }\n"
                        + "}",
                new String[][]{
                        tc("[[1,3,1],[1,5,1],[4,2,1]]", "7", false),
                        tc("[[1,2,3],[4,5,6]]", "12", false),
                        tc("[[1]]", "1", false),
                        tc("[[1,2],[1,1]]", "3", false),
                        tc("[[1,5],[2,3]]", "6", false),
                        tc("[[5,4,3],[2,1,0]]", "8", true),
                        tc("[[1,1,1],[1,1,1]]", "4", true),
                        tc("[[9,1,4,8]]", "22", true),
                        tc("[[1],[2],[3]]", "6", true),
                        tc("[[1,2,5],[3,2,1]]", "6", true),
                },
                new Object[][]{
                        {1, "grid[i][j] += min(grid[i-1][j], grid[i][j-1]).", 1},
                        {2, "In-place DP or 1D array.", 1},
                },
                "Minimum Path Sum: In-Place DP",
                "Accumulate minimal cost from top and left neighbors.",
                "1. Initialize first row and column.\n2. Update inner cells with min(top, left) + val.",
                "Time: O(m*n), Space: O(1)",
                "```java\n"
                        + "public class Solution {\n"
                        + "    public int minPathSum(int[][] grid) {\n"
                        + "        int m = grid.length, n = grid[0].length;\n"
                        + "        for (int i = 0; i < m; i++) {\n"
                        + "            for (int j = 0; j < n; j++) {\n"
                        + "                if (i == 0 && j == 0) continue;\n"
                        + "                if (i == 0) grid[i][j] += grid[i][j - 1];\n"
                        + "                else if (j == 0) grid[i][j] += grid[i - 1][j];\n"
                        + "                else grid[i][j] += Math.min(grid[i - 1][j], grid[i][j - 1]);\n"
                        + "            }\n"
                        + "        }\n"
                        + "        return grid[m - 1][n - 1];\n"
                        + "    }\n"
                        + "}\n"
                        + "```",
                2
        });

        specs.add(new Object[]{
                "Palindromic Substrings",
                "Given a string s, return the number of palindromic substrings in it.",
                Difficulty.MEDIUM, "String, Dynamic Programming",
                "1 <= s.length <= 1000",
                "countSubstrings",
                "public class Solution {\n"
                        + "    public int countSubstrings(String s) {\n"
                        + "        return 0;\n"
                        + "    }\n"
                        + "}",
                new String[][]{
                        tc("abc", "3", false),
                        tc("aaa", "6", false),
                        tc("a", "1", false),
                        tc("aba", "4", false),
                        tc("abccba", "9", false),
                        tc("racecar", "10", true),
                        tc("noon", "6", true),
                        tc("madam", "7", true),
                        tc("abcdef", "6", true),
                        tc("aaaaa", "15", true),
                },
                new Object[][]{
                        {1, "Expand around centers.", 1},
                        {2, "Each center can be single char (2n-1 centers) or pair.", 1},
                },
                "Palindromic Substrings: Expand Around Centers",
                "Expand from each center counting palindromes.",
                "1. For each center i, expand odd (i, i) and even (i, i+1).\n2. Count valid steps.",
                "Time: O(n^2), Space: O(1)",
                "```java\n"
                        + "public class Solution {\n"
                        + "    public int countSubstrings(String s) {\n"
                        + "        int count = 0, n = s.length();\n"
                        + "        for (int i = 0; i < n; i++) {\n"
                        + "            count += expand(s, i, i) + expand(s, i, i + 1);\n"
                        + "        }\n"
                        + "        return count;\n"
                        + "    }\n"
                        + "    private int expand(String s, int l, int r) {\n"
                        + "        int c = 0;\n"
                        + "        while (l >= 0 && r < s.length() && s.charAt(l) == s.charAt(r)) { c++; l--; r++; }\n"
                        + "        return c;\n"
                        + "    }\n"
                        + "}\n"
                        + "```",
                2
        });

        specs.add(new Object[]{
                "Longest Palindromic Substring",
                "Given a string s, return the longest palindromic substring in s.",
                Difficulty.MEDIUM, "String, Dynamic Programming",
                "1 <= s.length <= 1000",
                "longestPalindrome",
                "public class Solution {\n"
                        + "    public String longestPalindrome(String s) {\n"
                        + "        return \"\";\n"
                        + "    }\n"
                        + "}",
                new String[][]{
                        tc("babad", "bab", false),
                        tc("cbbd", "bb", false),
                        tc("a", "a", false),
                        tc("ac", "a", false),
                        tc("racecar", "racecar", false),
                        tc("noon", "noon", true),
                        tc("abb", "bb", true),
                        tc("aaaa", "aaaa", true),
                        tc("bananas", "anana", true),
                        tc("abacdfgdcaba", "aba", true),
                },
                new Object[][]{
                        {1, "Expand around center for each index.", 1},
                        {2, "Track max length start and end.", 1},
                },
                "Longest Palindromic Substring: Center Expansion",
                "Expand around each center and track max length substring.",
                "1. For each center, find palindrome boundaries.\n2. Return substring with maximum length.",
                "Time: O(n^2), Space: O(1)",
                "```java\n"
                        + "public class Solution {\n"
                        + "    public String longestPalindrome(String s) {\n"
                        + "        if (s == null || s.length() < 1) return \"\";\n"
                        + "        int start = 0, end = 0;\n"
                        + "        for (int i = 0; i < s.length(); i++) {\n"
                        + "            int len1 = expand(s, i, i), len2 = expand(s, i, i + 1);\n"
                        + "            int len = Math.max(len1, len2);\n"
                        + "            if (len > end - start) {\n"
                        + "                start = i - (len - 1) / 2;\n"
                        + "                end = i + len / 2;\n"
                        + "            }\n"
                        + "        }\n"
                        + "        return s.substring(start, end + 1);\n"
                        + "    }\n"
                        + "    private int expand(String s, int l, int r) {\n"
                        + "        while (l >= 0 && r < s.length() && s.charAt(l) == s.charAt(r)) { l--; r++; }\n"
                        + "        return r - l - 1;\n"
                        + "    }\n"
                        + "}\n"
                        + "```",
                2
        });

        specs.add(new Object[]{
                "Rotate Image",
                "You are given an n x n 2D matrix representing an image, rotate the image by 90 degrees (clockwise) in-place.",
                Difficulty.MEDIUM, "Arrays, Matrix",
                "matrix is n x n\n1 <= n <= 20",
                "rotate",
                "public class Solution {\n"
                        + "    public int[][] rotate(int[][] matrix) {\n"
                        + "        return matrix;\n"
                        + "    }\n"
                        + "}",
                new String[][]{
                        tc("[[1,2,3],[4,5,6],[7,8,9]]", "[[7,4,1],[8,5,2],[9,6,3]]", false),
                        tc("[[5,1,9,11],[2,4,8,10],[13,3,6,7],[15,14,12,16]]", "[[15,13,2,5],[14,3,4,1],[12,6,8,9],[16,7,10,11]]", false),
                        tc("[[1]]", "[[1]]", false),
                        tc("[[1,2],[3,4]]", "[[3,1],[4,2]]", false),
                        tc("[[0,1],[1,0]]", "[[1,0],[0,1]]", false),
                        tc("[[1,2,3,4],[5,6,7,8],[9,10,11,12],[13,14,15,16]]", "[[13,9,5,1],[14,10,6,2],[15,11,7,3],[16,12,8,4]]", true),
                        tc("[[2,4],[6,8]]", "[[6,2],[8,4]]", true),
                        tc("[[9,8,7],[6,5,4],[3,2,1]]", "[[3,6,9],[2,5,8],[1,4,7]]", true),
                        tc("[[1,0,0],[0,1,0],[0,0,1]]", "[[0,0,1],[0,1,0],[1,0,0]]", true),
                        tc("[[1,2],[1,2]]", "[[1,1],[2,2]]", true),
                },
                new Object[][]{
                        {1, "Transpose the matrix (swap matrix[i][j] with matrix[j][i]).", 1},
                        {2, "Reverse each row horizontally.", 1},
                },
                "Rotate Image: Transpose and Reverse",
                "Rotating clockwise 90 degrees = Transpose + Horizontal Reflection.",
                "1. Transpose matrix.\n2. Reverse each row.",
                "Time: O(n^2), Space: O(1)",
                "```java\n"
                        + "public class Solution {\n"
                        + "    public int[][] rotate(int[][] matrix) {\n"
                        + "        int n = matrix.length;\n"
                        + "        for (int i = 0; i < n; i++) {\n"
                        + "            for (int j = i; j < n; j++) {\n"
                        + "                int t = matrix[i][j]; matrix[i][j] = matrix[j][i]; matrix[j][i] = t;\n"
                        + "            }\n"
                        + "        }\n"
                        + "        for (int i = 0; i < n; i++) {\n"
                        + "            for (int j = 0; j < n / 2; j++) {\n"
                        + "                int t = matrix[i][j]; matrix[i][j] = matrix[i][n - 1 - j]; matrix[i][n - 1 - j] = t;\n"
                        + "            }\n"
                        + "        }\n"
                        + "        return matrix;\n"
                        + "    }\n"
                        + "}\n"
                        + "```",
                2
        });

        specs.add(new Object[]{
                "3Sum",
                "Given an integer array nums, return all the triplets [nums[i], nums[j], nums[k]] such that i != j, i != k, and j != k, and nums[i] + nums[j] + nums[k] == 0. The solution set must not contain duplicate triplets. Return total count of valid triplets.",
                Difficulty.MEDIUM, "Arrays, Two Pointers, Sorting",
                "3 <= nums.length <= 3000",
                "threeSumCount",
                "public class Solution {\n"
                        + "    public int threeSumCount(int[] nums) {\n"
                        + "        return 0;\n"
                        + "    }\n"
                        + "}",
                new String[][]{
                        tc("[-1,0,1,2,-1,-4]", "2", false),
                        tc("[0,1,1]", "0", false),
                        tc("[0,0,0]", "1", false),
                        tc("[1,2,-2,-1]", "0", false),
                        tc("[-2,0,1,1,2]", "2", false),
                        tc("[-4,-2,-2,-2,0,1,2,2,2,3,3,4,4,6,6]", "6", true),
                        tc("[0,0,0,0]", "1", true),
                        tc("[-1,0,1]", "1", true),
                        tc("[3,0,-2,-1,1,2]", "3", true),
                        tc("[-5,1,2,3,4]", "2", true),
                },
                new Object[][]{
                        {1, "Sort the array first.", 1},
                        {2, "Fix the first number and use two pointers for the rest.", 1},
                },
                "3Sum: Two Pointers with Sorting",
                "Sort and use two pointers, skipping duplicates.",
                "1. Sort array.\n2. For each i, use two pointers l and r.\n3. Count unique triplets summing to 0.",
                "Time: O(n^2), Space: O(1)",
                "```java\n"
                        + "public class Solution {\n"
                        + "    public int threeSumCount(int[] nums) {\n"
                        + "        java.util.Arrays.sort(nums);\n"
                        + "        int count = 0, n = nums.length;\n"
                        + "        for (int i = 0; i < n - 2; i++) {\n"
                        + "            if (i > 0 && nums[i] == nums[i - 1]) continue;\n"
                        + "            int l = i + 1, r = n - 1;\n"
                        + "            while (l < r) {\n"
                        + "                int sum = nums[i] + nums[l] + nums[r];\n"
                        + "                if (sum == 0) {\n"
                        + "                    count++;\n"
                        + "                    while (l < r && nums[l] == nums[l + 1]) l++;\n"
                        + "                    while (l < r && nums[r] == nums[r - 1]) r--;\n"
                        + "                    l++; r--;\n"
                        + "                } else if (sum < 0) l++;\n"
                        + "                else r--;\n"
                        + "            }\n"
                        + "        }\n"
                        + "        return count;\n"
                        + "    }\n"
                        + "}\n"
                        + "```",
                2
        });

        specs.add(new Object[]{
                "Search in Rotated Sorted Array",
                "Given the array nums after the possible rotation and an integer target, return the index of target if it is in nums, or -1 if it is not in nums. Must run in O(log n).",
                Difficulty.MEDIUM, "Arrays, Binary Search",
                "1 <= nums.length <= 5000",
                "search",
                "public class Solution {\n"
                        + "    public int search(int[] nums, int target) {\n"
                        + "        return -1;\n"
                        + "    }\n"
                        + "}",
                new String[][]{
                        tc("[4,5,6,7,0,1,2], 0", "4", false),
                        tc("[4,5,6,7,0,1,2], 3", "-1", false),
                        tc("[1], 0", "-1", false),
                        tc("[1], 1", "0", false),
                        tc("[3,1], 1", "1", false),
                        tc("[5,1,3], 5", "0", true),
                        tc("[4,5,6,7,8,1,2,3], 8", "4", true),
                        tc("[6,7,1,2,3,4,5], 3", "4", true),
                        tc("[2,3,4,5,1], 1", "4", true),
                        tc("[1,3,5], 1", "0", true),
                },
                new Object[][]{
                        {1, "One half of the array is always sorted.", 1},
                        {2, "Check if target lies within the sorted half.", 1},
                },
                "Search in Rotated Sorted Array: Binary Search",
                "Identify which half is strictly sorted.",
                "1. l = 0, r = len - 1.\n2. Check if left or right half is sorted and contains target.",
                "Time: O(log n), Space: O(1)",
                "```java\n"
                        + "public class Solution {\n"
                        + "    public int search(int[] nums, int target) {\n"
                        + "        int l = 0, r = nums.length - 1;\n"
                        + "        while (l <= r) {\n"
                        + "            int m = l + (r - l) / 2;\n"
                        + "            if (nums[m] == target) return m;\n"
                        + "            if (nums[l] <= nums[m]) {\n"
                        + "                if (target >= nums[l] && target < nums[m]) r = m - 1; else l = m + 1;\n"
                        + "            } else {\n"
                        + "                if (target > nums[m] && target <= nums[r]) l = m + 1; else r = m - 1;\n"
                        + "            }\n"
                        + "        }\n"
                        + "        return -1;\n"
                        + "    }\n"
                        + "}\n"
                        + "```",
                2
        });

        specs.add(new Object[]{
                "House Robber II",
                "Houses are arranged in a circle: the first house is the neighbor of the last one. Determine maximum amount of money you can rob without alerting the police.",
                Difficulty.MEDIUM, "Dynamic Programming, Arrays",
                "1 <= nums.length <= 100",
                "rob",
                "public class Solution {\n"
                        + "    public int rob(int[] nums) {\n"
                        + "        return 0;\n"
                        + "    }\n"
                        + "}",
                new String[][]{
                        tc("[2,3,2]", "3", false),
                        tc("[1,2,3,1]", "4", false),
                        tc("[1,2,3]", "3", false),
                        tc("[1]", "1", false),
                        tc("[5,1,1,5]", "6", false),
                        tc("[200,3,140,20,10]", "340", true),
                        tc("[1,2]", "2", true),
                        tc("[1,3,1,3,100]", "103", true),
                        tc("[6,6,4,8,4,3,3,10]", "24", true),
                        tc("[1,7,9,2]", "11", true),
                },
                new Object[][]{
                        {1, "Cannot rob both first and last houses.", 1},
                        {2, "Take max of robbing 0..n-2 and 1..n-1.", 1},
                },
                "House Robber II: Two Linear Passes",
                "Break circle into two linear subproblems.",
                "1. If n == 1 return nums[0].\n2. Return max(robLinear(0, n-2), robLinear(1, n-1)).",
                "Time: O(n), Space: O(1)",
                "```java\n"
                        + "public class Solution {\n"
                        + "    public int rob(int[] nums) {\n"
                        + "        if (nums.length == 1) return nums[0];\n"
                        + "        return Math.max(robSub(nums, 0, nums.length - 2), robSub(nums, 1, nums.length - 1));\n"
                        + "    }\n"
                        + "    private int robSub(int[] nums, int l, int r) {\n"
                        + "        int p1 = 0, p2 = 0;\n"
                        + "        for (int i = l; i <= r; i++) {\n"
                        + "            int t = Math.max(p1, p2 + nums[i]); p2 = p1; p1 = t;\n"
                        + "        }\n"
                        + "        return p1;\n"
                        + "    }\n"
                        + "}\n"
                        + "```",
                2
        });

        specs.add(new Object[]{
                "Jump Game II",
                "Return the minimum number of jumps to reach nums[n - 1] from index 0. You are guaranteed that you can reach the end.",
                Difficulty.MEDIUM, "Greedy, Dynamic Programming",
                "1 <= nums.length <= 10^4",
                "jump",
                "public class Solution {\n"
                        + "    public int jump(int[] nums) {\n"
                        + "        return 0;\n"
                        + "    }\n"
                        + "}",
                new String[][]{
                        tc("[2,3,1,1,4]", "2", false),
                        tc("[2,3,0,1,4]", "2", false),
                        tc("[0]", "0", false),
                        tc("[1,2]", "1", false),
                        tc("[1,1,1,1]", "3", false),
                        tc("[2,1]", "1", true),
                        tc("[3,2,1]", "1", true),
                        tc("[5,9,3,2,1,0,2,3,3,1,0,0]", "3", true),
                        tc("[1,2,3,4,5]", "3", true),
                        tc("[10,9,8,7,6,5,4,3,2,1,1]", "2", true),
                },
                new Object[][]{
                        {1, "Think BFS levels.", 1},
                        {2, "Track current jump's end and farthest reach.", 1},
                },
                "Jump Game II: BFS Greedy Intervals",
                "Increment jumps when index reaches current interval boundary.",
                "1. jumps = 0, curEnd = 0, farthest = 0.\n2. Update farthest, if i == curEnd increment jumps.",
                "Time: O(n), Space: O(1)",
                "```java\n"
                        + "public class Solution {\n"
                        + "    public int jump(int[] nums) {\n"
                        + "        int jumps = 0, curEnd = 0, curFarthest = 0;\n"
                        + "        for (int i = 0; i < nums.length - 1; i++) {\n"
                        + "            curFarthest = Math.max(curFarthest, i + nums[i]);\n"
                        + "            if (i == curEnd) { jumps++; curEnd = curFarthest; }\n"
                        + "        }\n"
                        + "        return jumps;\n"
                        + "    }\n"
                        + "}\n"
                        + "```",
                2
        });

        specs.add(new Object[]{
                "Group Anagrams",
                "Given an array of strings strs, group the anagrams together. Return the number of groups formed.",
                Difficulty.MEDIUM, "Hash Table, String",
                "1 <= strs.length <= 10^4",
                "groupAnagramsCount",
                "public class Solution {\n"
                        + "    public int groupAnagramsCount(String[] strs) {\n"
                        + "        return 0;\n"
                        + "    }\n"
                        + "}",
                new String[][]{
                        tc("[\"eat\",\"tea\",\"tan\",\"ate\",\"nat\",\"bat\"]", "3", false),
                        tc("[\"\"]", "1", false),
                        tc("[\"a\"]", "1", false),
                        tc("[\"ab\",\"ba\",\"abc\"]", "2", false),
                        tc("[\"cat\",\"dog\",\"act\",\"god\"]", "2", false),
                        tc("[\"a\",\"b\",\"c\"]", "3", true),
                        tc("[\"listen\",\"silent\",\"enlist\",\"rat\"]", "2", true),
                        tc("[\"hello\",\"olleh\",\"world\"]", "2", true),
                        tc("[\"stop\",\"pots\",\"tops\",\"spot\"]", "1", true),
                        tc("[\"abc\",\"def\",\"ghi\"]", "3", true),
                },
                new Object[][]{
                        {1, "Sort each word's characters as key.", 1},
                        {2, "Map sorted string to list of anagrams.", 1},
                },
                "Group Anagrams: Canonical Sorted Key",
                "Sorted characters provide canonical anagram key.",
                "1. Map<String, List> map.\n2. Count map keys.",
                "Time: O(n * k log k), Space: O(n * k)",
                "```java\n"
                        + "public class Solution {\n"
                        + "    public int groupAnagramsCount(String[] strs) {\n"
                        + "        java.util.Map<String, Integer> map = new java.util.HashMap<>();\n"
                        + "        for (String s : strs) {\n"
                        + "            char[] ca = s.toCharArray();\n"
                        + "            java.util.Arrays.sort(ca);\n"
                        + "            String k = new String(ca);\n"
                        + "            map.put(k, map.getOrDefault(k, 0) + 1);\n"
                        + "        }\n"
                        + "        return map.size();\n"
                        + "    }\n"
                        + "}\n"
                        + "```",
                2
        });

        specs.add(new Object[]{
                "Top K Frequent Elements",
                "Given an integer array nums and an integer k, return the k most frequent elements in any order.",
                Difficulty.MEDIUM, "Heap, Hash Table, Bucket Sort",
                "1 <= nums.length <= 10^5",
                "topKFrequent",
                "public class Solution {\n"
                        + "    public int[] topKFrequent(int[] nums, int k) {\n"
                        + "        return new int[0];\n"
                        + "    }\n"
                        + "}",
                new String[][]{
                        tc("[1,1,1,2,2,3], 2", "[1,2]", false),
                        tc("[1], 1", "[1]", false),
                        tc("[4,1,-1,2,-1,2,3], 2", "[-1,2]", false),
                        tc("[1,2,3], 3", "[1,2,3]", false),
                        tc("[5,5,5,5], 1", "[5]", false),
                        tc("[1,2,2,3,3,3], 1", "[3]", true),
                        tc("[1,1,2,2,3,3], 2", "[1,2]", true),
                        tc("[10,20,30,10,20,10], 2", "[10,20]", true),
                        tc("[9,9,9,8,8,7], 2", "[9,8]", true),
                        tc("[1,2,3,4,5,6], 1", "[1]", true),
                },
                new Object[][]{
                        {1, "Count frequencies in a map.", 1},
                        {2, "Bucket sort or min-heap of size k.", 1},
                },
                "Top K Frequent Elements: Bucket Sort",
                "Frequencies range from 1 to n, bucket sort gives O(n).",
                "1. Frequency map.\n2. Group by frequency buckets.\n3. Collect top k.",
                "Time: O(n), Space: O(n)",
                "```java\n"
                        + "public class Solution {\n"
                        + "    public int[] topKFrequent(int[] nums, int k) {\n"
                        + "        java.util.Map<Integer, Integer> map = new java.util.HashMap<>();\n"
                        + "        for (int n : nums) map.put(n, map.getOrDefault(n, 0) + 1);\n"
                        + "        java.util.PriorityQueue<Integer> pq = new java.util.PriorityQueue<>((a, b) -> map.get(a) - map.get(b));\n"
                        + "        for (int n : map.keySet()) {\n"
                        + "            pq.offer(n);\n"
                        + "            if (pq.size() > k) pq.poll();\n"
                        + "        }\n"
                        + "        int[] res = new int[k];\n"
                        + "        for (int i = 0; i < k; i++) res[i] = pq.poll();\n"
                        + "        return res;\n"
                        + "    }\n"
                        + "}\n"
                        + "```",
                2
        });

        specs.add(new Object[]{
                "Letter Combinations of a Phone Number",
                "Given a string containing digits from 2-9 inclusive, return the total count of possible letter combinations that the number could represent.",
                Difficulty.MEDIUM, "Hash Table, String, Backtracking",
                "0 <= digits.length <= 4",
                "letterCombinationsCount",
                "public class Solution {\n"
                        + "    public int letterCombinationsCount(String digits) {\n"
                        + "        return 0;\n"
                        + "    }\n"
                        + "}",
                new String[][]{
                        tc("23", "9", false),
                        tc("8", "3", false),
                        tc("2", "3", false),
                        tc("7", "4", false),
                        tc("79", "16", false),
                        tc("234", "27", true),
                        tc("99", "16", true),
                        tc("22", "9", true),
                        tc("2345", "81", true),
                        tc("777", "64", true),
                },
                new Object[][]{
                        {1, "Map digits to letters.", 1},
                        {2, "Combinations count is the product of letter counts for each digit.", 1},
                },
                "Letter Combinations: Product of Letter Choices",
                "Multiply letter choices per digit.",
                "1. If digits empty return 0.\n2. Multiply count for each digit (7 and 9 have 4, others have 3).",
                "Time: O(n), Space: O(1)",
                "```java\n"
                        + "public class Solution {\n"
                        + "    public int letterCombinationsCount(String digits) {\n"
                        + "        if (digits == null || digits.trim().isEmpty()) return 0;\n"
                        + "        int total = 1;\n"
                        + "        for (char c : digits.trim().toCharArray()) {\n"
                        + "            if (c == '7' || c == '9') total *= 4; else if (c >= '2' && c <= '8') total *= 3;\n"
                        + "        }\n"
                        + "        return total;\n"
                        + "    }\n"
                        + "}\n"
                        + "```",
                2
        });

        specs.add(new Object[]{
                "Permutations",
                "Given an array nums of distinct integers, return the total count of possible permutations.",
                Difficulty.MEDIUM, "Backtracking",
                "1 <= nums.length <= 6",
                "permuteCount",
                "public class Solution {\n"
                        + "    public int permuteCount(int[] nums) {\n"
                        + "        return 0;\n"
                        + "    }\n"
                        + "}",
                new String[][]{
                        tc("[1,2,3]", "6", false),
                        tc("[0,1]", "2", false),
                        tc("[1]", "1", false),
                        tc("[1,2,3,4]", "24", false),
                        tc("[1,2,3,4,5]", "120", false),
                        tc("[1,2,3,4,5,6]", "720", true),
                        tc("[5,6]", "2", true),
                        tc("[9,8,7]", "6", true),
                        tc("[2,4,6,8]", "24", true),
                        tc("[3]", "1", true),
                },
                new Object[][]{
                        {1, "Number of permutations of n distinct items is n!.", 1},
                        {2, "Factorial computation.", 1},
                },
                "Permutations Count: Factorial Formula",
                "Total permutations for n distinct elements is n factorial.",
                "1. ans = 1.\n2. Multiply from 1 to n.",
                "Time: O(n), Space: O(1)",
                "```java\n"
                        + "public class Solution {\n"
                        + "    public int permuteCount(int[] nums) {\n"
                        + "        int res = 1;\n"
                        + "        for (int i = 2; i <= nums.length; i++) res *= i;\n"
                        + "        return res;\n"
                        + "    }\n"
                        + "}\n"
                        + "```",
                2
        });

        specs.add(new Object[]{
                "Subsets",
                "Given an integer array nums of unique elements, return the total number of possible subsets (the power set).",
                Difficulty.MEDIUM, "Backtracking, Bit Manipulation",
                "1 <= nums.length <= 10",
                "subsetsCount",
                "public class Solution {\n"
                        + "    public int subsetsCount(int[] nums) {\n"
                        + "        return 0;\n"
                        + "    }\n"
                        + "}",
                new String[][]{
                        tc("[1,2,3]", "8", false),
                        tc("[0]", "2", false),
                        tc("[1,2]", "4", false),
                        tc("[1,2,3,4]", "16", false),
                        tc("[1,2,3,4,5]", "32", false),
                        tc("[1,2,3,4,5,6]", "64", true),
                        tc("[7]", "2", true),
                        tc("[8,9]", "4", true),
                        tc("[10,20,30]", "8", true),
                        tc("[1,2,3,4,5,6,7]", "128", true),
                },
                new Object[][]{
                        {1, "Each element can either be included or excluded.", 1},
                        {2, "2^n total subsets.", 1},
                },
                "Subsets Count: 2^n",
                "Every element has 2 choices.",
                "1. return 1 << nums.length;",
                "Time: O(1), Space: O(1)",
                "```java\n"
                        + "public class Solution {\n"
                        + "    public int subsetsCount(int[] nums) {\n"
                        + "        return 1 << nums.length;\n"
                        + "    }\n"
                        + "}\n"
                        + "```",
                2
        });

        specs.add(new Object[]{
                "Combination Sum",
                "Given an array of distinct integers candidates and a target integer, return the count of unique combinations of candidates where the chosen numbers sum to target.",
                Difficulty.MEDIUM, "Backtracking, Dynamic Programming",
                "1 <= candidates.length <= 30",
                "combinationSumCount",
                "public class Solution {\n"
                        + "    public int combinationSumCount(int[] candidates, int target) {\n"
                        + "        return 0;\n"
                        + "    }\n"
                        + "}",
                new String[][]{
                        tc("[2,3,6,7], 7", "2", false),
                        tc("[2,3,5], 8", "3", false),
                        tc("[2], 1", "0", false),
                        tc("[1], 3", "1", false),
                        tc("[2,4], 6", "2", false),
                        tc("[3,5], 11", "1", true),
                        tc("[2,3,5], 10", "5", true),
                        tc("[2,4,6], 8", "3", true),
                        tc("[1,2], 4", "3", true),
                        tc("[5], 5", "1", true),
                },
                new Object[][]{
                        {1, "Unbounded knapsack style counting.", 1},
                        {2, "dp[i] += dp[i - c] for coin c.", 1},
                },
                "Combination Sum: Unbounded DP Counting",
                "dp[s] represents combinations summing to s.",
                "1. dp = new int[target + 1], dp[0] = 1.\n2. For each coin, update dp.",
                "Time: O(N * Target), Space: O(Target)",
                "```java\n"
                        + "public class Solution {\n"
                        + "    public int combinationSumCount(int[] candidates, int target) {\n"
                        + "        int[] dp = new int[target + 1];\n"
                        + "        dp[0] = 1;\n"
                        + "        for (int c : candidates) {\n"
                        + "            for (int i = c; i <= target; i++) dp[i] += dp[i - c];\n"
                        + "        }\n"
                        + "        return dp[target];\n"
                        + "    }\n"
                        + "}\n"
                        + "```",
                2
        });

        specs.add(new Object[]{
                "Spiral Matrix",
                "Given an m x n matrix, return all elements of the matrix in spiral order.",
                Difficulty.MEDIUM, "Arrays, Matrix, Simulation",
                "1 <= m, n <= 10",
                "spiralOrder",
                "public class Solution {\n"
                        + "    public int[] spiralOrder(int[][] matrix) {\n"
                        + "        return new int[0];\n"
                        + "    }\n"
                        + "}",
                new String[][]{
                        tc("[[1,2,3],[4,5,6],[7,8,9]]", "[1,2,3,6,9,8,7,4,5]", false),
                        tc("[[1,2,3,4],[5,6,7,8],[9,10,11,12]]", "[1,2,3,4,8,12,11,10,9,5,6,7]", false),
                        tc("[[1]]", "[1]", false),
                        tc("[[1,2],[3,4]]", "[1,2,4,3]", false),
                        tc("[[1,2,3]]", "[1,2,3]", false),
                        tc("[[1],[2],[3]]", "[1,2,3]", true),
                        tc("[[2,5,8],[4,0,-1]]", "[2,5,8,-1,0,4]", true),
                        tc("[[1,2],[4,3]]", "[1,2,3,4]", true),
                        tc("[[6,9,7]]", "[6,9,7]", true),
                        tc("[[7],[9],[6]]", "[7,9,6]", true),
                },
                new Object[][]{
                        {1, "Maintain four boundaries: top, bottom, left, right.", 1},
                        {2, "Traverse right, down, left, up, shrinking boundaries.", 1},
                },
                "Spiral Matrix: Boundary Shrinking Simulation",
                "Simulate movement along 4 boundaries.",
                "1. top=0, bot=m-1, left=0, right=n-1.\n2. Collect border elements and shrink.",
                "Time: O(m*n), Space: O(1)",
                "```java\n"
                        + "public class Solution {\n"
                        + "    public int[] spiralOrder(int[][] matrix) {\n"
                        + "        int m = matrix.length, n = matrix[0].length;\n"
                        + "        int[] res = new int[m * n];\n"
                        + "        int idx = 0, top = 0, bot = m - 1, left = 0, right = n - 1;\n"
                        + "        while (top <= bot && left <= right) {\n"
                        + "            for (int j = left; j <= right; j++) res[idx++] = matrix[top][j];\n"
                        + "            top++;\n"
                        + "            for (int i = top; i <= bot; i++) res[idx++] = matrix[i][right];\n"
                        + "            right--;\n"
                        + "            if (top <= bot) {\n"
                        + "                for (int j = right; j >= left; j--) res[idx++] = matrix[bot][j];\n"
                        + "                bot--;\n"
                        + "            }\n"
                        + "            if (left <= right) {\n"
                        + "                for (int i = bot; i >= top; i--) res[idx++] = matrix[i][left];\n"
                        + "                left++;\n"
                        + "            }\n"
                        + "        }\n"
                        + "        return res;\n"
                        + "    }\n"
                        + "}\n"
                        + "```",
                2
        });

        specs.add(new Object[]{
                "Set Matrix Zeroes",
                "Given an m x n integer matrix, if an element is 0, set its entire row and column to 0's. You must do it in place.",
                Difficulty.MEDIUM, "Arrays, Matrix",
                "1 <= m, n <= 200",
                "setZeroes",
                "public class Solution {\n"
                        + "    public int[][] setZeroes(int[][] matrix) {\n"
                        + "        return matrix;\n"
                        + "    }\n"
                        + "}",
                new String[][]{
                        tc("[[1,1,1],[1,0,1],[1,1,1]]", "[[1,0,1],[0,0,0],[1,0,1]]", false),
                        tc("[[0,1,2,0],[3,4,5,2],[1,3,1,5]]", "[[0,0,0,0],[0,4,5,0],[0,3,1,0]]", false),
                        tc("[[1]]", "[[1]]", false),
                        tc("[[0]]", "[[0]]", false),
                        tc("[[1,0],[1,1]]", "[[0,0],[1,0]]", false),
                        tc("[[1,2,3],[4,0,6],[7,8,9]]", "[[1,0,3],[0,0,0],[7,0,9]]", true),
                        tc("[[0,1],[1,1]]", "[[0,0],[0,1]]", true),
                        tc("[[1,1],[0,1]]", "[[0,0],[0,1]]", true),
                        tc("[[1,2],[3,4]]", "[[1,2],[3,4]]", true),
                        tc("[[0,0],[0,0]]", "[[0,0],[0,0]]", true),
                },
                new Object[][]{
                        {1, "Use first row and column as markers.", 1},
                        {2, "Track if first row / col initially contained zero with booleans.", 1},
                },
                "Set Matrix Zeroes: First Row/Col Marker",
                "O(1) space by reusing matrix boundaries.",
                "1. Check if first row/col has zero.\n2. Mark inner zeroes on borders.\n3. Zero out cells.",
                "Time: O(m*n), Space: O(1)",
                "```java\n"
                        + "public class Solution {\n"
                        + "    public int[][] setZeroes(int[][] matrix) {\n"
                        + "        int m = matrix.length, n = matrix[0].length;\n"
                        + "        boolean firstRow = false, firstCol = false;\n"
                        + "        for (int i = 0; i < m; i++) if (matrix[i][0] == 0) firstCol = true;\n"
                        + "        for (int j = 0; j < n; j++) if (matrix[0][j] == 0) firstRow = true;\n"
                        + "        for (int i = 1; i < m; i++) {\n"
                        + "            for (int j = 1; j < n; j++) if (matrix[i][j] == 0) { matrix[i][0] = 0; matrix[0][j] = 0; }\n"
                        + "        }\n"
                        + "        for (int i = 1; i < m; i++) {\n"
                        + "            for (int j = 1; j < n; j++) if (matrix[i][0] == 0 || matrix[0][j] == 0) matrix[i][j] = 0;\n"
                        + "        }\n"
                        + "        if (firstCol) for (int i = 0; i < m; i++) matrix[i][0] = 0;\n"
                        + "        if (firstRow) for (int j = 0; j < n; j++) matrix[0][j] = 0;\n"
                        + "        return matrix;\n"
                        + "    }\n"
                        + "}\n"
                        + "```",
                2
        });

        specs.add(new Object[]{
                "Course Schedule",
                "There are a total of numCourses courses you have to take, labeled from 0 to numCourses - 1. You are given an array prerequisites. Return true if you can finish all courses, or false otherwise.",
                Difficulty.MEDIUM, "Graph, Topological Sort, Depth-First Search",
                "1 <= numCourses <= 2000",
                "canFinish",
                "public class Solution {\n"
                        + "    public boolean canFinish(int numCourses, int[][] prerequisites) {\n"
                        + "        return false;\n"
                        + "    }\n"
                        + "}",
                new String[][]{
                        tc("2, [[1,0]]", "true", false),
                        tc("2, [[1,0],[0,1]]", "false", false),
                        tc("1, []", "true", false),
                        tc("3, [[1,0],[2,1]]", "true", false),
                        tc("3, [[1,0],[0,2],[2,1]]", "false", false),
                        tc("4, [[1,0],[2,0],[3,1],[3,2]]", "true", true),
                        tc("2, []", "true", true),
                        tc("4, [[0,1],[1,2],[2,3],[3,0]]", "false", true),
                        tc("5, [[1,0],[2,1],[3,2],[4,3]]", "true", true),
                        tc("3, [[0,1],[1,2]]", "true", true),
                },
                new Object[][]{
                        {1, "Detect cycle in directed graph.", 1},
                        {2, "Use Kahn's topological sort (indegree array).", 1},
                },
                "Course Schedule: Kahn's Algorithm",
                "If topological sort visits all nodes, no cycle exists.",
                "1. Build adjacency list and indegrees.\n2. Queue courses with indegree 0.\n3. Count processed courses.",
                "Time: O(V + E), Space: O(V + E)",
                "```java\n"
                        + "public class Solution {\n"
                        + "    public boolean canFinish(int numCourses, int[][] prerequisites) {\n"
                        + "        int[] in = new int[numCourses];\n"
                        + "        java.util.List<Integer>[] adj = new java.util.ArrayList[numCourses];\n"
                        + "        for (int i = 0; i < numCourses; i++) adj[i] = new java.util.ArrayList<>();\n"
                        + "        for (int[] p : prerequisites) { adj[p[1]].add(p[0]); in[p[0]]++; }\n"
                        + "        java.util.Queue<Integer> q = new java.util.LinkedList<>();\n"
                        + "        for (int i = 0; i < numCourses; i++) if (in[i] == 0) q.offer(i);\n"
                        + "        int count = 0;\n"
                        + "        while (!q.isEmpty()) {\n"
                        + "            int cur = q.poll(); count++;\n"
                        + "            for (int nxt : adj[cur]) if (--in[nxt] == 0) q.offer(nxt);\n"
                        + "        }\n"
                        + "        return count == numCourses;\n"
                        + "    }\n"
                        + "}\n"
                        + "```",
                2
        });

        specs.add(new Object[]{
                "Word Search",
                "Given an m x n grid of characters board and a string word, return true if word exists in the grid.",
                Difficulty.MEDIUM, "Backtracking, Matrix",
                "1 <= m, n <= 6",
                "exist",
                "public class Solution {\n"
                        + "    public boolean exist(String[] board, String word) {\n"
                        + "        return false;\n"
                        + "    }\n"
                        + "}",
                new String[][]{
                        tc("[\"ABCE\",\"SFCS\",\"ADEE\"], ABCCED", "true", false),
                        tc("[\"ABCE\",\"SFCS\",\"ADEE\"], SEE", "true", false),
                        tc("[\"ABCE\",\"SFCS\",\"ADEE\"], ABCB", "false", false),
                        tc("[\"a\"], a", "true", false),
                        tc("[\"a\"], b", "false", false),
                        tc("[\"CAA\",\"AAA\",\"BCD\"], AAB", "true", true),
                        tc("[\"ABCE\",\"SFES\",\"ADEE\"], ABCESEEEFS", "true", true),
                        tc("[\"AB\",\"CD\"], ACDB", "true", true),
                        tc("[\"AB\",\"CD\"], ABCD", "false", true),
                        tc("[\"XYZ\"], XYZ", "true", true),
                },
                new Object[][]{
                        {1, "DFS from each matching starting character.", 1},
                        {2, "Temporarily mask visited cells to avoid revisiting.", 1},
                },
                "Word Search: Backtracking DFS",
                "Explore 4 directions matching each character.",
                "1. Iterate all cells.\n2. DFS match word[idx].\n3. Backtrack visited state.",
                "Time: O(M*N * 4^L), Space: O(L)",
                "```java\n"
                        + "public class Solution {\n"
                        + "    public boolean exist(String[] board, String word) {\n"
                        + "        char[][] b = new char[board.length][];\n"
                        + "        for (int i = 0; i < board.length; i++) b[i] = board[i].toCharArray();\n"
                        + "        for (int i = 0; i < b.length; i++) {\n"
                        + "            for (int j = 0; j < b[0].length; j++) if (dfs(b, i, j, word, 0)) return true;\n"
                        + "        }\n"
                        + "        return false;\n"
                        + "    }\n"
                        + "    private boolean dfs(char[][] b, int r, int c, String w, int idx) {\n"
                        + "        if (idx == w.length()) return true;\n"
                        + "        if (r < 0 || r >= b.length || c < 0 || c >= b[0].length || b[r][c] != w.charAt(idx)) return false;\n"
                        + "        char t = b[r][c]; b[r][c] = '#';\n"
                        + "        boolean res = dfs(b, r+1, c, w, idx+1) || dfs(b, r-1, c, w, idx+1) || dfs(b, r, c+1, w, idx+1) || dfs(b, r, c-1, w, idx+1);\n"
                        + "        b[r][c] = t;\n"
                        + "        return res;\n"
                        + "    }\n"
                        + "}\n"
                        + "```",
                2
        });

        specs.add(new Object[]{
                "Find Peak Element",
                "A peak element is an element that is strictly greater than its neighbors. Given a 0-indexed integer array nums, find a peak element, and return its index. You must write an algorithm that runs in O(log n) time.",
                Difficulty.MEDIUM, "Binary Search, Arrays",
                "1 <= nums.length <= 1000",
                "findPeakElement",
                "public class Solution {\n"
                        + "    public int findPeakElement(int[] nums) {\n"
                        + "        return 0;\n"
                        + "    }\n"
                        + "}",
                new String[][]{
                        tc("[1,2,3,1]", "2", false),
                        tc("[1,2,1,3,5,6,4]", "5", false),
                        tc("[1]", "0", false),
                        tc("[1,2]", "1", false),
                        tc("[2,1]", "0", false),
                        tc("[1,2,3]", "2", true),
                        tc("[3,2,1]", "0", true),
                        tc("[1,5,10,20,15,4]", "3", true),
                        tc("[1,3,2,1]", "1", true),
                        tc("[1,2,1]", "1", true),
                },
                new Object[][]{
                        {1, "Compare nums[mid] with nums[mid + 1].", 1},
                        {2, "Binary search towards the increasing slope.", 1},
                },
                "Find Peak Element: Binary Search",
                "Always move towards the side with the higher neighbor.",
                "1. l = 0, r = len - 1.\n2. if nums[m] < nums[m+1] l = m + 1; else r = m.",
                "Time: O(log n), Space: O(1)",
                "```java\n"
                        + "public class Solution {\n"
                        + "    public int findPeakElement(int[] nums) {\n"
                        + "        int l = 0, r = nums.length - 1;\n"
                        + "        while (l < r) {\n"
                        + "            int m = l + (r - l) / 2;\n"
                        + "            if (nums[m] < nums[m + 1]) l = m + 1; else r = m;\n"
                        + "        }\n"
                        + "        return l;\n"
                        + "    }\n"
                        + "}\n"
                        + "```",
                2
        });

        specs.add(new Object[]{
                "Partition Equal Subset Sum",
                "Given an integer array nums, return true if you can partition the array into two subsets such that the sum of the elements in both subsets is equal.",
                Difficulty.MEDIUM, "Dynamic Programming, Arrays",
                "1 <= nums.length <= 200",
                "canPartition",
                "public class Solution {\n"
                        + "    public boolean canPartition(int[] nums) {\n"
                        + "        return false;\n"
                        + "    }\n"
                        + "}",
                new String[][]{
                        tc("[1,5,11,5]", "true", false),
                        tc("[1,2,3,5]", "false", false),
                        tc("[2,2]", "true", false),
                        tc("[1,2,5]", "false", false),
                        tc("[1,1,2,2]", "true", false),
                        tc("[1,2,3,4,5,6,7]", "true", true),
                        tc("[100]", "false", true),
                        tc("[2,2,3,5]", "false", true),
                        tc("[14,9,8,4,3,2]", "true", true),
                        tc("[3,3,3,4,5]", "true", true),
                },
                new Object[][]{
                        {1, "Target is totalSum / 2.", 1},
                        {2, "Subset sum 0/1 knapsack.", 1},
                },
                "Partition Equal Subset Sum: 0/1 Knapsack DP",
                "Check if any subset sums to totalSum / 2.",
                "1. If totalSum % 2 != 0 return false.\n2. dp[j] |= dp[j - num].",
                "Time: O(n * sum), Space: O(sum)",
                "```java\n"
                        + "public class Solution {\n"
                        + "    public boolean canPartition(int[] nums) {\n"
                        + "        int sum = 0;\n"
                        + "        for (int x : nums) sum += x;\n"
                        + "        if (sum % 2 != 0) return false;\n"
                        + "        int target = sum / 2;\n"
                        + "        boolean[] dp = new boolean[target + 1];\n"
                        + "        dp[0] = true;\n"
                        + "        for (int x : nums) {\n"
                        + "            for (int j = target; j >= x; j--) dp[j] = dp[j] || dp[j - x];\n"
                        + "        }\n"
                        + "        return dp[target];\n"
                        + "    }\n"
                        + "}\n"
                        + "```",
                2
        });

        specs.add(new Object[]{
                "Target Sum",
                "You are given an integer array nums and an integer target. Build an expression by adding '+' or '-' before each integer. Return the number of different expressions that evaluate to target.",
                Difficulty.MEDIUM, "Dynamic Programming, Backtracking",
                "1 <= nums.length <= 20",
                "findTargetSumWays",
                "public class Solution {\n"
                        + "    public int findTargetSumWays(int[] nums, int target) {\n"
                        + "        return 0;\n"
                        + "    }\n"
                        + "}",
                new String[][]{
                        tc("[1,1,1,1,1], 3", "5", false),
                        tc("[1], 1", "1", false),
                        tc("[1], 2", "0", false),
                        tc("[1,0], 1", "2", false),
                        tc("[2,1], 1", "1", false),
                        tc("[0,0,0,0,0,0,0,0,1], 1", "256", true),
                        tc("[1,2,3], 2", "1", true),
                        tc("[2,2,2], 2", "3", true),
                        tc("[1,2,1], 0", "2", true),
                        tc("[5,5], 0", "2", true),
                },
                new Object[][]{
                        {1, "Transform into subset sum: P - N = target => 2P = target + totalSum.", 1},
                        {2, "Count subsets summing to (target + totalSum) / 2.", 1},
                },
                "Target Sum: Subset Sum Reduction",
                "P = (target + sum) / 2.",
                "1. If (target + sum) is odd or negative return 0.\n2. Standard 0/1 knapsack counting.",
                "Time: O(n * sum), Space: O(sum)",
                "```java\n"
                        + "public class Solution {\n"
                        + "    public int findTargetSumWays(int[] nums, int target) {\n"
                        + "        int sum = 0;\n"
                        + "        for (int x : nums) sum += x;\n"
                        + "        if ((sum + target) % 2 != 0 || sum < Math.abs(target)) return 0;\n"
                        + "        int s1 = (sum + target) / 2;\n"
                        + "        int[] dp = new int[s1 + 1];\n"
                        + "        dp[0] = 1;\n"
                        + "        for (int x : nums) {\n"
                        + "            for (int j = s1; j >= x; j--) dp[j] += dp[j - x];\n"
                        + "        }\n"
                        + "        return dp[s1];\n"
                        + "    }\n"
                        + "}\n"
                        + "```",
                2
        });

        specs.add(new Object[]{
                "Non-overlapping Intervals",
                "Given an array of intervals intervals where intervals[i] = [starti, endi], return the minimum number of intervals you need to remove to make the rest of the intervals non-overlapping.",
                Difficulty.MEDIUM, "Greedy, Arrays, Sorting",
                "1 <= intervals.length <= 10^5",
                "eraseOverlapIntervals",
                "public class Solution {\n"
                        + "    public int eraseOverlapIntervals(int[][] intervals) {\n"
                        + "        return 0;\n"
                        + "    }\n"
                        + "}",
                new String[][]{
                        tc("[[1,2],[2,3],[3,4],[1,3]]", "1", false),
                        tc("[[1,2],[1,2],[1,2]]", "2", false),
                        tc("[[1,2],[2,3]]", "0", false),
                        tc("[[1,100],[11,22],[1,11],[2,12]]", "2", false),
                        tc("[[1,5]]", "0", false),
                        tc("[[0,2],[1,3],[2,4],[3,5],[4,6]]", "2", true),
                        tc("[[1,2],[1,3],[1,4]]", "2", true),
                        tc("[[1,4],[2,3],[3,5]]", "1", true),
                        tc("[[1,10],[2,3],[3,4],[4,5]]", "1", true),
                        tc("[[1,2],[3,4],[5,6]]", "0", true),
                },
                new Object[][]{
                        {1, "Sort intervals by end time.", 1},
                        {2, "Greedily keep intervals that finish earliest.", 1},
                },
                "Non-overlapping Intervals: Interval Scheduling Greedy",
                "Sort by end time to maximize remaining non-overlapping intervals.",
                "1. Sort by interval[1].\n2. Track last end, count overlaps.",
                "Time: O(n log n), Space: O(1)",
                "```java\n"
                        + "public class Solution {\n"
                        + "    public int eraseOverlapIntervals(int[][] intervals) {\n"
                        + "        java.util.Arrays.sort(intervals, (a, b) -> Integer.compare(a[1], b[1]));\n"
                        + "        int count = 0, lastEnd = intervals[0][1];\n"
                        + "        for (int i = 1; i < intervals.length; i++) {\n"
                        + "            if (intervals[i][0] < lastEnd) count++; else lastEnd = intervals[i][1];\n"
                        + "        }\n"
                        + "        return count;\n"
                        + "    }\n"
                        + "}\n"
                        + "```",
                2
        });

        specs.add(new Object[]{
                "Min Stack Simulation",
                "Simulate a stack supporting push, pop, top, and retrieving the minimum element in constant time. Given operations formatted as integers (positive=push, 0=getMin), return the sum of all getMin values.",
                Difficulty.MEDIUM, "Stack, Design",
                "1 <= ops.length <= 10^4",
                "minStackSim",
                "public class Solution {\n"
                        + "    public int minStackSim(int[] ops) {\n"
                        + "        return 0;\n"
                        + "    }\n"
                        + "}",
                new String[][]{
                        tc("[5,3,0,7,0]", "6", false),
                        tc("[10,0]", "10", false),
                        tc("[8,4,2,0]", "2", false),
                        tc("[1,0,2,0,3,0]", "3", false),
                        tc("[5,5,0]", "5", false),
                        tc("[100,50,25,0,0]", "50", true),
                        tc("[7,7,7,0]", "7", true),
                        tc("[9,4,0,1,0]", "5", true),
                        tc("[12,0,8,0]", "20", true),
                        tc("[3,1,0,4,0]", "2", true),
                },
                new Object[][]{
                        {1, "Track running minimum with each element.", 1},
                        {2, "Use two stacks or pair.", 1},
                },
                "Min Stack Simulation: Two Stacks",
                "Track min at each stack level.",
                "1. stack and minStack.\n2. Accumulate getMin calls.",
                "Time: O(n), Space: O(n)",
                "```java\n"
                        + "public class Solution {\n"
                        + "    public int minStackSim(int[] ops) {\n"
                        + "        java.util.Deque<Integer> st = new java.util.ArrayDeque<>(), minSt = new java.util.ArrayDeque<>();\n"
                        + "        int sum = 0;\n"
                        + "        for (int op : ops) {\n"
                        + "            if (op == 0) sum += minSt.isEmpty() ? 0 : minSt.peek();\n"
                        + "            else { st.push(op); minSt.push(minSt.isEmpty() ? op : Math.min(op, minSt.peek())); }\n"
                        + "        }\n"
                        + "        return sum;\n"
                        + "    }\n"
                        + "}\n"
                        + "```",
                2
        });

        specs.add(new Object[]{
                "Maximum Subarray Sum with One Deletion",
                "Given an array of integers, return the maximum sum for a non-empty subarray with at most one element deletion.",
                Difficulty.MEDIUM, "Dynamic Programming, Arrays",
                "1 <= arr.length <= 10^5",
                "maximumSum",
                "public class Solution {\n"
                        + "    public int maximumSum(int[] arr) {\n"
                        + "        return 0;\n"
                        + "    }\n"
                        + "}",
                new String[][]{
                        tc("[1,-2,0,3]", "4", false),
                        tc("[1,-2,-2,3]", "3", false),
                        tc("[-1,-1,-1,-1]", "-1", false),
                        tc("[1,2,3]", "6", false),
                        tc("[2,-1,2]", "4", false),
                        tc("[5,-2,5]", "10", true),
                        tc("[-50]", "-50", true),
                        tc("[2,1,-2,3]", "6", true),
                        tc("[3,-1,0,-2,3]", "5", true),
                        tc("[1,-4,3,2]", "5", true),
                },
                new Object[][]{
                        {1, "dp[i][0] = max sum ending at i without deletion.", 1},
                        {2, "dp[i][1] = max sum ending at i with one deletion.", 1},
                },
                "Max Subarray with Deletion: Two State DP",
                "State 0: no deletion. State 1: one deletion.",
                "1. noDel = arr[0], oneDel = 0, res = arr[0].\n2. Update both states.",
                "Time: O(n), Space: O(1)",
                "```java\n"
                        + "public class Solution {\n"
                        + "    public int maximumSum(int[] arr) {\n"
                        + "        int noDel = arr[0], oneDel = 0, res = arr[0];\n"
                        + "        for (int i = 1; i < arr.length; i++) {\n"
                        + "            oneDel = Math.max(oneDel + arr[i], noDel);\n"
                        + "            noDel = Math.max(arr[i], noDel + arr[i]);\n"
                        + "            res = Math.max(res, Math.max(noDel, oneDel));\n"
                        + "        }\n"
                        + "        return res;\n"
                        + "    }\n"
                        + "}\n"
                        + "```",
                2
        });

        specs.add(new Object[]{
                "Remove Nth Node From End of List",
                "Given an array representing a linked list and an integer n, remove the nth node from the end of the list and return the resulting array.",
                Difficulty.MEDIUM, "Linked List, Two Pointers",
                "1 <= n <= nums.length <= 30",
                "removeNthFromEnd",
                "public class Solution {\n"
                        + "    public int[] removeNthFromEnd(int[] nums, int n) {\n"
                        + "        return new int[0];\n"
                        + "    }\n"
                        + "}",
                new String[][]{
                        tc("[1,2,3,4,5], 2", "[1,2,3,5]", false),
                        tc("[1], 1", "[]", false),
                        tc("[1,2], 1", "[1]", false),
                        tc("[1,2], 2", "[2]", false),
                        tc("[1,2,3], 3", "[2,3]", false),
                        tc("[1,2,3,4], 4", "[2,3,4]", true),
                        tc("[1,2,3,4,5], 1", "[1,2,3,4]", true),
                        tc("[1,2,3,4,5], 5", "[2,3,4,5]", true),
                        tc("[10,20,30], 2", "[10,30]", true),
                        tc("[5,6], 1", "[5]", true),
                },
                new Object[][]{
                        {1, "Identify index to remove: length - n.", 1},
                        {2, "Copy all elements except that index.", 1},
                },
                "Remove Nth Node: Array Translation",
                "Remove element at index (len - n).",
                "1. target = nums.length - n.\n2. Copy rest.",
                "Time: O(len), Space: O(len)",
                "```java\n"
                        + "public class Solution {\n"
                        + "    public int[] removeNthFromEnd(int[] nums, int n) {\n"
                        + "        int target = nums.length - n;\n"
                        + "        int[] res = new int[nums.length - 1];\n"
                        + "        int k = 0;\n"
                        + "        for (int i = 0; i < nums.length; i++) if (i != target) res[k++] = nums[i];\n"
                        + "        return res;\n"
                        + "    }\n"
                        + "}\n"
                        + "```",
                2
        });

        specs.add(new Object[]{
                "Reorder List",
                "Given an array representing a list L0 -> L1 -> ... -> Ln - 1 -> Ln, reorder it to: L0 -> Ln -> L1 -> Ln - 1 -> L2 -> Ln - 2 -> ...",
                Difficulty.MEDIUM, "Linked List, Two Pointers",
                "1 <= nums.length <= 5 * 10^4",
                "reorderList",
                "public class Solution {\n"
                        + "    public int[] reorderList(int[] nums) {\n"
                        + "        return nums;\n"
                        + "    }\n"
                        + "}",
                new String[][]{
                        tc("[1,2,3,4]", "[1,4,2,3]", false),
                        tc("[1,2,3,4,5]", "[1,5,2,4,3]", false),
                        tc("[1]", "[1]", false),
                        tc("[1,2]", "[1,2]", false),
                        tc("[1,2,3]", "[1,3,2]", false),
                        tc("[1,2,3,4,5,6]", "[1,6,2,5,3,4]", true),
                        tc("[10,20,30,40]", "[10,40,20,30]", true),
                        tc("[2,4,6,8,10]", "[2,10,4,8,6]", true),
                        tc("[1,1,1,1]", "[1,1,1,1]", true),
                        tc("[5,4,3,2,1]", "[5,1,4,2,3]", true),
                },
                new Object[][]{
                        {1, "Take elements alternatively from start and end.", 1},
                        {2, "Two pointers l and r.", 1},
                },
                "Reorder List: Alternating Two Pointers",
                "Interleave from front and back.",
                "1. l = 0, r = len - 1.\n2. Pick l then r alternately.",
                "Time: O(n), Space: O(n)",
                "```java\n"
                        + "public class Solution {\n"
                        + "    public int[] reorderList(int[] nums) {\n"
                        + "        int[] res = new int[nums.length];\n"
                        + "        int l = 0, r = nums.length - 1, k = 0;\n"
                        + "        while (l <= r) {\n"
                        + "            res[k++] = nums[l++];\n"
                        + "            if (l <= r) res[k++] = nums[r--];\n"
                        + "        }\n"
                        + "        return res;\n"
                        + "    }\n"
                        + "}\n"
                        + "```",
                2
        });

        specs.add(new Object[]{
                "Validate Binary Search Tree",
                "Given an array of values sorted in inorder traversal of a binary tree, return true if it is a valid BST (strictly increasing).",
                Difficulty.MEDIUM, "Tree, Binary Search Tree",
                "1 <= tree.length <= 10^4",
                "isValidBST",
                "public class Solution {\n"
                        + "    public boolean isValidBST(int[] tree) {\n"
                        + "        return false;\n"
                        + "    }\n"
                        + "}",
                new String[][]{
                        tc("[1,2,3]", "true", false),
                        tc("[1,1,2]", "false", false),
                        tc("[2,1,3]", "false", false),
                        tc("[1]", "true", false),
                        tc("[10,20,30,40]", "true", false),
                        tc("[5,4,6]", "false", true),
                        tc("[1,3,5,7,9]", "true", true),
                        tc("[2,2]", "false", true),
                        tc("[10,15,12]", "false", true),
                        tc("[3,4,5,6]", "true", true),
                },
                new Object[][]{
                        {1, "Inorder traversal of a valid BST is strictly increasing.", 1},
                        {2, "Check tree[i] < tree[i+1].", 1},
                },
                "Validate BST: Inorder Strict Increase",
                "A valid BST must have strictly increasing inorder sequence.",
                "1. For each i: if tree[i] >= tree[i+1] return false.",
                "Time: O(n), Space: O(1)",
                "```java\n"
                        + "public class Solution {\n"
                        + "    public boolean isValidBST(int[] tree) {\n"
                        + "        for (int i = 0; i < tree.length - 1; i++) if (tree[i] >= tree[i + 1]) return false;\n"
                        + "        return true;\n"
                        + "    }\n"
                        + "}\n"
                        + "```",
                2
        });

        specs.add(new Object[]{
                "Kth Smallest Element in a BST",
                "Given an array representing the inorder traversal of a binary search tree, return the kth smallest element (1-indexed).",
                Difficulty.MEDIUM, "Tree, Binary Search Tree",
                "1 <= k <= tree.length <= 10^4",
                "kthSmallest",
                "public class Solution {\n"
                        + "    public int kthSmallest(int[] tree, int k) {\n"
                        + "        return 0;\n"
                        + "    }\n"
                        + "}",
                new String[][]{
                        tc("[1,2,3,4,5], 1", "1", false),
                        tc("[1,2,3,4,5], 3", "3", false),
                        tc("[3,5,7,9], 2", "5", false),
                        tc("[10], 1", "10", false),
                        tc("[2,4,6,8,10], 5", "10", false),
                        tc("[1,2,3], 2", "2", true),
                        tc("[10,20,30,40,50], 4", "40", true),
                        tc("[5,10,15], 1", "5", true),
                        tc("[7,8,9], 3", "9", true),
                        tc("[2,4], 1", "2", true),
                },
                new Object[][]{
                        {1, "Inorder traversal is sorted.", 1},
                        {2, "Kth smallest is at index k - 1.", 1},
                },
                "Kth Smallest in BST: Inorder Index",
                "kth smallest is at index k - 1 of inorder.",
                "1. return tree[k - 1];",
                "Time: O(1), Space: O(1)",
                "```java\n"
                        + "public class Solution {\n"
                        + "    public int kthSmallest(int[] tree, int k) {\n"
                        + "        return tree[k - 1];\n"
                        + "    }\n"
                        + "}\n"
                        + "```",
                2
        });

        specs.add(new Object[]{
                "Lowest Common Ancestor of a BST",
                "Given values in a BST, find the lowest common ancestor of two given values p and q.",
                Difficulty.MEDIUM, "Tree, Binary Search Tree",
                "2 <= nodes <= 10^5",
                "lowestCommonAncestor",
                "public class Solution {\n"
                        + "    public int lowestCommonAncestor(int root, int p, int q) {\n"
                        + "        return 0;\n"
                        + "    }\n"
                        + "}",
                new String[][]{
                        tc("6, 2, 8", "6", false),
                        tc("6, 2, 4", "6", false),
                        tc("2, 1, 3", "2", false),
                        tc("5, 3, 6", "5", false),
                        tc("10, 5, 15", "10", false),
                        tc("20, 10, 30", "20", true),
                        tc("8, 4, 12", "8", true),
                        tc("50, 25, 75", "50", true),
                        tc("4, 2, 6", "4", true),
                        tc("15, 10, 20", "15", true),
                },
                new Object[][]{
                        {1, "If p and q lie on opposite sides of root, root is LCA.", 1},
                        {2, "Return root if min(p,q) <= root <= max(p,q).", 1},
                },
                "Lowest Common Ancestor in BST: Value Comparison",
                "Split point in BST is the lowest common ancestor.",
                "1. If root lies between p and q, it is LCA.",
                "Time: O(1), Space: O(1)",
                "```java\n"
                        + "public class Solution {\n"
                        + "    public int lowestCommonAncestor(int root, int p, int q) {\n"
                        + "        return root;\n"
                        + "    }\n"
                        + "}\n"
                        + "```",
                2
        });

        specs.add(new Object[]{
                "Binary Tree Level Order Traversal",
                "Given a level-order array representation of a binary tree, return the count of levels in the tree.",
                Difficulty.MEDIUM, "Tree, Breadth-First Search",
                "0 <= tree.length <= 2000",
                "levelOrderCount",
                "public class Solution {\n"
                        + "    public int levelOrderCount(int[] tree) {\n"
                        + "        return 0;\n"
                        + "    }\n"
                        + "}",
                new String[][]{
                        tc("[3,9,20,-1,-1,15,7]", "3", false),
                        tc("[1]", "1", false),
                        tc("[]", "0", false),
                        tc("[1,2,3]", "2", false),
                        tc("[1,2,3,4,5,6,7]", "3", false),
                        tc("[1,2,3,4,5,6,7,8]", "4", true),
                        tc("[10,20]", "2", true),
                        tc("[5]", "1", true),
                        tc("[1,2,3,4]", "3", true),
                        tc("[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15]", "4", true),
                },
                new Object[][]{
                        {1, "Number of levels in a tree of size n is floor(log2(n)) + 1.", 1},
                        {2, "Use integer log.", 1},
                },
                "Level Order Count: Log2 of Nodes",
                "Compute number of levels in complete tree.",
                "1. If tree empty return 0.\n2. return (int)(Math.log(len)/Math.log(2)) + 1.",
                "Time: O(1), Space: O(1)",
                "```java\n"
                        + "public class Solution {\n"
                        + "    public int levelOrderCount(int[] tree) {\n"
                        + "        if (tree.length == 0) return 0;\n"
                        + "        return (int)(Math.log(tree.length) / Math.log(2)) + 1;\n"
                        + "    }\n"
                        + "}\n"
                        + "```",
                2
        });

        specs.add(new Object[]{
                "Construct Tree from Preorder and Inorder",
                "Given two integer arrays preorder and inorder, return the root value of the constructed tree.",
                Difficulty.MEDIUM, "Tree, Divide and Conquer",
                "1 <= preorder.length <= 3000",
                "buildTreeRoot",
                "public class Solution {\n"
                        + "    public int buildTreeRoot(int[] preorder, int[] inorder) {\n"
                        + "        return 0;\n"
                        + "    }\n"
                        + "}",
                new String[][]{
                        tc("[3,9,20,15,7], [9,3,15,20,7]", "3", false),
                        tc("[-1], [-1]", "-1", false),
                        tc("[1,2], [2,1]", "1", false),
                        tc("[1,2,3], [2,1,3]", "1", false),
                        tc("[5,3,8], [3,5,8]", "5", false),
                        tc("[10,5,15], [5,10,15]", "10", true),
                        tc("[4,2,5,1,3], [2,4,5,1,3]", "4", true),
                        tc("[2,1,3], [1,2,3]", "2", true),
                        tc("[7,3,9], [3,7,9]", "7", true),
                        tc("[8], [8]", "8", true),
                },
                new Object[][]{
                        {1, "The first element of preorder is always the root.", 1},
                        {2, "Return preorder[0].", 1},
                },
                "Construct Tree: Preorder Root Identification",
                "Preorder traversal visits root first.",
                "1. return preorder[0];",
                "Time: O(1), Space: O(1)",
                "```java\n"
                        + "public class Solution {\n"
                        + "    public int buildTreeRoot(int[] preorder, int[] inorder) {\n"
                        + "        return preorder[0];\n"
                        + "    }\n"
                        + "}\n"
                        + "```",
                2
        });

        specs.add(new Object[]{
                "Second Highest Salary",
                "Find the second highest salary from the Employee table. If there is no second highest salary, return null/empty. Given array of salaries, return second highest distinct value.",
                Difficulty.MEDIUM, "SQL, Database",
                "1 <= salaries.length <= 100",
                "secondHighestSalary",
                "public class Solution {\n"
                        + "    public int secondHighestSalary(int[] salaries) {\n"
                        + "        return -1;\n"
                        + "    }\n"
                        + "}",
                new String[][]{
                        tc("[100,200,300]", "200", false),
                        tc("[100]", "-1", false),
                        tc("[100,100]", "-1", false),
                        tc("[500,300,200,400]", "400", false),
                        tc("[10,20]", "10", false),
                        tc("[50,50,40]", "40", true),
                        tc("[1000,2000,1500]", "1500", true),
                        tc("[70,80,90,100]", "90", true),
                        tc("[250,250,250]", "-1", true),
                        tc("[5,10,15,20]", "15", true),
                },
                new Object[][]{
                        {1, "SELECT MAX(salary) FROM Employee WHERE salary < (SELECT MAX(salary) FROM Employee)", 1},
                        {2, "Find distinct max and second max.", 1},
                },
                "Second Highest Salary: Subquery / Scan",
                "Find max strictly less than overall max.",
                "1. Track max1 and max2.\n2. Return max2 or -1.",
                "Time: O(n), Space: O(1)",
                "```java\n"
                        + "public class Solution {\n"
                        + "    public int secondHighestSalary(int[] salaries) {\n"
                        + "        int m1 = -1, m2 = -1;\n"
                        + "        for (int s : salaries) {\n"
                        + "            if (s > m1) { m2 = m1; m1 = s; } else if (s > m2 && s < m1) { m2 = s; }\n"
                        + "        }\n"
                        + "        return m2;\n"
                        + "    }\n"
                        + "}\n"
                        + "```",
                2
        });

        return specs;
    }
}
