package com.oj.platform.config;

import com.oj.platform.entity.Difficulty;

import java.util.ArrayList;
import java.util.List;

/**
 * ExpandedProblemCatalog3 - Additional curated algorithmic problems.
 */
public class ExpandedProblemCatalog3 {

    private static String[] tc(String input, String expected, boolean hidden) {
        return new String[]{input, expected, hidden ? "true" : "false"};
    }

    public static List<Object[]> getSpecs() {
        List<Object[]> specs = new ArrayList<>();

        // 1. Rotate Array
        specs.add(new Object[]{
                "Rotate Array",
                "Given an integer array nums, rotate the array to the right by k steps, where k is non-negative.",
                Difficulty.MEDIUM, "Arrays, Two Pointers",
                "1 <= nums.length <= 10^5\n0 <= k <= 10^5",
                "rotate",
                "public class Solution {\n"
                        + "    public int[] rotate(int[] nums, int k) {\n"
                        + "        return nums;\n"
                        + "    }\n"
                        + "}",
                new String[][]{
                        tc("[1,2,3,4,5,6,7], 3", "[5,6,7,1,2,3,4]", false),
                        tc("[-1,-100,3,99], 2", "[3,99,-1,-100]", false),
                        tc("[1,2], 3", "[2,1]", false),
                        tc("[1], 0", "[1]", false),
                        tc("[1,2,3,4], 2", "[3,4,1,2]", false),
                        tc("[1,2,3,4,5], 1", "[5,1,2,3,4]", true),
                        tc("[1,2,3,4,5], 5", "[1,2,3,4,5]", true),
                        tc("[10,20,30,40], 4", "[10,20,30,40]", true),
                        tc("[5,10,15,20,25], 7", "[20,25,5,10,15]", true),
                        tc("[9,8,7,6], 1", "[6,9,8,7]", true),
                },
                new Object[][]{
                        {1, "Use k = k % n since rotating n times results in the same array.", 1},
                        {2, "Reverse the whole array, then reverse the first k elements, then reverse the remaining elements.", 2},
                },
                "Rotate Array: Reverse Method",
                "Reverse the array and subarrays to rotate in-place with O(1) extra space.",
                "1. k = k % n.\n2. Reverse entire array.\n3. Reverse first k elements.\n4. Reverse remaining n - k elements.",
                "Time: O(n), Space: O(1)",
                "```java\n"
                        + "public class Solution {\n"
                        + "    public int[] rotate(int[] nums, int k) {\n"
                        + "        int n = nums.length;\n"
                        + "        k %= n;\n"
                        + "        reverse(nums, 0, n - 1);\n"
                        + "        reverse(nums, 0, k - 1);\n"
                        + "        reverse(nums, k, n - 1);\n"
                        + "        return nums;\n"
                        + "    }\n"
                        + "    private void reverse(int[] nums, int start, int end) {\n"
                        + "        while (start < end) {\n"
                        + "            int temp = nums[start];\n"
                        + "            nums[start] = nums[end];\n"
                        + "            nums[end] = temp;\n"
                        + "            start++;\n"
                        + "            end--;\n"
                        + "        }\n"
                        + "    }\n"
                        + "}\n"
                        + "```",
                3
        });

        // 2. First Unique Character in a String
        specs.add(new Object[]{
                "First Unique Character in a String",
                "Given a string s, find the first non-repeating character in it and return its index. If it does not exist, return -1.",
                Difficulty.EASY, "String, Hash Table, Counting",
                "1 <= s.length <= 10^5\ns consists of only lowercase English letters.",
                "firstUniqChar",
                "public class Solution {\n"
                        + "    public int firstUniqChar(String s) {\n"
                        + "        return -1;\n"
                        + "    }\n"
                        + "}",
                new String[][]{
                        tc("\"leetcode\"", "0", false),
                        tc("\"loveleetcode\"", "2", false),
                        tc("\"aabb\"", "-1", false),
                        tc("\"a\"", "0", false),
                        tc("\"ab\"", "0", false),
                        tc("\"ba\"", "0", true),
                        tc("\"zxyzx\"", "1", true),
                        tc("\"abcdef\"", "0", true),
                        tc("\"aabbccd\"", "6", true),
                        tc("\"dddccbba\"", "7", true),
                },
                new Object[][]{
                        {1, "Count frequency of each character using an integer array of size 26.", 1},
                        {2, "Iterate through the string again to find the first index with count equal to 1.", 1},
                },
                "First Unique Character: Frequency Array",
                "Two-pass frequency counting using a fixed-size 26 array.",
                "1. Initialize count[26].\n2. First pass: count[c - 'a']++.\n3. Second pass: return first index i where count[s.charAt(i) - 'a'] == 1.\n4. If none found, return -1.",
                "Time: O(n), Space: O(1)",
                "```java\n"
                        + "public class Solution {\n"
                        + "    public int firstUniqChar(String s) {\n"
                        + "        int[] count = new int[26];\n"
                        + "        for (int i = 0; i < s.length(); i++) {\n"
                        + "            count[s.charAt(i) - 'a']++;\n"
                        + "        }\n"
                        + "        for (int i = 0; i < s.length(); i++) {\n"
                        + "            if (count[s.charAt(i) - 'a'] == 1) return i;\n"
                        + "        }\n"
                        + "        return -1;\n"
                        + "    }\n"
                        + "}\n"
                        + "```",
                1
        });

        // 3. Product of Array Except Self
        specs.add(new Object[]{
                "Product of Array Except Self",
                "Given an integer array nums, return an array answer such that answer[i] is equal to the product of all the elements of nums except nums[i]. Do not use the division operation and solve in O(n) time.",
                Difficulty.MEDIUM, "Arrays, Prefix Sum",
                "2 <= nums.length <= 10^5\n-30 <= nums[i] <= 30",
                "productExceptSelf",
                "public class Solution {\n"
                        + "    public int[] productExceptSelf(int[] nums) {\n"
                        + "        return new int[0];\n"
                        + "    }\n"
                        + "}",
                new String[][]{
                        tc("[1,2,3,4]", "[24,12,8,6]", false),
                        tc("[-1,1,0,-3,3]", "[0,0,9,0,0]", false),
                        tc("[2,3]", "[3,2]", false),
                        tc("[1,1,1,1]", "[1,1,1,1]", false),
                        tc("[0,0]", "[0,0]", false),
                        tc("[1,2,3]", "[6,3,2]", true),
                        tc("[4,5,1,8,2]", "[80,64,320,40,160]", true),
                        tc("[1,0,3,4]", "[0,12,0,0]", true),
                        tc("[-2,3,-4,5]", "[-60,40,-30,24]", true),
                        tc("[2,2,2,2]", "[8,8,8,8]", true),
                },
                new Object[][]{
                        {1, "Calculate prefix products left-to-right into the answer array.", 1},
                        {2, "Multiply with suffix products in a second right-to-left pass using a running variable.", 2},
                },
                "Product Except Self: Prefix & Suffix Products",
                "Use the output array for prefix products and a running variable for suffixes.",
                "1. ans[0] = 1, ans[i] = ans[i-1] * nums[i-1].\n2. suffix = 1, iterate backwards: ans[i] *= suffix, suffix *= nums[i].",
                "Time: O(n), Space: O(1) extra space",
                "```java\n"
                        + "public class Solution {\n"
                        + "    public int[] productExceptSelf(int[] nums) {\n"
                        + "        int n = nums.length;\n"
                        + "        int[] ans = new int[n];\n"
                        + "        ans[0] = 1;\n"
                        + "        for (int i = 1; i < n; i++) {\n"
                        + "            ans[i] = ans[i - 1] * nums[i - 1];\n"
                        + "        }\n"
                        + "        int suffix = 1;\n"
                        + "        for (int i = n - 1; i >= 0; i--) {\n"
                        + "            ans[i] *= suffix;\n"
                        + "            suffix *= nums[i];\n"
                        + "        }\n"
                        + "        return ans;\n"
                        + "    }\n"
                        + "}\n"
                        + "```",
                2
        });

        // 4. Majority Element
        specs.add(new Object[]{
                "Majority Element",
                "Given an array nums of size n, return the majority element. The majority element is the element that appears more than ⌊n / 2⌋ times.",
                Difficulty.EASY, "Arrays, Counting, Divide and Conquer",
                "1 <= nums.length <= 5 * 10^4\n-10^9 <= nums[i] <= 10^9",
                "majorityElement",
                "public class Solution {\n"
                        + "    public int majorityElement(int[] nums) {\n"
                        + "        return 0;\n"
                        + "    }\n"
                        + "}",
                new String[][]{
                        tc("[3,2,3]", "3", false),
                        tc("[2,2,1,1,1,2,2]", "2", false),
                        tc("[1]", "1", false),
                        tc("[6,5,5]", "5", false),
                        tc("[10,10,20]", "10", false),
                        tc("[1,1,1,2,3,1,1]", "1", true),
                        tc("[-1,-1,2,-1]", "-1", true),
                        tc("[7,7,7,7,8,8,7]", "7", true),
                        tc("[4,4,4,5,4]", "4", true),
                        tc("[99,99,99,100,99]", "99", true),
                },
                new Object[][]{
                        {1, "Boyer-Moore Voting Algorithm allows O(n) time and O(1) space.", 1},
                        {2, "Maintain a candidate and count. Increment count for matching candidate, decrement otherwise.", 1},
                },
                "Majority Element: Boyer-Moore Voting",
                "Track current candidate and cancel out opposing elements.",
                "1. candidate = 0, count = 0.\n2. For each x: if count == 0, candidate = x; count += (x == candidate ? 1 : -1).\n3. Return candidate.",
                "Time: O(n), Space: O(1)",
                "```java\n"
                        + "public class Solution {\n"
                        + "    public int majorityElement(int[] nums) {\n"
                        + "        int candidate = 0, count = 0;\n"
                        + "        for (int num : nums) {\n"
                        + "            if (count == 0) {\n"
                        + "                candidate = num;\n"
                        + "            }\n"
                        + "            count += (num == candidate) ? 1 : -1;\n"
                        + "        }\n"
                        + "        return candidate;\n"
                        + "    }\n"
                        + "}\n"
                        + "```",
                1
        });

        // 5. Move Zeroes
        specs.add(new Object[]{
                "Move Zeroes",
                "Given an integer array nums, move all 0's to the end of it while maintaining the relative order of the non-zero elements.",
                Difficulty.EASY, "Arrays, Two Pointers",
                "1 <= nums.length <= 10^4\n-2^31 <= nums[i] <= 2^31 - 1",
                "moveZeroes",
                "public class Solution {\n"
                        + "    public int[] moveZeroes(int[] nums) {\n"
                        + "        return nums;\n"
                        + "    }\n"
                        + "}",
                new String[][]{
                        tc("[0,1,0,3,12]", "[1,3,12,0,0]", false),
                        tc("[0]", "[0]", false),
                        tc("[1,0]", "[1,0]", false),
                        tc("[0,1]", "[1,0]", false),
                        tc("[1,2,3]", "[1,2,3]", false),
                        tc("[0,0,1]", "[1,0,0]", true),
                        tc("[4,2,4,0,0,3,0,5,1,0]", "[4,2,4,3,5,1,0,0,0,0]", true),
                        tc("[0,0,0,0]", "[0,0,0,0]", true),
                        tc("[10,0,20,0,30]", "[10,20,30,0,0]", true),
                        tc("[-1,0,-2,0,-3]", "[-1,-2,-3,0,0]", true),
                },
                new Object[][]{
                        {1, "Use a pointer to place non-zero elements sequentially at the front.", 1},
                        {2, "Fill the remaining indices with zeroes.", 1},
                },
                "Move Zeroes: Two Pointers",
                "Compact non-zero elements to the left, zero-fill the rest.",
                "1. insertPos = 0.\n2. For each num in nums: if num != 0, nums[insertPos++] = num.\n3. While insertPos < n, nums[insertPos++] = 0.",
                "Time: O(n), Space: O(1)",
                "```java\n"
                        + "public class Solution {\n"
                        + "    public int[] moveZeroes(int[] nums) {\n"
                        + "        int pos = 0;\n"
                        + "        for (int num : nums) {\n"
                        + "            if (num != 0) nums[pos++] = num;\n"
                        + "        }\n"
                        + "        while (pos < nums.length) {\n"
                        + "            nums[pos++] = 0;\n"
                        + "        }\n"
                        + "        return nums;\n"
                        + "    }\n"
                        + "}\n"
                        + "```",
                1
        });

        // 6. Find Minimum in Rotated Sorted Array
        specs.add(new Object[]{
                "Find Minimum in Rotated Sorted Array",
                "Suppose an array of length n sorted in ascending order is rotated between 1 and n times. Given the sorted rotated array nums of unique elements, return the minimum element of this array in O(log n) time.",
                Difficulty.MEDIUM, "Binary Search, Arrays",
                "n == nums.length\n1 <= n <= 5000\n-5000 <= nums[i] <= 5000\nAll integers in nums are unique.",
                "findMin",
                "public class Solution {\n"
                        + "    public int findMin(int[] nums) {\n"
                        + "        return 0;\n"
                        + "    }\n"
                        + "}",
                new String[][]{
                        tc("[3,4,5,1,2]", "1", false),
                        tc("[4,5,6,7,0,1,2]", "0", false),
                        tc("[11,13,15,17]", "11", false),
                        tc("[2,1]", "1", false),
                        tc("[1]", "1", false),
                        tc("[5,1,2,3,4]", "1", true),
                        tc("[3,1,2]", "1", true),
                        tc("[10,20,30,40,5]", "5", true),
                        tc("[20,30,40,50,60,10]", "10", true),
                        tc("[7,8,9,1,2,3,4,5,6]", "1", true),
                },
                new Object[][]{
                        {1, "Compare nums[mid] with nums[right].", 1},
                        {2, "If nums[mid] > nums[right], the minimum is in the right half (left = mid + 1). Otherwise right = mid.", 2},
                },
                "Find Minimum in Rotated Array: Binary Search",
                "Binary search for the inflection point.",
                "1. left = 0, right = n - 1.\n2. While left < right: mid = left + (right - left) / 2. If nums[mid] > nums[right], left = mid + 1; else right = mid.\n3. Return nums[left].",
                "Time: O(log n), Space: O(1)",
                "```java\n"
                        + "public class Solution {\n"
                        + "    public int findMin(int[] nums) {\n"
                        + "        int left = 0, right = nums.length - 1;\n"
                        + "        while (left < right) {\n"
                        + "            int mid = left + (right - left) / 2;\n"
                        + "            if (nums[mid] > nums[right]) {\n"
                        + "                left = mid + 1;\n"
                        + "            } else {\n"
                        + "                right = mid;\n"
                        + "            }\n"
                        + "        }\n"
                        + "        return nums[left];\n"
                        + "    }\n"
                        + "}\n"
                        + "```",
                2
        });

        // 7. Single Number
        specs.add(new Object[]{
                "Single Number",
                "Given a non-empty array of integers nums, every element appears twice except for one. Find that single one. Implement a solution with O(n) linear runtime and O(1) extra space.",
                Difficulty.EASY, "Bit Manipulation, Arrays",
                "1 <= nums.length <= 3 * 10^4\n-3 * 10^4 <= nums[i] <= 3 * 10^4\nEach element in nums appears twice except for one element.",
                "singleNumber",
                "public class Solution {\n"
                        + "    public int singleNumber(int[] nums) {\n"
                        + "        return 0;\n"
                        + "    }\n"
                        + "}",
                new String[][]{
                        tc("[2,2,1]", "1", false),
                        tc("[4,1,2,1,2]", "4", false),
                        tc("[1]", "1", false),
                        tc("[0,1,0]", "1", false),
                        tc("[-1,-1,-2]", "-2", false),
                        tc("[10,20,10,30,20]", "30", true),
                        tc("[99,88,99,77,88]", "77", true),
                        tc("[5,3,5]", "3", true),
                        tc("[100,200,300,100,200]", "300", true),
                        tc("[-5,10,-5]", "10", true),
                },
                new Object[][]{
                        {1, "XOR of any number with itself is 0 (a ^ a = 0).", 1},
                        {2, "XOR of any number with 0 is the number itself (a ^ 0 = a).", 1},
                },
                "Single Number: Bitwise XOR",
                "XOR all elements together; duplicates cancel out leaving the unique value.",
                "1. result = 0.\n2. For each num in nums: result ^= num.\n3. Return result.",
                "Time: O(n), Space: O(1)",
                "```java\n"
                        + "public class Solution {\n"
                        + "    public int singleNumber(int[] nums) {\n"
                        + "        int result = 0;\n"
                        + "        for (int num : nums) {\n"
                        + "            result ^= num;\n"
                        + "        }\n"
                        + "        return result;\n"
                        + "    }\n"
                        + "}\n"
                        + "```",
                1
        });

        // 8. Intersection of Two Arrays
        specs.add(new Object[]{
                "Intersection of Two Arrays",
                "Given two integer arrays nums1 and nums2, return an array of their intersection. Each element in the result must be unique and you may return the result in any order.",
                Difficulty.EASY, "Arrays, Hash Table, Two Pointers",
                "1 <= nums1.length, nums2.length <= 1000\n0 <= nums1[i], nums2[i] <= 1000",
                "intersection",
                "public class Solution {\n"
                        + "    public int[] intersection(int[] nums1, int[] nums2) {\n"
                        + "        return new int[0];\n"
                        + "    }\n"
                        + "}",
                new String[][]{
                        tc("[1,2,2,1], [2,2]", "[2]", false),
                        tc("[4,9,5], [9,4,9,8,4]", "[4,9]", false),
                        tc("[1], [1]", "[1]", false),
                        tc("[1,2], [3,4]", "[]", false),
                        tc("[5,5,5], [5,5]", "[5]", false),
                        tc("[1,2,3], [2,3,4]", "[2,3]", true),
                        tc("[10,20], [20,30]", "[20]", true),
                        tc("[7,8,9], [9,8,7]", "[7,8,9]", true),
                        tc("[100], [200]", "[]", true),
                        tc("[1,3,5,7], [3,7,9]", "[3,7]", true),
                },
                new Object[][]{
                        {1, "Store all elements of nums1 into a HashSet.", 1},
                        {2, "Iterate over nums2 and add to resultSet if present in set1.", 1},
                },
                "Intersection of Arrays: HashSet",
                "Collect common unique elements using sets.",
                "1. set1 = new HashSet(nums1).\n2. intersect = new HashSet.\n3. For num in nums2: if set1.contains(num) intersect.add(num).\n4. Convert intersect to int[].",
                "Time: O(n + m), Space: O(n)",
                "```java\n"
                        + "import java.util.*;\n"
                        + "public class Solution {\n"
                        + "    public int[] intersection(int[] nums1, int[] nums2) {\n"
                        + "        Set<Integer> set1 = new HashSet<>();\n"
                        + "        for (int n : nums1) set1.add(n);\n"
                        + "        Set<Integer> resSet = new HashSet<>();\n"
                        + "        for (int n : nums2) {\n"
                        + "            if (set1.contains(n)) resSet.add(n);\n"
                        + "        }\n"
                        + "        int[] res = new int[resSet.size()];\n"
                        + "        int i = 0;\n"
                        + "        for (int n : resSet) res[i++] = n;\n"
                        + "        return res;\n"
                        + "    }\n"
                        + "}\n"
                        + "```",
                1
        });

        // 9. Daily Temperatures
        specs.add(new Object[]{
                "Daily Temperatures",
                "Given an array of integers temperatures represents the daily temperatures, return an array answer such that answer[i] is the number of days you have to wait after the ith day to get a warmer temperature. If there is no future day for which this is possible, keep answer[i] == 0 instead.",
                Difficulty.MEDIUM, "Stack, Monotonic Stack, Arrays",
                "1 <= temperatures.length <= 10^5\n30 <= temperatures[i] <= 100",
                "dailyTemperatures",
                "public class Solution {\n"
                        + "    public int[] dailyTemperatures(int[] temperatures) {\n"
                        + "        return new int[0];\n"
                        + "    }\n"
                        + "}",
                new String[][]{
                        tc("[73,74,75,71,69,72,76,73]", "[1,1,4,2,1,1,0,0]", false),
                        tc("[30,40,50,60]", "[1,1,1,0]", false),
                        tc("[30,60,90]", "[1,1,0]", false),
                        tc("[90,80,70]", "[0,0,0]", false),
                        tc("[50]", "[0]", false),
                        tc("[70,71,70,72]", "[1,2,1,0]", true),
                        tc("[40,50,40,50]", "[1,0,1,0]", true),
                        tc("[60,60,60]", "[0,0,0]", true),
                        tc("[35,45,35,45,55]", "[1,3,1,1,0]", true),
                        tc("[80,75,85]", "[2,1,0]", true),
                },
                new Object[][]{
                        {1, "Maintain a monotonic decreasing stack of day indices.", 1},
                        {2, "When encountering a warmer temperature, pop indices and calculate distance: i - prevIndex.", 2},
                },
                "Daily Temperatures: Monotonic Stack",
                "Stack stores indices of unresolved cooler days.",
                "1. ans = new int[n], stack = ArrayDeque.\n2. For i = 0..n-1: while stack not empty and temp[i] > temp[stack.peek()]: prev = stack.pop(), ans[prev] = i - prev.\n3. stack.push(i).\n4. Return ans.",
                "Time: O(n), Space: O(n)",
                "```java\n"
                        + "import java.util.*;\n"
                        + "public class Solution {\n"
                        + "    public int[] dailyTemperatures(int[] temperatures) {\n"
                        + "        int n = temperatures.length;\n"
                        + "        int[] ans = new int[n];\n"
                        + "        Deque<Integer> stack = new ArrayDeque<>();\n"
                        + "        for (int i = 0; i < n; i++) {\n"
                        + "            while (!stack.isEmpty() && temperatures[i] > temperatures[stack.peek()]) {\n"
                        + "                int prev = stack.pop();\n"
                        + "                ans[prev] = i - prev;\n"
                        + "            }\n"
                        + "            stack.push(i);\n"
                        + "        }\n"
                        + "        return ans;\n"
                        + "    }\n"
                        + "}\n"
                        + "```",
                2
        });

        // 10. Peak Index in a Mountain Array
        specs.add(new Object[]{
                "Peak Index in a Mountain Array",
                "An array arr is a mountain if arr.length >= 3 and there exists some i with 0 < i < arr.length - 1 such that arr[0] < arr[1] < ... < arr[i - 1] < arr[i] > arr[i + 1] > ... > arr[arr.length - 1]. Given a mountain array arr, return the index i such that arr[0] < arr[1] < ... < arr[i - 1] < arr[i] > arr[i + 1] > ... > arr[arr.length - 1] in O(log n) time.",
                Difficulty.MEDIUM, "Binary Search, Arrays",
                "3 <= arr.length <= 10^5\n0 <= arr[i] <= 10^6\narr is guaranteed to be a mountain array.",
                "peakIndexInMountainArray",
                "public class Solution {\n"
                        + "    public int peakIndexInMountainArray(int[] arr) {\n"
                        + "        return 0;\n"
                        + "    }\n"
                        + "}",
                new String[][]{
                        tc("[0,1,0]", "1", false),
                        tc("[0,2,1,0]", "1", false),
                        tc("[0,10,5,2]", "1", false),
                        tc("[3,4,5,1]", "2", false),
                        tc("[24,69,100,99,79,78,67,36,26,19]", "2", false),
                        tc("[1,3,5,4,2]", "2", true),
                        tc("[10,20,30,40,50,25,10]", "4", true),
                        tc("[0,5,10,2]", "2", true),
                        tc("[100,200,300,400,350,150]", "3", true),
                        tc("[2,8,12,20,15,9,1]", "3", true),
                },
                new Object[][]{
                        {1, "Compare arr[mid] with arr[mid + 1].", 1},
                        {2, "If arr[mid] < arr[mid + 1], you are on the ascending slope (left = mid + 1); otherwise right = mid.", 2},
                },
                "Mountain Array Peak: Binary Search",
                "Binary search for the local maximum.",
                "1. left = 0, right = n - 1.\n2. While left < right: mid = left + (right - left) / 2. If arr[mid] < arr[mid + 1] left = mid + 1 else right = mid.\n3. Return left.",
                "Time: O(log n), Space: O(1)",
                "```java\n"
                        + "public class Solution {\n"
                        + "    public int peakIndexInMountainArray(int[] arr) {\n"
                        + "        int left = 0, right = arr.length - 1;\n"
                        + "        while (left < right) {\n"
                        + "            int mid = left + (right - left) / 2;\n"
                        + "            if (arr[mid] < arr[mid + 1]) {\n"
                        + "                left = mid + 1;\n"
                        + "            } else {\n"
                        + "                right = mid;\n"
                        + "            }\n"
                        + "        }\n"
                        + "        return left;\n"
                        + "    }\n"
                        + "}\n"
                        + "```",
                2
        });

        return specs;
    }
}
