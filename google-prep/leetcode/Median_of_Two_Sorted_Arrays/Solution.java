class Solution {
    public double findMedianSortedArrays(int[] nums1, int[] nums2) {
        // always binary search on the smaller array for efficiency
        if (nums1.length > nums2.length) {
            return findMedianSortedArrays(nums2, nums1);
        }

        int m = nums1.length, n = nums2.length;
        int lo = 0, hi = m;
        // half = number of elements that should be on the LEFT side of the median
        int half = (m + n + 1) / 2;

        while (lo <= hi) {
            int i = (lo + hi) / 2;   // partition point in nums1
            int j = half - i;         // partition point in nums2

            // values just left and right of each partition
            // use -INF/+INF for out-of-bounds edges
            int nums1Left  = (i == 0) ? Integer.MIN_VALUE : nums1[i - 1];
            int nums1Right = (i == m) ? Integer.MAX_VALUE : nums1[i];
            int nums2Left  = (j == 0) ? Integer.MIN_VALUE : nums2[j - 1];
            int nums2Right = (j == n) ? Integer.MAX_VALUE : nums2[j];

            if (nums1Left <= nums2Right && nums2Left <= nums1Right) {
                // correct partition found
                if ((m + n) % 2 == 1) {
                    // odd total: median is max of left halves
                    return Math.max(nums1Left, nums2Left);
                } else {
                    // even total: median is avg of max-left and min-right
                    return (Math.max(nums1Left, nums2Left) + Math.min(nums1Right, nums2Right)) / 2.0;
                }
            } else if (nums1Left > nums2Right) {
                hi = i - 1;  // too far right in nums1, move left
            } else {
                lo = i + 1;  // too far left in nums1, move right
            }
        }
        return 0.0;
    }
}

/*
CORE IDEA
----------
Instead of merging (O(m+n)), binary search for the correct "partition" in both arrays
such that everything on the left <= everything on the right.

Imagine splitting both arrays into left and right halves:

    nums1: [1, 3 | 5, 7]     <- partition after index i
    nums2: [2, 4 | 6, 8]     <- partition after index j

Left side has (m+n)/2 elements total. If:
    max(left side) <= min(right side)
then the median is between those two values.

WALKTHROUGH: nums1=[1,3], nums2=[2]
------------------------------------
m=2, n=1, half=(2+1+1)/2=2

lo=0, hi=2
  i = 1,  j = 2-1 = 1
  nums1Left=1, nums1Right=3
  nums2Left=2, nums2Right=+INF

  1 <= +INF  AND  2 <= 3  → correct partition!
  total=3 (odd) → return max(1, 2) = 2.0 ✓

WALKTHROUGH: nums1=[1,2], nums2=[3,4]
--------------------------------------
m=2, n=2, half=(2+2+1)/2=2

lo=0, hi=2
  i=1, j=1
  nums1Left=1, nums1Right=2
  nums2Left=3, nums2Right=4

  1 <= 4  AND  3 <= 2? NO → nums2Left > nums1Right → lo = 2

  i=2, j=0
  nums1Left=2, nums1Right=+INF
  nums2Left=-INF, nums2Right=3

  2 <= 3  AND  -INF <= +INF → correct partition!
  total=4 (even) → (max(2,-INF) + min(+INF,3)) / 2.0 = (2+3)/2.0 = 2.5 ✓


WHY O(log(m+n))?
-----------------
We binary search only on the smaller array (size m).
Each iteration halves the search space → O(log m) = O(log(m+n)).
*/