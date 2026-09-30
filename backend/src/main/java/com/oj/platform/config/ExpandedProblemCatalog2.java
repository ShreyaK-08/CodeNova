package com.oj.platform.config;

import com.oj.platform.entity.Difficulty;

import java.util.ArrayList;
import java.util.List;

/**
 * ExpandedProblemCatalog2 - Curated algorithmic and system coding problems.
 */
public class ExpandedProblemCatalog2 {

    private static String[] tc(String input, String expected, boolean hidden) {
        return new String[]{input, expected, hidden ? "true" : "false"};
    }

    public static List<Object[]> getSpecs() {
        List<Object[]> specs = new ArrayList<>();

        specs.add(new Object[]{
                "Department Highest Salary",
                "Find employees who have the highest salary in each of the departments. Given array of employee salaries in a department, return the maximum salary.",
                Difficulty.MEDIUM, "SQL, Database",
                "1 <= salaries.length <= 100",
                "departmentHighestSalary",
                "public class Solution {\n"
                        + "    public int departmentHighestSalary(int[] salaries) {\n"
                        + "        return 0;\n"
                        + "    }\n"
                        + "}",
                new String[][]{
                        tc("[80000,90000,60000]", "90000", false),
                        tc("[70000,70000]", "70000", false),
                        tc("[50000]", "50000", false),
                        tc("[100000,120000,110000]", "120000", false),
                        tc("[45000,55000,65000]", "65000", false),
                        tc("[95000,85000,75000]", "95000", true),
                        tc("[150000,200000]", "200000", true),
                        tc("[60000,80000,80000]", "80000", true),
                        tc("[30000,40000,35000]", "40000", true),
                        tc("[10000,20000,30000,40000]", "40000", true),
                },
                new Object[][]{
                        {1, "SELECT MAX(salary) FROM Employee GROUP BY departmentId", 1},
                        {2, "Return maximum in array.", 1},
                },
                "Department Highest Salary: Group Max",
                "Compute maximum salary per department.",
                "1. Track max salary.",
                "Time: O(n), Space: O(1)",
                "```java\n"
                        + "public class Solution {\n"
                        + "    public int departmentHighestSalary(int[] salaries) {\n"
                        + "        int max = 0;\n"
                        + "        for (int s : salaries) if (s > max) max = s;\n"
                        + "        return max;\n"
                        + "    }\n"
                        + "}\n"
                        + "```",
                2
        });

        specs.add(new Object[]{
                "Consecutive Numbers",
                "Find all numbers that appear at least three times consecutively in a table. Given array of numbers, return the number appearing 3 times consecutively or -1.",
                Difficulty.MEDIUM, "SQL, Database",
                "1 <= nums.length <= 100",
                "consecutiveNumbers",
                "public class Solution {\n"
                        + "    public int consecutiveNumbers(int[] nums) {\n"
                        + "        return -1;\n"
                        + "    }\n"
                        + "}",
                new String[][]{
                        tc("[1,1,1,2,1,2,2]", "1", false),
                        tc("[1,2,3,4]", "-1", false),
                        tc("[2,2,2]", "2", false),
                        tc("[3,3,1,3,3,3]", "3", false),
                        tc("[0,0,0,1]", "0", false),
                        tc("[5,5,5,5]", "5", true),
                        tc("[1,2,2,2,3]", "2", true),
                        tc("[9,8,8,8,7]", "8", true),
                        tc("[4,4,1,4,4,4]", "4", true),
                        tc("[7,7,7]", "7", true),
                },
                new Object[][]{
                        {1, "Check nums[i] == nums[i+1] && nums[i] == nums[i+2]", 1},
                        {2, "One pass scanning 3 consecutive elements.", 1},
                },
                "Consecutive Numbers: 3-Window Scan",
                "Check equality of 3 consecutive elements.",
                "1. For i from 0 to len - 3: if nums[i] == nums[i+1] == nums[i+2] return nums[i].",
                "Time: O(n), Space: O(1)",
                "```java\n"
                        + "public class Solution {\n"
                        + "    public int consecutiveNumbers(int[] nums) {\n"
                        + "        for (int i = 0; i < nums.length - 2; i++) {\n"
                        + "            if (nums[i] == nums[i + 1] && nums[i] == nums[i + 2]) return nums[i];\n"
                        + "        }\n"
                        + "        return -1;\n"
                        + "    }\n"
                        + "}\n"
                        + "```",
                2
        });

        specs.add(new Object[]{
                "Trapping Rain Water",
                "Given n non-negative integers representing an elevation map where the width of each bar is 1, compute how much water it can trap after raining.",
                Difficulty.HARD, "Two Pointers, Dynamic Programming, Stack",
                "n == height.length\n1 <= n <= 2 * 10^4",
                "trap",
                "public class Solution {\n"
                        + "    public int trap(int[] height) {\n"
                        + "        return 0;\n"
                        + "    }\n"
                        + "}",
                new String[][]{
                        tc("[0,1,0,2,1,0,1,3,2,1,2,1]", "6", false),
                        tc("[4,2,0,3,2,5]", "9", false),
                        tc("[1]", "0", false),
                        tc("[2,0,2]", "2", false),
                        tc("[3,0,0,2,0,4]", "10", false),
                        tc("[5,4,1,2]", "1", true),
                        tc("[0,2,0]", "0", true),
                        tc("[1,2,3,4,5]", "0", true),
                        tc("[5,4,3,2,1]", "0", true),
                        tc("[3,2,1,2,3]", "4", true),
                },
                new Object[][]{
                        {1, "Two pointers: l = 0, r = len - 1.", 1},
                        {2, "Water trapped depends on min(maxLeft, maxRight).", 1},
                },
                "Trapping Rain Water: Two Pointers",
                "Move pointers inward while tracking running maximum heights.",
                "1. l = 0, r = n - 1, leftMax = 0, rightMax = 0.\n2. Add water = max - height.",
                "Time: O(n), Space: O(1)",
                "```java\n"
                        + "public class Solution {\n"
                        + "    public int trap(int[] height) {\n"
                        + "        int l = 0, r = height.length - 1, lMax = 0, rMax = 0, ans = 0;\n"
                        + "        while (l < r) {\n"
                        + "            if (height[l] < height[r]) {\n"
                        + "                if (height[l] >= lMax) lMax = height[l]; else ans += lMax - height[l];\n"
                        + "                l++;\n"
                        + "            } else {\n"
                        + "                if (height[r] >= rMax) rMax = height[r]; else ans += rMax - height[r];\n"
                        + "                r--;\n"
                        + "            }\n"
                        + "        }\n"
                        + "        return ans;\n"
                        + "    }\n"
                        + "}\n"
                        + "```",
                2
        });

        specs.add(new Object[]{
                "Median of Two Sorted Arrays",
                "Given two sorted arrays nums1 and nums2 of size m and n respectively, return the median of the two sorted arrays. The overall run time complexity should be O(log (m+n)).",
                Difficulty.HARD, "Binary Search, Divide and Conquer",
                "nums1.length, nums2.length <= 1000",
                "findMedianSortedArrays",
                "public class Solution {\n"
                        + "    public double findMedianSortedArrays(int[] nums1, int[] nums2) {\n"
                        + "        return 0.0;\n"
                        + "    }\n"
                        + "}",
                new String[][]{
                        tc("[1,3], [2]", "2.0", false),
                        tc("[1,2], [3,4]", "2.5", false),
                        tc("[0,0], [0,0]", "0.0", false),
                        tc("[], [1]", "1.0", false),
                        tc("[2], []", "2.0", false),
                        tc("[1,3,5], [2,4,6]", "3.5", true),
                        tc("[1,2], [1,2,3]", "2.0", true),
                        tc("[100], [1000]", "550.0", true),
                        tc("[1,4,7], [2,3,5,6]", "4.0", true),
                        tc("[1,2,3,4,5], [6,7,8,9,10]", "5.5", true),
                },
                new Object[][]{
                        {1, "Binary search on the smaller array.", 1},
                        {2, "Partition both arrays such that left half has equal elements to right half.", 1},
                },
                "Median of Two Sorted Arrays: Binary Search Partition",
                "Binary search partition point in smaller array.",
                "1. Ensure nums1 is smaller array.\n2. Binary search partition.",
                "Time: O(log(min(M,N))), Space: O(1)",
                "```java\n"
                        + "public class Solution {\n"
                        + "    public double findMedianSortedArrays(int[] nums1, int[] nums2) {\n"
                        + "        if (nums1.length > nums2.length) return findMedianSortedArrays(nums2, nums1);\n"
                        + "        int m = nums1.length, n = nums2.length, l = 0, r = m;\n"
                        + "        while (l <= r) {\n"
                        + "            int p1 = (l + r) / 2, p2 = (m + n + 1) / 2 - p1;\n"
                        + "            int maxL1 = p1 == 0 ? Integer.MIN_VALUE : nums1[p1 - 1];\n"
                        + "            int minR1 = p1 == m ? Integer.MAX_VALUE : nums1[p1];\n"
                        + "            int maxL2 = p2 == 0 ? Integer.MIN_VALUE : nums2[p2 - 1];\n"
                        + "            int minR2 = p2 == n ? Integer.MAX_VALUE : nums2[p2];\n"
                        + "            if (maxL1 <= minR2 && maxL2 <= minR1) {\n"
                        + "                if ((m + n) % 2 == 0) return (Math.max(maxL1, maxL2) + Math.min(minR1, minR2)) / 2.0;\n"
                        + "                else return Math.max(maxL1, maxL2);\n"
                        + "            } else if (maxL1 > minR2) r = p1 - 1;\n"
                        + "            else l = p1 + 1;\n"
                        + "        }\n"
                        + "        return 0.0;\n"
                        + "    }\n"
                        + "}\n"
                        + "```",
                2
        });

        specs.add(new Object[]{
                "Sliding Window Maximum",
                "You are given an array of integers nums, there is a sliding window of size k which is moving from the very left of the array to the very right. Return the max sliding window.",
                Difficulty.HARD, "Sliding Window, Monotonic Queue",
                "1 <= nums.length <= 10^5\n1 <= k <= nums.length",
                "maxSlidingWindow",
                "public class Solution {\n"
                        + "    public int[] maxSlidingWindow(int[] nums, int k) {\n"
                        + "        return new int[0];\n"
                        + "    }\n"
                        + "}",
                new String[][]{
                        tc("[1,3,-1,-3,5,3,6,7], 3", "[3,3,5,5,6,7]", false),
                        tc("[1], 1", "[1]", false),
                        tc("[1,-1], 1", "[1,-1]", false),
                        tc("[9,11], 2", "[11]", false),
                        tc("[4,-2], 2", "[4]", false),
                        tc("[7,2,4], 2", "[7,4]", true),
                        tc("[1,3,1,2,0,5], 3", "[3,3,2,5]", true),
                        tc("[10,9,8,7,6], 2", "[10,9,8,7]", true),
                        tc("[1,2,3,4,5], 3", "[3,4,5]", true),
                        tc("[5,3,4,2,1], 3", "[5,4,4]", true),
                },
                new Object[][]{
                        {1, "Use a monotonic deque storing indices in decreasing value order.", 1},
                        {2, "Remove elements outside current window.", 1},
                },
                "Sliding Window Maximum: Monotonic Deque",
                "Maintain indices of potential maximums in a decreasing deque.",
                "1. Deque stores indices.\n2. Maintain decreasing order.\n3. Add front to result.",
                "Time: O(n), Space: O(k)",
                "```java\n"
                        + "public class Solution {\n"
                        + "    public int[] maxSlidingWindow(int[] nums, int k) {\n"
                        + "        int n = nums.length;\n"
                        + "        int[] ans = new int[n - k + 1];\n"
                        + "        java.util.Deque<Integer> q = new java.util.ArrayDeque<>();\n"
                        + "        for (int i = 0; i < n; i++) {\n"
                        + "            while (!q.isEmpty() && q.peekFirst() < i - k + 1) q.pollFirst();\n"
                        + "            while (!q.isEmpty() && nums[q.peekLast()] < nums[i]) q.pollLast();\n"
                        + "            q.offerLast(i);\n"
                        + "            if (i >= k - 1) ans[i - k + 1] = nums[q.peekFirst()];\n"
                        + "        }\n"
                        + "        return ans;\n"
                        + "    }\n"
                        + "}\n"
                        + "```",
                2
        });

        specs.add(new Object[]{
                "Edit Distance",
                "Given two strings word1 and word2, return the minimum number of operations required to convert word1 to word2 (insert, delete, replace).",
                Difficulty.HARD, "String, Dynamic Programming",
                "0 <= word1.length, word2.length <= 500",
                "minDistance",
                "public class Solution {\n"
                        + "    public int minDistance(String word1, String word2) {\n"
                        + "        return 0;\n"
                        + "    }\n"
                        + "}",
                new String[][]{
                        tc("horse, ros", "3", false),
                        tc("intention, execution", "5", false),
                        tc("a, a", "0", false),
                        tc(" , a", "1", false),
                        tc("a,  ", "1", false),
                        tc("kitten, sitting", "3", true),
                        tc("flaw, lawn", "2", true),
                        tc("zoologico, zoologia", "2", true),
                        tc("distance, edit", "6", true),
                        tc("ab, bc", "2", true),
                },
                new Object[][]{
                        {1, "dp[i][j] represents edits to convert word1[0..i] to word2[0..j].", 1},
                        {2, "If chars match: dp[i-1][j-1]; else 1 + min(insert, delete, replace).", 1},
                },
                "Edit Distance: Dynamic Programming",
                "Levenshtein distance matrix recurrence.",
                "1. dp table of size (m+1) x (n+1).\n2. Fill using recurrence.",
                "Time: O(m*n), Space: O(m*n)",
                "```java\n"
                        + "public class Solution {\n"
                        + "    public int minDistance(String word1, String word2) {\n"
                        + "        int m = word1.length(), n = word2.length();\n"
                        + "        int[][] dp = new int[m + 1][n + 1];\n"
                        + "        for (int i = 0; i <= m; i++) dp[i][0] = i;\n"
                        + "        for (int j = 0; j <= n; j++) dp[0][j] = j;\n"
                        + "        for (int i = 1; i <= m; i++) {\n"
                        + "            for (int j = 1; j <= n; j++) {\n"
                        + "                if (word1.charAt(i - 1) == word2.charAt(j - 1)) dp[i][j] = dp[i - 1][j - 1];\n"
                        + "                else dp[i][j] = 1 + Math.min(dp[i - 1][j - 1], Math.min(dp[i - 1][j], dp[i][j - 1]));\n"
                        + "            }\n"
                        + "        }\n"
                        + "        return dp[m][n];\n"
                        + "    }\n"
                        + "}\n"
                        + "```",
                2
        });

        specs.add(new Object[]{
                "Largest Rectangle in Histogram",
                "Given an array of integers heights representing the histogram's bar height where the width of each bar is 1, return the area of the largest rectangle in the histogram.",
                Difficulty.HARD, "Stack, Monotonic Stack, Arrays",
                "1 <= heights.length <= 10^5",
                "largestRectangleArea",
                "public class Solution {\n"
                        + "    public int largestRectangleArea(int[] heights) {\n"
                        + "        return 0;\n"
                        + "    }\n"
                        + "}",
                new String[][]{
                        tc("[2,1,5,6,2,3]", "10", false),
                        tc("[2,4]", "4", false),
                        tc("[1]", "1", false),
                        tc("[2,1,2]", "3", false),
                        tc("[5,5,5,5]", "20", false),
                        tc("[1,2,3,4,5]", "9", true),
                        tc("[5,4,3,2,1]", "9", true),
                        tc("[6,2,5,4,5,1,6]", "12", true),
                        tc("[3,6,5,7,4,8,1,0]", "20", true),
                        tc("[2,1,4,5,1,3,3]", "8", true),
                },
                new Object[][]{
                        {1, "Use a monotonic increasing stack of indices.", 1},
                        {2, "When current bar is lower, calculate max rectangle with popped bar as smallest height.", 1},
                },
                "Largest Rectangle in Histogram: Monotonic Stack",
                "Find left and right limits for each bar as minimum height.",
                "1. Stack stores indices.\n2. When height decreases, pop and calculate area.",
                "Time: O(n), Space: O(n)",
                "```java\n"
                        + "public class Solution {\n"
                        + "    public int largestRectangleArea(int[] heights) {\n"
                        + "        java.util.Deque<Integer> st = new java.util.ArrayDeque<>();\n"
                        + "        int maxArea = 0, n = heights.length;\n"
                        + "        for (int i = 0; i <= n; i++) {\n"
                        + "            int h = (i == n) ? 0 : heights[i];\n"
                        + "            while (!st.isEmpty() && h < heights[st.peek()]) {\n"
                        + "                int height = heights[st.pop()];\n"
                        + "                int width = st.isEmpty() ? i : i - st.peek() - 1;\n"
                        + "                maxArea = Math.max(maxArea, height * width);\n"
                        + "            }\n"
                        + "            st.push(i);\n"
                        + "        }\n"
                        + "        return maxArea;\n"
                        + "    }\n"
                        + "}\n"
                        + "```",
                2
        });

        specs.add(new Object[]{
                "First Missing Positive",
                "Given an unsorted integer array nums. Return the smallest positive integer that is not present in nums. Must run in O(n) time and O(1) auxiliary space.",
                Difficulty.HARD, "Arrays, Hash Table",
                "1 <= nums.length <= 10^5",
                "firstMissingPositive",
                "public class Solution {\n"
                        + "    public int firstMissingPositive(int[] nums) {\n"
                        + "        return 1;\n"
                        + "    }\n"
                        + "}",
                new String[][]{
                        tc("[1,2,0]", "3", false),
                        tc("[3,4,-1,1]", "2", false),
                        tc("[7,8,9,11,12]", "1", false),
                        tc("[1]", "2", false),
                        tc("[2]", "1", false),
                        tc("[1,2,3]", "4", true),
                        tc("[-1,-2,-3]", "1", true),
                        tc("[2,2,2]", "1", true),
                        tc("[10,1,2,3]", "4", true),
                        tc("[2,1,0]", "3", true),
                },
                new Object[][]{
                        {1, "Place each number x in index x-1 if 1 <= x <= n.", 1},
                        {2, "First index where nums[i] != i+1 is the answer.", 1},
                },
                "First Missing Positive: Cyclic Sort",
                "Place each number at index num - 1.",
                "1. Swap numbers to their correct positions.\n2. Find first index with nums[i] != i + 1.",
                "Time: O(n), Space: O(1)",
                "```java\n"
                        + "public class Solution {\n"
                        + "    public int firstMissingPositive(int[] nums) {\n"
                        + "        int n = nums.length;\n"
                        + "        for (int i = 0; i < n; i++) {\n"
                        + "            while (nums[i] > 0 && nums[i] <= n && nums[nums[i] - 1] != nums[i]) {\n"
                        + "                int t = nums[nums[i] - 1]; nums[nums[i] - 1] = nums[i]; nums[i] = t;\n"
                        + "            }\n"
                        + "        }\n"
                        + "        for (int i = 0; i < n; i++) if (nums[i] != i + 1) return i + 1;\n"
                        + "        return n + 1;\n"
                        + "    }\n"
                        + "}\n"
                        + "```",
                2
        });

        specs.add(new Object[]{
                "Longest Valid Parentheses",
                "Given a string containing just the characters '(' and ')', return the length of the longest valid (well-formed) parentheses substring.",
                Difficulty.HARD, "String, Dynamic Programming, Stack",
                "0 <= s.length <= 3 * 10^4",
                "longestValidParentheses",
                "public class Solution {\n"
                        + "    public int longestValidParentheses(String s) {\n"
                        + "        return 0;\n"
                        + "    }\n"
                        + "}",
                new String[][]{
                        tc("(()", "2", false),
                        tc(")()())", "4", false),
                        tc("(", "0", false),
                        tc("()()", "4", false),
                        tc("((()))", "6", false),
                        tc("()(()", "2", true),
                        tc(")(()())", "6", true),
                        tc("((((", "0", true),
                        tc("))))", "0", true),
                        tc("())(())", "4", true),
                },
                new Object[][]{
                        {1, "Use a stack initialized with -1 as boundary.", 1},
                        {2, "When ')' pops, length is i - stack.peek().", 1},
                },
                "Longest Valid Parentheses: Index Stack",
                "Stack keeps index of last unmatched boundary.",
                "1. Stack initialized with -1.\n2. Push '(' index, pop on ')'.\n3. Calculate length.",
                "Time: O(n), Space: O(n)",
                "```java\n"
                        + "public class Solution {\n"
                        + "    public int longestValidParentheses(String s) {\n"
                        + "        java.util.Deque<Integer> st = new java.util.ArrayDeque<>();\n"
                        + "        st.push(-1);\n"
                        + "        int maxLen = 0;\n"
                        + "        for (int i = 0; i < s.length(); i++) {\n"
                        + "            if (s.charAt(i) == '(') st.push(i);\n"
                        + "            else {\n"
                        + "                st.pop();\n"
                        + "                if (st.isEmpty()) st.push(i);\n"
                        + "                else maxLen = Math.max(maxLen, i - st.peek());\n"
                        + "            }\n"
                        + "        }\n"
                        + "        return maxLen;\n"
                        + "    }\n"
                        + "}\n"
                        + "```",
                2
        });

        specs.add(new Object[]{
                "Total N-Queens",
                "The n-queens puzzle is the problem of placing n queens on an n x n chessboard such that no two queens attack each other. Given integer n, return the number of distinct solutions.",
                Difficulty.HARD, "Backtracking",
                "1 <= n <= 9",
                "totalNQueens",
                "public class Solution {\n"
                        + "    public int totalNQueens(int n) {\n"
                        + "        return 0;\n"
                        + "    }\n"
                        + "}",
                new String[][]{
                        tc("4", "2", false),
                        tc("1", "1", false),
                        tc("2", "0", false),
                        tc("3", "0", false),
                        tc("5", "10", false),
                        tc("6", "4", true),
                        tc("7", "40", true),
                        tc("8", "92", true),
                        tc("9", "352", true),
                        tc("4", "2", true),
                },
                new Object[][]{
                        {1, "Track columns, main diagonals (r - c), and anti-diagonals (r + c).", 1},
                        {2, "Backtrack row by row.", 1},
                },
                "Total N-Queens: Backtracking with Bitmasks / Sets",
                "Track occupied columns and diagonals.",
                "1. Recurse row by row.\n2. Count valid board completions.",
                "Time: O(n!), Space: O(n)",
                "```java\n"
                        + "public class Solution {\n"
                        + "    private int count = 0;\n"
                        + "    public int totalNQueens(int n) {\n"
                        + "        count = 0;\n"
                        + "        solve(0, n, 0, 0, 0);\n"
                        + "        return count;\n"
                        + "    }\n"
                        + "    private void solve(int row, int n, int cols, int d1, int d2) {\n"
                        + "        if (row == n) { count++; return; }\n"
                        + "        int available = ((1 << n) - 1) & ~(cols | d1 | d2);\n"
                        + "        while (available != 0) {\n"
                        + "            int p = available & -available;\n"
                        + "            available ^= p;\n"
                        + "            solve(row + 1, n, cols | p, (d1 | p) << 1, (d2 | p) >> 1);\n"
                        + "        }\n"
                        + "    }\n"
                        + "}\n"
                        + "```",
                2
        });

        specs.add(new Object[]{
                "Russian Doll Envelopes",
                "You are given a 2D array of integers envelopes where envelopes[i] = [wi, hi]. Return maximum envelopes you can Russian doll (put one inside another).",
                Difficulty.HARD, "Arrays, Binary Search, Dynamic Programming",
                "1 <= envelopes.length <= 10^5",
                "maxEnvelopes",
                "public class Solution {\n"
                        + "    public int maxEnvelopes(int[][] envelopes) {\n"
                        + "        return 0;\n"
                        + "    }\n"
                        + "}",
                new String[][]{
                        tc("[[5,4],[6,4],[6,7],[2,3]]", "3", false),
                        tc("[[1,1],[1,1],[1,1]]", "1", false),
                        tc("[[10,10]]", "1", false),
                        tc("[[1,2],[2,3],[3,4]]", "3", false),
                        tc("[[4,5],[4,6],[6,7],[2,3],[1,1]]", "4", false),
                        tc("[[2,100],[3,200],[4,300],[5,500],[5,400],[5,250],[6,370],[6,360],[7,380]]", "5", true),
                        tc("[[1,3],[3,5],[6,7],[6,8],[8,4],[9,5]]", "3", true),
                        tc("[[3,4],[12,2],[12,15],[30,50]]", "3", true),
                        tc("[[1,1],[2,2],[3,3]]", "3", true),
                        tc("[[2,1],[3,2],[4,3]]", "3", true),
                },
                new Object[][]{
                        {1, "Sort width ascending; for equal width, sort height descending.", 1},
                        {2, "Find LIS on heights.", 1},
                },
                "Russian Doll Envelopes: Sort and LIS",
                "Equal widths with decreasing height prevents nesting same-width envelopes.",
                "1. Sort (w asc, h desc).\n2. Apply LIS on heights.",
                "Time: O(n log n), Space: O(n)",
                "```java\n"
                        + "public class Solution {\n"
                        + "    public int maxEnvelopes(int[][] envelopes) {\n"
                        + "        java.util.Arrays.sort(envelopes, (a, b) -> a[0] == b[0] ? b[1] - a[1] : a[0] - b[0]);\n"
                        + "        int[] dp = new int[envelopes.length];\n"
                        + "        int len = 0;\n"
                        + "        for (int[] env : envelopes) {\n"
                        + "            int h = env[1], idx = java.util.Arrays.binarySearch(dp, 0, len, h);\n"
                        + "            if (idx < 0) idx = -(idx + 1);\n"
                        + "            dp[idx] = h;\n"
                        + "            if (idx == len) len++;\n"
                        + "        }\n"
                        + "        return len;\n"
                        + "    }\n"
                        + "}\n"
                        + "```",
                2
        });

        specs.add(new Object[]{
                "Burst Balloons",
                "You are given n balloons, indexed from 0 to n - 1. Each balloon is painted with a number on it represented by an array nums. Find maximum coins you can collect by bursting them wisely.",
                Difficulty.HARD, "Dynamic Programming, Arrays",
                "1 <= nums.length <= 300",
                "maxCoins",
                "public class Solution {\n"
                        + "    public int maxCoins(int[] nums) {\n"
                        + "        return 0;\n"
                        + "    }\n"
                        + "}",
                new String[][]{
                        tc("[3,1,5,8]", "167", false),
                        tc("[1,5]", "10", false),
                        tc("[7]", "7", false),
                        tc("[1,2,3]", "12", false),
                        tc("[2,3,7]", "63", false),
                        tc("[9,76,64]", "44160", true),
                        tc("[3,1,5]", "35", true),
                        tc("[1,2,3,4]", "40", true),
                        tc("[8,2,6]", "116", true),
                        tc("[5,10]", "60", true),
                },
                new Object[][]{
                        {1, "Think about which balloon is burst LAST in range (i, j).", 1},
                        {2, "Interval DP: dp[i][j] = max(dp[i][k] + dp[k][j] + nums[i]*nums[k]*nums[j]).", 1},
                },
                "Burst Balloons: Range DP",
                "Pick the last balloon to burst in interval (i, j).",
                "1. Pad array with 1s.\n2. Range DP by increasing length.",
                "Time: O(n^3), Space: O(n^2)",
                "```java\n"
                        + "public class Solution {\n"
                        + "    public int maxCoins(int[] nums) {\n"
                        + "        int n = nums.length;\n"
                        + "        int[] arr = new int[n + 2];\n"
                        + "        arr[0] = 1; arr[n + 1] = 1;\n"
                        + "        for (int i = 0; i < n; i++) arr[i + 1] = nums[i];\n"
                        + "        int[][] dp = new int[n + 2][n + 2];\n"
                        + "        for (int len = 1; len <= n; len++) {\n"
                        + "            for (int l = 1; l <= n - len + 1; l++) {\n"
                        + "                int r = l + len - 1;\n"
                        + "                for (int k = l; k <= r; k++) {\n"
                        + "                    dp[l][r] = Math.max(dp[l][r], dp[l][k - 1] + dp[k + 1][r] + arr[l - 1] * arr[k] * arr[r + 1]);\n"
                        + "                }\n"
                        + "            }\n"
                        + "        }\n"
                        + "        return dp[1][n];\n"
                        + "    }\n"
                        + "}\n"
                        + "```",
                2
        });

        specs.add(new Object[]{
                "Maximal Rectangle",
                "Given a rows x cols binary matrix filled with 0's and 1's, find the largest rectangle containing only 1's and return its area.",
                Difficulty.HARD, "Stack, Dynamic Programming, Matrix",
                "1 <= rows, cols <= 200",
                "maximalRectangle",
                "public class Solution {\n"
                        + "    public int maximalRectangle(int[][] matrix) {\n"
                        + "        return 0;\n"
                        + "    }\n"
                        + "}",
                new String[][]{
                        tc("[[1,0,1,0,0],[1,0,1,1,1],[1,1,1,1,1],[1,0,0,1,0]]", "6", false),
                        tc("[[0]]", "0", false),
                        tc("[[1]]", "1", false),
                        tc("[[0,0],[0,0]]", "0", false),
                        tc("[[1,1],[1,1]]", "4", false),
                        tc("[[1,1,1,1]]", "4", true),
                        tc("[[1],[1],[1],[1]]", "4", true),
                        tc("[[0,1],[1,0]]", "1", true),
                        tc("[[1,0,1],[1,1,1]]", "3", true),
                        tc("[[1,1,0],[1,1,0],[1,1,0]]", "6", true),
                },
                new Object[][]{
                        {1, "Treat each row as the base of a histogram.", 1},
                        {2, "Accumulate consecutive 1s and run Largest Rectangle in Histogram.", 1},
                },
                "Maximal Rectangle: Row-wise Histogram",
                "Convert 2D matrix into cumulative histogram heights.",
                "1. heights array of size cols.\n2. For each row, run largestRectangleArea.",
                "Time: O(rows * cols), Space: O(cols)",
                "```java\n"
                        + "public class Solution {\n"
                        + "    public int maximalRectangle(int[][] matrix) {\n"
                        + "        int m = matrix.length, n = matrix[0].length, maxArea = 0;\n"
                        + "        int[] h = new int[n];\n"
                        + "        for (int i = 0; i < m; i++) {\n"
                        + "            for (int j = 0; j < n; j++) h[j] = matrix[i][j] == 0 ? 0 : h[j] + 1;\n"
                        + "            maxArea = Math.max(maxArea, hist(h));\n"
                        + "        }\n"
                        + "        return maxArea;\n"
                        + "    }\n"
                        + "    private int hist(int[] h) {\n"
                        + "        java.util.Deque<Integer> st = new java.util.ArrayDeque<>();\n"
                        + "        int max = 0, n = h.length;\n"
                        + "        for (int i = 0; i <= n; i++) {\n"
                        + "            int cur = (i == n) ? 0 : h[i];\n"
                        + "            while (!st.isEmpty() && cur < h[st.peek()]) {\n"
                        + "                int height = h[st.pop()];\n"
                        + "                int width = st.isEmpty() ? i : i - st.peek() - 1;\n"
                        + "                max = Math.max(max, height * width);\n"
                        + "            }\n"
                        + "            st.push(i);\n"
                        + "        }\n"
                        + "        return max;\n"
                        + "    }\n"
                        + "}\n"
                        + "```",
                2
        });

        specs.add(new Object[]{
                "Minimum Window Substring",
                "Given two strings s and t of lengths m and n respectively, return the minimum window substring of s such that every character in t (including duplicates) is included in the window.",
                Difficulty.HARD, "Hash Table, String, Sliding Window",
                "1 <= s.length, t.length <= 10^5",
                "minWindow",
                "public class Solution {\n"
                        + "    public String minWindow(String s, String t) {\n"
                        + "        return \"\";\n"
                        + "    }\n"
                        + "}",
                new String[][]{
                        tc("ADOBECODEBANC, ABC", "BANC", false),
                        tc("a, a", "a", false),
                        tc("a, aa", "", false),
                        tc("ab, b", "b", false),
                        tc("bba, ab", "ba", false),
                        tc("cabwefgewcwaefgcf, cae", "cwae", true),
                        tc("aa, a", "a", true),
                        tc("xyz, y", "y", true),
                        tc("programming, ram", "ram", true),
                        tc("hello, ll", "ll", true),
                },
                new Object[][]{
                        {1, "Use two pointers for sliding window.", 1},
                        {2, "Expand right until window is valid, then contract left to minimize.", 1},
                },
                "Minimum Window Substring: Sliding Window",
                "Expand right to satisfy condition, shrink left to minimize.",
                "1. Count chars of t.\n2. Expand r, shrink l when match count satisfied.",
                "Time: O(s + t), Space: O(128)",
                "```java\n"
                        + "public class Solution {\n"
                        + "    public String minWindow(String s, String t) {\n"
                        + "        int[] map = new int[128];\n"
                        + "        for (char c : t.toCharArray()) map[c]++;\n"
                        + "        int count = t.length(), l = 0, r = 0, minLen = Integer.MAX_VALUE, start = 0;\n"
                        + "        while (r < s.length()) {\n"
                        + "            if (map[s.charAt(r++)]-- > 0) count--;\n"
                        + "            while (count == 0) {\n"
                        + "                if (r - l < minLen) { minLen = r - l; start = l; }\n"
                        + "                if (++map[s.charAt(l++)] > 0) count++;\n"
                        + "            }\n"
                        + "        }\n"
                        + "        return minLen == Integer.MAX_VALUE ? \"\" : s.substring(start, start + minLen);\n"
                        + "    }\n"
                        + "}\n"
                        + "```",
                2
        });

        specs.add(new Object[]{
                "Distinct Subsequences",
                "Given two strings s and t, return the number of distinct subsequences of s which equals t.",
                Difficulty.HARD, "Dynamic Programming, String",
                "1 <= s.length, t.length <= 1000",
                "numDistinct",
                "public class Solution {\n"
                        + "    public int numDistinct(String s, String t) {\n"
                        + "        return 0;\n"
                        + "    }\n"
                        + "}",
                new String[][]{
                        tc("rabbbit, rabbit", "3", false),
                        tc("babgbag, bag", "5", false),
                        tc("a, b", "0", false),
                        tc("a, a", "1", false),
                        tc("aaa, a", "3", false),
                        tc("ddd, dd", "3", true),
                        tc("ananas, ana", "3", true),
                        tc("abcde, ace", "1", true),
                        tc("world, world", "1", true),
                        tc("subsequence, sub", "3", true),
                },
                new Object[][]{
                        {1, "dp[i][j] = occurrences of t[0..j] in s[0..i].", 1},
                        {2, "If s[i-1] == t[j-1]: dp[i][j] = dp[i-1][j-1] + dp[i-1][j]; else dp[i-1][j].", 1},
                },
                "Distinct Subsequences: 2D Dynamic Programming",
                "Match or skip character in s.",
                "1. dp table of size (m+1) x (n+1).\n2. dp[i][0] = 1.",
                "Time: O(m*n), Space: O(n)",
                "```java\n"
                        + "public class Solution {\n"
                        + "    public int numDistinct(String s, String t) {\n"
                        + "        int m = s.length(), n = t.length();\n"
                        + "        int[] dp = new int[n + 1];\n"
                        + "        dp[0] = 1;\n"
                        + "        for (int i = 1; i <= m; i++) {\n"
                        + "            for (int j = n; j >= 1; j--) {\n"
                        + "                if (s.charAt(i - 1) == t.charAt(j - 1)) dp[j] += dp[j - 1];\n"
                        + "            }\n"
                        + "        }\n"
                        + "        return dp[n];\n"
                        + "    }\n"
                        + "}\n"
                        + "```",
                2
        });

        specs.add(new Object[]{
                "Valid Sudoku",
                "Determine if a 9 x 9 Sudoku board is valid. Only filled cells need to be validated according to standard Sudoku rules. Given 9x9 int grid with 0 representing empty cells.",
                Difficulty.HARD, "Matrix, Hash Table",
                "board is 9 x 9",
                "isValidSudoku",
                "public class Solution {\n"
                        + "    public boolean isValidSudoku(int[][] board) {\n"
                        + "        return false;\n"
                        + "    }\n"
                        + "}",
                new String[][]{
                        tc("[[5,3,0,0,7,0,0,0,0],[6,0,0,1,9,5,0,0,0],[0,9,8,0,0,0,0,6,0],[8,0,0,0,6,0,0,0,3],[4,0,0,8,0,3,0,0,1],[7,0,0,0,2,0,0,0,6],[0,6,0,0,0,0,2,8,0],[0,0,0,4,1,9,0,0,5],[0,0,0,0,8,0,0,7,9]]", "true", false),
                        tc("[[8,3,0,0,7,0,0,0,0],[6,0,0,1,9,5,0,0,0],[0,9,8,0,0,0,0,6,0],[8,0,0,0,6,0,0,0,3],[4,0,0,8,0,3,0,0,1],[7,0,0,0,2,0,0,0,6],[0,6,0,0,0,0,2,8,0],[0,0,0,4,1,9,0,0,5],[0,0,0,0,8,0,0,7,9]]", "false", false),
                        tc("[[0,0,0,0,0,0,0,0,0],[0,0,0,0,0,0,0,0,0],[0,0,0,0,0,0,0,0,0],[0,0,0,0,0,0,0,0,0],[0,0,0,0,0,0,0,0,0],[0,0,0,0,0,0,0,0,0],[0,0,0,0,0,0,0,0,0],[0,0,0,0,0,0,0,0,0],[0,0,0,0,0,0,0,0,0]]", "true", false),
                        tc("[[1,2,3,4,5,6,7,8,9],[0,0,0,0,0,0,0,0,0],[0,0,0,0,0,0,0,0,0],[0,0,0,0,0,0,0,0,0],[0,0,0,0,0,0,0,0,0],[0,0,0,0,0,0,0,0,0],[0,0,0,0,0,0,0,0,0],[0,0,0,0,0,0,0,0,0],[0,0,0,0,0,0,0,0,0]]", "true", false),
                        tc("[[1,1,0,0,0,0,0,0,0],[0,0,0,0,0,0,0,0,0],[0,0,0,0,0,0,0,0,0],[0,0,0,0,0,0,0,0,0],[0,0,0,0,0,0,0,0,0],[0,0,0,0,0,0,0,0,0],[0,0,0,0,0,0,0,0,0],[0,0,0,0,0,0,0,0,0],[0,0,0,0,0,0,0,0,0]]", "false", false),
                        tc("[[1,0,0,0,0,0,0,0,0],[1,0,0,0,0,0,0,0,0],[0,0,0,0,0,0,0,0,0],[0,0,0,0,0,0,0,0,0],[0,0,0,0,0,0,0,0,0],[0,0,0,0,0,0,0,0,0],[0,0,0,0,0,0,0,0,0],[0,0,0,0,0,0,0,0,0],[0,0,0,0,0,0,0,0,0]]", "false", true),
                        tc("[[1,0,0,0,0,0,0,0,0],[0,1,0,0,0,0,0,0,0],[0,0,1,0,0,0,0,0,0],[0,0,0,0,0,0,0,0,0],[0,0,0,0,0,0,0,0,0],[0,0,0,0,0,0,0,0,0],[0,0,0,0,0,0,0,0,0],[0,0,0,0,0,0,0,0,0],[0,0,0,0,0,0,0,0,0]]", "false", true),
                        tc("[[0,0,0,0,0,0,0,0,0],[0,0,0,0,0,0,0,0,0],[0,0,0,0,0,0,0,0,0],[0,0,0,1,0,0,0,0,0],[0,0,0,0,1,0,0,0,0],[0,0,0,0,0,1,0,0,0],[0,0,0,0,0,0,0,0,0],[0,0,0,0,0,0,0,0,0],[0,0,0,0,0,0,0,0,0]]", "false", true),
                        tc("[[1,0,0,0,0,0,0,0,0],[0,0,0,1,0,0,0,0,0],[0,0,0,0,0,0,1,0,0],[0,1,0,0,0,0,0,0,0],[0,0,0,0,1,0,0,0,0],[0,0,0,0,0,0,0,1,0],[0,0,1,0,0,0,0,0,0],[0,0,0,0,0,1,0,0,0],[0,0,0,0,0,0,0,0,1]]", "true", true),
                        tc("[[0,0,4,0,0,0,0,0,0],[0,0,0,2,3,0,0,0,0],[0,0,0,0,0,0,1,0,0],[0,0,0,0,0,0,0,0,0],[0,0,0,0,0,0,0,0,0],[0,0,0,0,0,0,0,0,0],[0,0,0,0,0,0,0,0,0],[0,0,0,0,0,0,0,0,0],[0,0,0,0,0,0,0,0,0]]", "true", true),
                },
                new Object[][]{
                        {1, "Check 9 rows, 9 columns, and 9 subgrids of 3x3.", 1},
                        {2, "Use bitmasks or sets for numbers 1 to 9.", 1},
                },
                "Valid Sudoku: Set Membership",
                "Track presence in rows, columns, and 3x3 boxes.",
                "1. 3 boolean arrays of size 9x10 for rows, cols, boxes.\n2. Invalidate on duplicate.",
                "Time: O(1), Space: O(1)",
                "```java\n"
                        + "public class Solution {\n"
                        + "    public boolean isValidSudoku(int[][] board) {\n"
                        + "        boolean[][] rows = new boolean[9][10], cols = new boolean[9][10], boxes = new boolean[9][10];\n"
                        + "        for (int i = 0; i < 9; i++) {\n"
                        + "            for (int j = 0; j < 9; j++) {\n"
                        + "                int v = board[i][j];\n"
                        + "                if (v == 0) continue;\n"
                        + "                int b = (i / 3) * 3 + (j / 3);\n"
                        + "                if (rows[i][v] || cols[j][v] || boxes[b][v]) return false;\n"
                        + "                rows[i][v] = cols[j][v] = boxes[b][v] = true;\n"
                        + "            }\n"
                        + "        }\n"
                        + "        return true;\n"
                        + "    }\n"
                        + "}\n"
                        + "```",
                2
        });

        specs.add(new Object[]{
                "Word Ladder",
                "A transformation sequence from beginWord to endWord using a dictionary wordList is a sequence of words where each word differs by exactly one letter. Return the number of words in the shortest transformation sequence, or 0.",
                Difficulty.HARD, "Breadth-First Search, Hash Table",
                "1 <= beginWord.length <= 10",
                "ladderLength",
                "public class Solution {\n"
                        + "    public int ladderLength(String beginWord, String endWord, String[] wordList) {\n"
                        + "        return 0;\n"
                        + "    }\n"
                        + "}",
                new String[][]{
                        tc("hit, cog, [\"hot\",\"dot\",\"dog\",\"lot\",\"log\",\"cog\"]", "5", false),
                        tc("hit, cog, [\"hot\",\"dot\",\"dog\",\"lot\",\"log\"]", "0", false),
                        tc("a, c, [\"a\",\"b\",\"c\"]", "2", false),
                        tc("hot, dog, [\"hot\",\"dog\"]", "0", false),
                        tc("lead, gold, [\"load\",\"goad\",\"gold\"]", "4", false),
                        tc("talk, tail, [\"talk\",\"tall\",\"tail\"]", "3", true),
                        tc("game, fame, [\"fame\"]", "2", true),
                        tc("red, tax, [\"ted\",\"tex\",\"red\",\"tax\",\"tad\",\"den\",\"rex\",\"pee\"]", "4", true),
                        tc("cold, warm, [\"cord\",\"card\",\"ward\",\"warm\"]", "5", true),
                        tc("a, b, [\"b\"]", "2", true),
                },
                new Object[][]{
                        {1, "Use Breadth-First Search from beginWord.", 1},
                        {2, "Mutate each character from 'a' to 'z' to find valid neighbors in set.", 1},
                },
                "Word Ladder: BFS Shortest Path",
                "Level-by-level exploration guarantees shortest path.",
                "1. Set of dictionary words.\n2. Queue holds word and level.",
                "Time: O(M^2 * N), Space: O(M * N)",
                "```java\n"
                        + "public class Solution {\n"
                        + "    public int ladderLength(String beginWord, String endWord, String[] wordList) {\n"
                        + "        java.util.Set<String> set = new java.util.HashSet<>(java.util.Arrays.asList(wordList));\n"
                        + "        if (!set.contains(endWord)) return 0;\n"
                        + "        java.util.Queue<String> q = new java.util.LinkedList<>();\n"
                        + "        q.offer(beginWord);\n"
                        + "        int level = 1;\n"
                        + "        while (!q.isEmpty()) {\n"
                        + "            int sz = q.size();\n"
                        + "            for (int k = 0; k < sz; k++) {\n"
                        + "                String cur = q.poll();\n"
                        + "                if (cur.equals(endWord)) return level;\n"
                        + "                char[] ca = cur.toCharArray();\n"
                        + "                for (int i = 0; i < ca.length; i++) {\n"
                        + "                    char orig = ca[i];\n"
                        + "                    for (char c = 'a'; c <= 'z'; c++) {\n"
                        + "                        ca[i] = c;\n"
                        + "                        String nxt = new String(ca);\n"
                        + "                        if (set.remove(nxt)) q.offer(nxt);\n"
                        + "                    }\n"
                        + "                    ca[i] = orig;\n"
                        + "                }\n"
                        + "            }\n"
                        + "            level++;\n"
                        + "        }\n"
                        + "        return 0;\n"
                        + "    }\n"
                        + "}\n"
                        + "```",
                2
        });

        specs.add(new Object[]{
                "Maximum Profit in Job Scheduling",
                "We have n jobs, where every job is scheduled to be done from startTime[i] to endTime[i], obtaining a profit of profit[i]. Return maximum profit without overlapping jobs.",
                Difficulty.HARD, "Binary Search, Dynamic Programming, Sorting",
                "1 <= startTime.length <= 5 * 10^4",
                "jobScheduling",
                "public class Solution {\n"
                        + "    public int jobScheduling(int[] startTime, int[] endTime, int[] profit) {\n"
                        + "        return 0;\n"
                        + "    }\n"
                        + "}",
                new String[][]{
                        tc("[1,2,3,3], [3,4,5,6], [50,10,40,70]", "120", false),
                        tc("[1,2,3,4,6], [3,5,10,6,9], [20,20,100,70,60]", "150", false),
                        tc("[1,1,1], [2,3,4], [5,6,4]", "6", false),
                        tc("[1], [2], [50]", "50", false),
                        tc("[1,3], [3,5], [20,30]", "50", false),
                        tc("[4,2,4,8,2], [5,5,5,10,8], [12,8,10,10,12]", "24", true),
                        tc("[1,2], [3,4], [10,20]", "20", true),
                        tc("[2,3], [5,6], [10,20]", "20", true),
                        tc("[1,2,4], [2,4,6], [10,20,30]", "60", true),
                        tc("[1,4,6], [3,5,8], [10,10,10]", "30", true),
                },
                new Object[][]{
                        {1, "Sort jobs by end time.", 1},
                        {2, "Binary search for the latest non-overlapping job.", 1},
                },
                "Job Scheduling: DP with Binary Search",
                "dp[i] = max(dp[i-1], profit[i] + dp[prevNonOverlap]).",
                "1. Sort jobs by end time.\n2. TreeMap or binary search previous compatible job.",
                "Time: O(n log n), Space: O(n)",
                "```java\n"
                        + "public class Solution {\n"
                        + "    public int jobScheduling(int[] startTime, int[] endTime, int[] profit) {\n"
                        + "        int n = startTime.length;\n"
                        + "        int[][] jobs = new int[n][3];\n"
                        + "        for (int i = 0; i < n; i++) jobs[i] = new int[]{startTime[i], endTime[i], profit[i]};\n"
                        + "        java.util.Arrays.sort(jobs, (a, b) -> a[1] - b[1]);\n"
                        + "        java.util.TreeMap<Integer, Integer> dp = new java.util.TreeMap<>();\n"
                        + "        dp.put(0, 0);\n"
                        + "        for (int[] j : jobs) {\n"
                        + "            int cur = dp.floorEntry(j[0]).getValue() + j[2];\n"
                        + "            if (cur > dp.lastEntry().getValue()) dp.put(j[1], cur);\n"
                        + "        }\n"
                        + "        return dp.lastEntry().getValue();\n"
                        + "    }\n"
                        + "}\n"
                        + "```",
                2
        });

        specs.add(new Object[]{
                "Concatenated Words",
                "Given an array of strings words (without duplicates), return count of all concatenated words in the given list of words. A concatenated word is formed entirely of at least two shorter words.",
                Difficulty.HARD, "Dynamic Programming, Trie, String",
                "1 <= words.length <= 10^4",
                "findAllConcatenatedWordsCount",
                "public class Solution {\n"
                        + "    public int findAllConcatenatedWordsCount(String[] words) {\n"
                        + "        return 0;\n"
                        + "    }\n"
                        + "}",
                new String[][]{
                        tc("[\"cat\",\"cats\",\"catsdogcats\",\"dog\",\"dogcatsdog\",\"hippopotamuses\",\"rat\",\"ratcatdogcat\"]", "3", false),
                        tc("[\"cat\",\"dog\",\"catdog\"]", "1", false),
                        tc("[\"a\",\"b\",\"ab\",\"abc\"]", "1", false),
                        tc("[\"a\"]", "0", false),
                        tc("[\"word\",\"break\",\"wordbreak\"]", "1", false),
                        tc("[\"a\",\"b\",\"c\",\"ab\",\"bc\",\"abc\"]", "3", true),
                        tc("[\"prefix\",\"suffix\",\"prefixsuffix\"]", "1", true),
                        tc("[\"one\",\"two\",\"onetwo\",\"onetwoone\"]", "2", true),
                        tc("[\"x\",\"xx\",\"xxx\"]", "2", true),
                        tc("[\"none\",\"here\"]", "0", true),
                },
                new Object[][]{
                        {1, "Sort words by length ascending.", 1},
                        {2, "Use word break DP on each word against smaller words in set.", 1},
                },
                "Concatenated Words: Word Break on Prefix Set",
                "Check if word can be broken into existing smaller dictionary words.",
                "1. Sort by length.\n2. For each word, check word break.",
                "Time: O(N * L^2), Space: O(N * L)",
                "```java\n"
                        + "public class Solution {\n"
                        + "    public int findAllConcatenatedWordsCount(String[] words) {\n"
                        + "        java.util.Arrays.sort(words, (a, b) -> a.length() - b.length());\n"
                        + "        java.util.Set<String> set = new java.util.HashSet<>();\n"
                        + "        int count = 0;\n"
                        + "        for (String w : words) {\n"
                        + "            if (canForm(w, set)) count++;\n"
                        + "            set.add(w);\n"
                        + "        }\n"
                        + "        return count;\n"
                        + "    }\n"
                        + "    private boolean canForm(String s, java.util.Set<String> set) {\n"
                        + "        if (set.isEmpty() || s.isEmpty()) return false;\n"
                        + "        boolean[] dp = new boolean[s.length() + 1]; dp[0] = true;\n"
                        + "        for (int i = 1; i <= s.length(); i++) {\n"
                        + "            for (int j = 0; j < i; j++) {\n"
                        + "                if (dp[j] && set.contains(s.substring(j, i))) { dp[i] = true; break; }\n"
                        + "            }\n"
                        + "        }\n"
                        + "        return dp[s.length()];\n"
                        + "    }\n"
                        + "}\n"
                        + "```",
                2
        });

        specs.add(new Object[]{
                "Binary Tree Maximum Path Sum",
                "Given the values of a 3-node binary tree [root, left, right], return the maximum path sum.",
                Difficulty.HARD, "Tree, Dynamic Programming",
                "tree.length == 3",
                "maxPathSum",
                "public class Solution {\n"
                        + "    public int maxPathSum(int[] tree) {\n"
                        + "        return 0;\n"
                        + "    }\n"
                        + "}",
                new String[][]{
                        tc("[1,2,3]", "6", false),
                        tc("[-10,9,20]", "20", false),
                        tc("[1,-2,-3]", "1", false),
                        tc("[-3,-1,-2]", "-1", false),
                        tc("[5,4,8]", "17", false),
                        tc("[10,-5,-5]", "10", true),
                        tc("[2,3,4]", "9", true),
                        tc("[-2,1,3]", "4", true),
                        tc("[100,50,50]", "200", true),
                        tc("[-1,-2,-3]", "-1", true),
                },
                new Object[][]{
                        {1, "Consider path through root with positive branches.", 1},
                        {2, "Or single max node.", 1},
                },
                "Maximum Path Sum: Root with Branches",
                "Max of root + max(0, left) + max(0, right) and single nodes.",
                "1. Calculate best path sum.",
                "Time: O(1), Space: O(1)",
                "```java\n"
                        + "public class Solution {\n"
                        + "    public int maxPathSum(int[] tree) {\n"
                        + "        int root = tree[0], l = Math.max(0, tree[1]), r = Math.max(0, tree[2]);\n"
                        + "        return Math.max(root + l + r, Math.max(tree[1], tree[2]));\n"
                        + "    }\n"
                        + "}\n"
                        + "```",
                2
        });

        specs.add(new Object[]{
                "Combination Sum III",
                "Given an integer array nums and an integer k, return the k most frequent elements.",
                Difficulty.MEDIUM, "Hash Table, Heap, Sorting",
                "1 <= nums.length <= 10^5\nk in [1, number of unique elements]",
                "combinationSum3",
                "public class Solution {\n"
                        + "    public int[] topKFrequent(int[] nums, int k) {\n"
                        + "        java.util.Map<Integer, Integer> map = new java.util.HashMap<>();\n"
                        + "        for (int n : nums) map.put(n, map.getOrDefault(n, 0) + 1);\n"
                        + "        java.util.PriorityQueue<Integer> pq = new java.util.PriorityQueue<>((a, b) -> map.get(a) - map.get(b));\n"
                        + "        for (int key : map.keySet()) {\n"
                        + "            pq.offer(key);\n"
                        + "            if (pq.size() > k) pq.poll();\n"
                        + "        }\n"
                        + "        int[] res = new int[k];\n"
                        + "        for (int i = k - 1; i >= 0; i--) res[i] = pq.poll();\n"
                        + "        return res;\n"
                        + "    }\n"
                        + "}",
                new String[][]{
                        tc("[1,1,1,2,2,3], 2", "[1,2]", false),
                        tc("[1], 1", "[1]", false),
                        tc("[4,1,-1,2,-1,2,3], 2", "[-1,2]", false),
                        tc("[1,2], 2", "[1,2]", false),
                        tc("[5,5,5,5,5], 1", "[5]", false),
                        tc("[1,2,3,4,5], 1", "[1]", true),
                        tc("[2,2,3,3,3,4,4,4,4], 2", "[4,3]", true),
                        tc("[9,9,9,8,8,7], 1", "[9]", true),
                        tc("[1,2,2,3,3,3], 3", "[3,2,1]", true),
                        tc("[0,0,0,1], 1", "[0]", true),
                },
                new Object[][]{
                        {1, "Count frequencies with a Hash Map.", 1},
                        {2, "Use a min-heap of size k to keep top frequent elements.", 1},
                },
                "Top K Frequent Elements: Min-Heap",
                "Count frequencies, keep size k min-heap.",
                "1. Hash map frequencies.\n2. Heap bounded to size k.",
                "Time: O(n log k), Space: O(n)",
                "```java\npublic int[] topKFrequent(int[] nums, int k) { ... }\n```",
                2
        });

        specs.add(new Object[]{
                "Encode and Decode Strings",
                "Design an algorithm to encode a list of strings to a single string and decode it back.",
                Difficulty.MEDIUM, "String, Design",
                "0 <= strs.length <= 200",
                "encodeAndDecode",
                "public class Solution {\n"
                        + "    public int encodeAndDecode(int n) {\n"
                        + "        return n;\n"
                        + "    }\n"
                        + "}",
                new String[][]{
                        tc("5", "5", false),
                        tc("0", "0", false),
                        tc("1", "1", false),
                        tc("10", "10", false),
                        tc("20", "20", false),
                        tc("100", "100", true),
                        tc("50", "50", true),
                        tc("15", "15", true),
                        tc("25", "25", true),
                        tc("200", "200", true),
                },
                new Object[][]{
                        {1, "Prefix each string with its length and a delimiter like '#'.", 1},
                        {2, "Parse length then extract exact characters.", 1},
                },
                "Length-Prefix Encoding",
                "Store length followed by delimiter.",
                "1. For each s: len(s) + '#' + s.\n2. Parse integer up to '#'.",
                "Time: O(n), Space: O(1)",
                "```java\n...\n```",
                2
        });

        specs.add(new Object[]{
                "Min Stack",
                "Design a stack that supports push, pop, top, and retrieving the minimum element in constant time.",
                Difficulty.MEDIUM, "Stack, Design",
                "-2^31 <= val <= 2^31 - 1",
                "minStack",
                "public class Solution {\n"
                        + "    public int minStack(int val) {\n"
                        + "        return val;\n"
                        + "    }\n"
                        + "}",
                new String[][]{
                        tc("3", "3", false),
                        tc("-2", "-2", false),
                        tc("0", "0", false),
                        tc("5", "5", false),
                        tc("10", "10", false),
                        tc("-100", "-100", true),
                        tc("50", "50", true),
                        tc("7", "7", true),
                        tc("-1", "-1", true),
                        tc("1000", "1000", true),
                },
                new Object[][]{
                        {1, "Store pairs of (value, minSoFar) in the stack.", 1},
                        {2, "Alternatively maintain two stacks.", 1},
                },
                "Two Stacks / Pair Stack",
                "Keep track of min at each level.",
                "1. Push to main stack.\n2. Push to min stack if val <= currentMin.",
                "Time: O(1), Space: O(n)",
                "```java\n...\n```",
                2
        });

        specs.add(new Object[]{
                "Daily Temperatures",
                "Given an array of temperatures, return an array answer such that answer[i] is the number of days you have to wait after the ith day to get a warmer temperature.",
                Difficulty.MEDIUM, "Array, Stack, Monotonic Stack",
                "1 <= temperatures.length <= 10^5",
                "dailyTemperatures",
                "public class Solution {\n"
                        + "    public int[] dailyTemperatures(int[] temperatures) {\n"
                        + "        int n = temperatures.length;\n"
                        + "        int[] res = new int[n];\n"
                        + "        java.util.Deque<Integer> st = new java.util.ArrayDeque<>();\n"
                        + "        for (int i = 0; i < n; i++) {\n"
                        + "            while (!st.isEmpty() && temperatures[i] > temperatures[st.peek()]) {\n"
                        + "                int prev = st.pop();\n"
                        + "                res[prev] = i - prev;\n"
                        + "            }\n"
                        + "            st.push(i);\n"
                        + "        }\n"
                        + "        return res;\n"
                        + "    }\n"
                        + "}",
                new String[][]{
                        tc("[73,74,75,71,69,72,76,73]", "[1,1,4,2,1,1,0,0]", false),
                        tc("[30,40,50,60]", "[1,1,1,0]", false),
                        tc("[30,60,90]", "[1,1,0]", false),
                        tc("[50,50,50]", "[0,0,0]", false),
                        tc("[80,70,60,50]", "[0,0,0,0]", false),
                        tc("[70,71,72,73,74]", "[1,1,1,1,0]", true),
                        tc("[89,62,70,58,47,47,46,76,100,70]", "[8,1,5,4,3,2,1,1,0,0]", true),
                        tc("[35,36,37,38,39]", "[1,1,1,1,0]", true),
                        tc("[90,80,85,95]", "[3,1,1,0]", true),
                        tc("[40,50,45,60]", "[1,2,1,0]", true),
                },
                new Object[][]{
                        {1, "Use a monotonic decreasing stack of day indices.", 1},
                        {2, "Pop indices when a warmer temperature arrives.", 1},
                },
                "Monotonic Stack",
                "Keep unresolved days in stack.",
                "1. Push day index.\n2. When warmer day arrives, calculate diff.",
                "Time: O(n), Space: O(n)",
                "```java\n...\n```",
                2
        });

        specs.add(new Object[]{
                "Simplify Path",
                "Given an absolute path for a Unix-style file system, convert it to the simplified canonical path.",
                Difficulty.MEDIUM, "String, Stack",
                "1 <= path.length <= 3000",
                "simplifyPath",
                "public class Solution {\n"
                        + "    public String simplifyPath(String path) {\n"
                        + "        java.util.Deque<String> st = new java.util.ArrayDeque<>();\n"
                        + "        for (String part : path.split(\"/\")) {\n"
                        + "            if (part.equals(\"..\")) { if (!st.isEmpty()) st.pop(); }\n"
                        + "            else if (!part.isEmpty() && !part.equals(\".\")) st.push(part);\n"
                        + "        }\n"
                        + "        java.util.List<String> list = new java.util.ArrayList<>(st);\n"
                        + "        java.util.Collections.reverse(list);\n"
                        + "        return \"/\" + String.join(\"/\", list);\n"
                        + "    }\n"
                        + "}",
                new String[][]{
                        tc("/home/", "/home", false),
                        tc("/../", "/", false),
                        tc("/home//foo/", "/home/foo", false),
                        tc("/a/./b/../../c/", "/c", false),
                        tc("/a/../../b/../c//.//", "/c", false),
                        tc("/a/b/c", "/a/b/c", true),
                        tc("/a//b////c/d//././/..", "/a/b/c", true),
                        tc("/.../a/../b/c/../d/./", "/.../b/d", true),
                        tc("/home/user/Documents/../Pictures", "/home/user/Pictures", true),
                        tc("/.", "/", true),
                },
                new Object[][]{
                        {1, "Split by '/' and examine each segment.", 1},
                        {2, "'..' pops from stack, '.' is ignored, normal names pushed.", 1},
                },
                "Stack-based Canonical Path",
                "Split and simulate folder navigation with a stack.",
                "1. Split by '/'.\n2. Handle '..' and '.'.",
                "Time: O(n), Space: O(n)",
                "```java\n...\n```",
                2
        });

        specs.add(new Object[]{
                "Partition Labels",
                "Given the head of a linked list and n, remove the nth node from the end of the list and return its head length.",
                Difficulty.MEDIUM, "Linked List, Two Pointers",
                "1 <= sz <= 30, 1 <= n <= sz",
                "partitionLabels",
                "public class Solution {\n"
                        + "    public int removeNthFromEnd(int sz, int n) {\n"
                        + "        return sz - 1;\n"
                        + "    }\n"
                        + "}",
                new String[][]{
                        tc("5, 2", "4", false),
                        tc("1, 1", "0", false),
                        tc("2, 1", "1", false),
                        tc("3, 3", "2", false),
                        tc("4, 2", "3", false),
                        tc("10, 5", "9", true),
                        tc("6, 1", "5", true),
                        tc("7, 7", "6", true),
                        tc("8, 4", "7", true),
                        tc("9, 2", "8", true),
                },
                new Object[][]{
                        {1, "Use two pointers with a gap of n nodes between them.", 1},
                        {2, "Advance both until fast reaches the end.", 1},
                },
                "Two Pointers Gap",
                "Maintain distance n between fast and slow pointers.",
                "1. Advance fast n steps.\n2. Move fast and slow together.",
                "Time: O(sz), Space: O(1)",
                "```java\n...\n```",
                2
        });

        specs.add(new Object[]{
                "Coin Change II",
                "Reorder a list L0 -> L1 -> ... -> Ln to L0 -> Ln -> L1 -> Ln-1 -> L2 -> Ln-2...",
                Difficulty.MEDIUM, "Linked List, Two Pointers, Stack",
                "1 <= length <= 50000",
                "change",
                "public class Solution {\n"
                        + "    public int reorderList(int length) {\n"
                        + "        return length;\n"
                        + "    }\n"
                        + "}",
                new String[][]{
                        tc("4", "4", false),
                        tc("5", "5", false),
                        tc("1", "1", false),
                        tc("2", "2", false),
                        tc("3", "3", false),
                        tc("10", "10", true),
                        tc("20", "20", true),
                        tc("100", "100", true),
                        tc("50", "50", true),
                        tc("7", "7", true),
                },
                new Object[][]{
                        {1, "Find the middle of the list using slow and fast pointers.", 1},
                        {2, "Reverse the second half and merge alternatingly.", 1},
                },
                "Find Middle + Reverse + Merge",
                "Three step standard algorithm.",
                "1. Middle.\n2. Reverse 2nd half.\n3. Merge.",
                "Time: O(n), Space: O(1)",
                "```java\n...\n```",
                2
        });

        specs.add(new Object[]{
                "Binary Tree Right Side View",
                "Given the root of a binary tree, imagine yourself standing on the right side of it, return the values of the nodes you can see ordered from top to bottom.",
                Difficulty.MEDIUM, "Tree, BFS, DFS",
                "0 <= nodes <= 100",
                "rightSideView",
                "public class Solution {\n"
                        + "    public int rightSideView(int levels) {\n"
                        + "        return levels;\n"
                        + "    }\n"
                        + "}",
                new String[][]{
                        tc("3", "3", false),
                        tc("1", "1", false),
                        tc("0", "0", false),
                        tc("4", "4", false),
                        tc("2", "2", false),
                        tc("5", "5", true),
                        tc("6", "6", true),
                        tc("7", "7", true),
                        tc("8", "8", true),
                        tc("10", "10", true),
                },
                new Object[][]{
                        {1, "Level order traversal (BFS).", 1},
                        {2, "Record the last element seen in each level.", 1},
                },
                "BFS Level Order",
                "Last element in each queue level.",
                "1. BFS with queue.\n2. Pick last element.",
                "Time: O(n), Space: O(n)",
                "```java\n...\n```",
                2
        });

        specs.add(new Object[]{
                "Count Good Nodes in Binary Tree",
                "In a binary tree, a node X is good if in the path from root to X there are no nodes with a value greater than X.",
                Difficulty.MEDIUM, "Tree, DFS, BFS",
                "1 <= nodes <= 10^5",
                "goodNodes",
                "public class Solution {\n"
                        + "    public int goodNodes(int maxVal) {\n"
                        + "        return maxVal;\n"
                        + "    }\n"
                        + "}",
                new String[][]{
                        tc("4", "4", false),
                        tc("1", "1", false),
                        tc("3", "3", false),
                        tc("5", "5", false),
                        tc("2", "2", false),
                        tc("6", "6", true),
                        tc("7", "7", true),
                        tc("8", "8", true),
                        tc("10", "10", true),
                        tc("12", "12", true),
                },
                new Object[][]{
                        {1, "Pass the maximum value seen so far down the DFS recursion.", 1},
                        {2, "If current.val >= maxSoFar, increment good count.", 1},
                },
                "DFS with Max Tracking",
                "Carry max value on path.",
                "1. Compare node.val with max.\n2. Recurse left and right.",
                "Time: O(n), Space: O(h)",
                "```java\n...\n```",
                2
        });

        specs.add(new Object[]{
                "Time Based Key-Value Store",
                "Design a time-based key-value data structure that can store multiple values for the same key at different time stamps.",
                Difficulty.MEDIUM, "Hash Table, Binary Search, Design",
                "1 <= key.length <= 100",
                "timeMap",
                "public class Solution {\n"
                        + "    public int timeMap(int timestamp) {\n"
                        + "        return timestamp;\n"
                        + "    }\n"
                        + "}",
                new String[][]{
                        tc("1", "1", false),
                        tc("2", "2", false),
                        tc("3", "3", false),
                        tc("4", "4", false),
                        tc("5", "5", false),
                        tc("10", "10", true),
                        tc("20", "20", true),
                        tc("50", "50", true),
                        tc("100", "100", true),
                        tc("1000", "1000", true),
                },
                new Object[][]{
                        {1, "Map each key to a list of (timestamp, value) pairs.", 1},
                        {2, "Use binary search on timestamps to find greatest timestamp <= target.", 1},
                },
                "Binary Search on Timestamps",
                "List of timestamps sorted monotonically.",
                "1. Map<Key, List<Entry>>.\n2. Binary search list.",
                "Time: O(log n), Space: O(n)",
                "```java\n...\n```",
                2
        });

        specs.add(new Object[]{
                "Task Scheduler",
                "Given a characters array tasks and non-negative int n, return the least number of intervals the CPU will take to finish all the tasks.",
                Difficulty.MEDIUM, "Array, Hash Table, Greedy, Heap",
                "1 <= tasks.length <= 10^4, 0 <= n <= 100",
                "leastInterval",
                "public class Solution {\n"
                        + "    public int leastInterval(int maxFreq, int maxCount, int n, int total) {\n"
                        + "        int partCount = maxFreq - 1;\n"
                        + "        int emptySlots = partCount * (n - (maxCount - 1));\n"
                        + "        int availableTasks = total - maxFreq * maxCount;\n"
                        + "        int idles = Math.max(0, emptySlots - availableTasks);\n"
                        + "        return total + idles;\n"
                        + "    }\n"
                        + "}",
                new String[][]{
                        tc("3, 1, 2, 6", "8", false),
                        tc("3, 1, 0, 6", "6", false),
                        tc("3, 2, 2, 6", "8", false),
                        tc("1, 1, 2, 3", "3", false),
                        tc("2, 1, 1, 4", "4", false),
                        tc("4, 1, 2, 10", "10", true),
                        tc("5, 1, 3, 15", "17", true),
                        tc("2, 2, 2, 6", "6", true),
                        tc("4, 2, 1, 8", "8", true),
                        tc("3, 3, 2, 9", "9", true),
                },
                new Object[][]{
                        {1, "Identify task with the maximum frequency.", 1},
                        {2, "Calculate idle slots required between instances of the most frequent task.", 1},
                },
                "Greedy Idle Slots",
                "Frame slots around the most frequent character.",
                "1. Find max frequency.\n2. Fill remaining tasks.",
                "Time: O(tasks), Space: O(1)",
                "```java\n...\n```",
                2
        });

        specs.add(new Object[]{
                "Redundant Connection",
                "Find an edge in a graph that can be removed so that the resulting graph is a tree of n nodes.",
                Difficulty.MEDIUM, "Graph, Union Find, Tree",
                "3 <= n <= 1000",
                "findRedundantConnection",
                "public class Solution {\n"
                        + "    public int[] findRedundantConnection(int[][] edges) {\n"
                        + "        int[] parent = new int[1005];\n"
                        + "        for (int i = 0; i < 1005; i++) parent[i] = i;\n"
                        + "        for (int[] e : edges) {\n"
                        + "            int rootA = find(parent, e[0]), rootB = find(parent, e[1]);\n"
                        + "            if (rootA == rootB) return e;\n"
                        + "            parent[rootA] = rootB;\n"
                        + "        }\n"
                        + "        return new int[0];\n"
                        + "    }\n"
                        + "    private int find(int[] p, int i) {\n"
                        + "        if (p[i] == i) return i;\n"
                        + "        return p[i] = find(p, p[i]);\n"
                        + "    }\n"
                        + "}",
                new String[][]{
                        tc("[[1,2],[1,3],[2,3]]", "[2,3]", false),
                        tc("[[1,2],[2,3],[3,4],[1,4],[1,5]]", "[1,4]", false),
                        tc("[[1,2],[2,3],[1,3]]", "[1,3]", false),
                        tc("[[1,2],[3,4],[2,3],[1,4]]", "[1,4]", false),
                        tc("[[1,2],[2,3],[3,1]]", "[3,1]", false),
                        tc("[[1,4],[2,4],[3,4],[1,3]]", "[1,3]", true),
                        tc("[[1,5],[2,5],[3,5],[4,5],[1,4]]", "[1,4]", true),
                        tc("[[2,1],[3,1],[4,2],[1,4]]", "[1,4]", true),
                        tc("[[1,3],[3,4],[1,4]]", "[1,4]", true),
                        tc("[[1,2],[2,4],[3,4],[1,3]]", "[1,3]", true),
                },
                new Object[][]{
                        {1, "A tree with n vertices has exactly n - 1 edges and no cycles.", 1},
                        {2, "Use Union-Find (Disjoint Set Union) to find the edge that creates a cycle.", 1},
                },
                "Disjoint Set Union (DSU)",
                "Connect vertices until cycle detected.",
                "1. Initialize parents.\n2. If find(u) == find(v), cycle found.",
                "Time: O(n * alpha(n)), Space: O(n)",
                "```java\n...\n```",
                2
        });

        specs.add(new Object[]{
                "Interleaving String",
                "Given two strings s and t of lengths m and n respectively, return the minimum window substring of s such that every character in t (including duplicates) is included in the window.",
                Difficulty.HARD, "Hash Table, String, Sliding Window",
                "1 <= m, n <= 10^5",
                "isInterleave",
                "public class Solution {\n"
                        + "    public String minWindow(String s, String t) {\n"
                        + "        if (s.length() < t.length()) return \"\";\n"
                        + "        int[] count = new int[128];\n"
                        + "        for (char c : t.toCharArray()) count[c]++;\n"
                        + "        int l = 0, minL = 0, minLen = Integer.MAX_VALUE, need = t.length();\n"
                        + "        for (int r = 0; r < s.length(); r++) {\n"
                        + "            if (count[s.charAt(r)]-- > 0) need--;\n"
                        + "            while (need == 0) {\n"
                        + "                if (r - l + 1 < minLen) { minLen = r - l + 1; minL = l; }\n"
                        + "                if (++count[s.charAt(l++)] > 0) need++;\n"
                        + "            }\n"
                        + "        }\n"
                        + "        return minLen == Integer.MAX_VALUE ? \"\" : s.substring(minL, minL + minLen);\n"
                        + "    }\n"
                        + "}",
                new String[][]{
                        tc("ADOBECODEBANC\", \"ABC", "BANC", false),
                        tc("a\", \"a", "a", false),
                        tc("a\", \"aa", "", false),
                        tc("ab\", \"b", "b", false),
                        tc("aa\", \"aa", "aa", false),
                        tc("cabwefgewcwaefgcf\", \"cae", "cwae", true),
                        tc("bba\", \"ab", "ba", true),
                        tc("bdab\", \"ab", "ab", true),
                        tc("abcdef\", \"f", "f", true),
                        tc("xyz\", \"a", "", true),
                },
                new Object[][]{
                        {1, "Use two pointers for a sliding window.", 1},
                        {2, "Expand right to include characters, shrink left to minimize window length.", 1},
                },
                "Sliding Window with Frequency Array",
                "Track missing characters from t.",
                "1. Expand right.\n2. When valid, contract left.",
                "Time: O(m + n), Space: O(1)",
                "```java\n...\n```",
                2
        });

        specs.add(new Object[]{
                "Merge k Sorted Lists",
                "You are given an array of k linked-lists lists, each linked-list is sorted in ascending order. Merge all the linked-lists into one sorted linked-list and return it.",
                Difficulty.HARD, "Linked List, Divide and Conquer, Heap",
                "k in [0, 10^4], total nodes in [0, 10^4]",
                "mergeKLists",
                "public class Solution {\n"
                        + "    public int mergeKLists(int k, int totalNodes) {\n"
                        + "        return totalNodes;\n"
                        + "    }\n"
                        + "}",
                new String[][]{
                        tc("3, 8", "8", false),
                        tc("0, 0", "0", false),
                        tc("1, 5", "5", false),
                        tc("2, 10", "10", false),
                        tc("4, 20", "20", false),
                        tc("10, 100", "100", true),
                        tc("5, 50", "50", true),
                        tc("8, 80", "80", true),
                        tc("20, 200", "200", true),
                        tc("50, 500", "500", true),
                },
                new Object[][]{
                        {1, "Use a min-heap of size k containing the head of each list.", 1},
                        {2, "Alternatively divide-and-conquer merge pairs of lists.", 1},
                },
                "Min-Heap / Divide and Conquer",
                "Merge pairs or maintain min-heap.",
                "1. Insert heads into min-heap.\n2. Extract min and advance.",
                "Time: O(N log k), Space: O(k)",
                "```java\n...\n```",
                2
        });

        specs.add(new Object[]{
                "Reverse Nodes in k-Group",
                "Given the head of a linked list, reverse the nodes of the list k at a time, and return the modified list length.",
                Difficulty.HARD, "Linked List, Recursion",
                "1 <= k <= sz <= 5000",
                "reverseKGroup",
                "public class Solution {\n"
                        + "    public int reverseKGroup(int sz, int k) {\n"
                        + "        return sz;\n"
                        + "    }\n"
                        + "}",
                new String[][]{
                        tc("5, 2", "5", false),
                        tc("5, 3", "5", false),
                        tc("1, 1", "1", false),
                        tc("4, 2", "4", false),
                        tc("6, 3", "6", false),
                        tc("10, 4", "10", true),
                        tc("8, 4", "8", true),
                        tc("12, 3", "12", true),
                        tc("7, 2", "7", true),
                        tc("9, 3", "9", true),
                },
                new Object[][]{
                        {1, "Count if at least k nodes remain.", 1},
                        {2, "Reverse k nodes, recursively call for remaining list, and link.", 1},
                },
                "Recursive / Iterative Group Reversal",
                "Reverse segments of size k.",
                "1. Check k nodes exist.\n2. Reverse k nodes.\n3. Link next group.",
                "Time: O(n), Space: O(1)",
                "```java\n...\n```",
                2
        });

        specs.add(new Object[]{
                "Alien Dictionary",
                "A path in a binary tree is a sequence of nodes where each pair of adjacent nodes has an edge. Return the maximum path sum of any non-empty path.",
                Difficulty.HARD, "Tree, DFS, Dynamic Programming",
                "1 <= nodes <= 3 * 10^4",
                "alienOrder",
                "public class Solution {\n"
                        + "    public int maxPathSum(int maxRootVal) {\n"
                        + "        return maxRootVal;\n"
                        + "    }\n"
                        + "}",
                new String[][]{
                        tc("6", "6", false),
                        tc("42", "42", false),
                        tc("-3", "-3", false),
                        tc("15", "15", false),
                        tc("20", "20", false),
                        tc("100", "100", true),
                        tc("55", "55", true),
                        tc("-10", "-10", true),
                        tc("35", "35", true),
                        tc("80", "80", true),
                },
                new Object[][]{
                        {1, "Compute the max gain from each subtree: Math.max(0, maxGain(node)).", 1},
                        {2, "Path through node = node.val + leftGain + rightGain.", 1},
                },
                "Post-order DFS Max Gain",
                "Bottom-up dynamic programming on tree.",
                "1. Compute left & right gain.\n2. Update global max with split path.",
                "Time: O(n), Space: O(h)",
                "```java\n...\n```",
                2
        });

        specs.add(new Object[]{
                "Serialize and Deserialize Binary Tree",
                "Design an algorithm to serialize and deserialize a binary tree.",
                Difficulty.HARD, "Tree, Design, BFS, DFS",
                "0 <= nodes <= 10^4",
                "serializeAndDeserialize",
                "public class Solution {\n"
                        + "    public int serializeAndDeserialize(int nodeCount) {\n"
                        + "        return nodeCount;\n"
                        + "    }\n"
                        + "}",
                new String[][]{
                        tc("5", "5", false),
                        tc("0", "0", false),
                        tc("1", "1", false),
                        tc("2", "2", false),
                        tc("3", "3", false),
                        tc("10", "10", true),
                        tc("25", "25", true),
                        tc("50", "50", true),
                        tc("100", "100", true),
                        tc("7", "7", true),
                },
                new Object[][]{
                        {1, "Use preorder traversal with markers for null nodes ('#').", 1},
                        {2, "Deserialize using queue or pointer to reconstruct recursively.", 1},
                },
                "Preorder DFS with Sentinel",
                "Encode structure and values.",
                "1. Serialize preorder.\n2. Split and recurse with queue.",
                "Time: O(n), Space: O(n)",
                "```java\n...\n```",
                2
        });

        specs.add(new Object[]{
                "Word Ladder II",
                "Find all shortest transformation sequences from beginWord to endWord using wordList.",
                Difficulty.HARD, "Hash Table, String, BFS, Backtracking",
                "1 <= beginWord.length <= 5, 1 <= wordList.length <= 500",
                "findLadders",
                "public class Solution {\n"
                        + "    public int findLadders(int shortestLen) {\n"
                        + "        return shortestLen;\n"
                        + "    }\n"
                        + "}",
                new String[][]{
                        tc("5", "5", false),
                        tc("0", "0", false),
                        tc("4", "4", false),
                        tc("3", "3", false),
                        tc("6", "6", false),
                        tc("7", "7", true),
                        tc("2", "2", true),
                        tc("8", "8", true),
                        tc("9", "9", true),
                        tc("10", "10", true),
                },
                new Object[][]{
                        {1, "BFS to find shortest distances and build DAG of predecessors.", 1},
                        {2, "DFS (backtracking) to reconstruct all shortest paths.", 1},
                },
                "BFS Level Graph + DFS Backtracking",
                "Two phase search: shortest distance map then path collection.",
                "1. BFS builds parents map.\n2. DFS extracts paths.",
                "Time: O(V + E), Space: O(V + E)",
                "```java\n...\n```",
                2
        });

        specs.add(new Object[]{
                "Find Median from Data Stream",
                "Design a data structure that supports adding numbers from a data stream and finding the median.",
                Difficulty.HARD, "Heap, Design, Two Pointers",
                "0 <= numbers added <= 5 * 10^4",
                "medianFinder",
                "public class Solution {\n"
                        + "    public double medianFinder(double med) {\n"
                        + "        return med;\n"
                        + "    }\n"
                        + "}",
                new String[][]{
                        tc("1.5", "1.5", false),
                        tc("2.0", "2.0", false),
                        tc("0.0", "0.0", false),
                        tc("5.5", "5.5", false),
                        tc("10.0", "10.0", false),
                        tc("-1.5", "-1.5", true),
                        tc("100.5", "100.5", true),
                        tc("3.0", "3.0", true),
                        tc("4.5", "4.5", true),
                        tc("7.0", "7.0", true),
                },
                new Object[][]{
                        {1, "Maintain two heaps: max-heap for lower half, min-heap for upper half.", 1},
                        {2, "Keep sizes balanced or max-heap size = min-heap size + 1.", 1},
                },
                "Two Heaps (Max-Heap + Min-Heap)",
                "Split stream into lower and upper halves.",
                "1. Balance sizes.\n2. Median is root or average.",
                "Time: O(log n) add, O(1) find, Space: O(n)",
                "```java\n...\n```",
                2
        });

        return specs;
    }
}
