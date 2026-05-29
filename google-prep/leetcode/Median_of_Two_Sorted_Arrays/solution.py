from typing import List

class Solution:
    def findMedianSortedArrays(self, nums1: List[int], nums2: List[int]) -> float:
        if len(nums1) > len(nums2):
            return self.findMedianSortedArrays(nums2, nums1)

        m, n = len(nums1), len(nums2)
        lo, hi = 0, m
        half = (m + n + 1) // 2

        while lo <= hi:
            i = (lo + hi) // 2
            j = half - i

            nums1_left  = float('-inf') if i == 0 else nums1[i - 1]
            nums1_right = float('inf')  if i == m else nums1[i]
            nums2_left  = float('-inf') if j == 0 else nums2[j - 1]
            nums2_right = float('inf')  if j == n else nums2[j]

            if nums1_left <= nums2_right and nums2_left <= nums1_right:
                if (m + n) % 2 == 1:
                    return max(nums1_left, nums2_left)
                else:
                    return (max(nums1_left, nums2_left) + min(nums1_right, nums2_right)) / 2.0
            elif nums1_left > nums2_right:
                hi = i - 1
            else:
                lo = i + 1

        return 0.0


"""
CORE IDEA
----------
Binary search for the correct "partition" in both arrays such that
everything on the left <= everything on the right. No merging needed.

    nums1: [1, 3 | 5, 7]   <- partition after index i
    nums2: [2, 4 | 6, 8]   <- partition after index j

    i + j = half = (m+n+1)//2   (left side always has ceil of total elements)

A valid partition means:
    nums1_left <= nums2_right   AND   nums2_left <= nums1_right

WALKTHROUGH: nums1=[1,3], nums2=[2]   total=3 (odd)
----------------------------------------------------
m=2, n=1, half=(2+1+1)//2=2

lo=0, hi=2
  i=1, j=1
  nums1_left=1,  nums1_right=3
  nums2_left=2,  nums2_right=+inf

  1 <= +inf  AND  2 <= 3  → valid partition!
  odd total → return max(1, 2) = 2.0 ✓

WALKTHROUGH: nums1=[1,2], nums2=[3,4]   total=4 (even)
-------------------------------------------------------
m=2, n=2, half=(2+2+1)//2=2

lo=0, hi=2
  i=1, j=1
  nums1_left=1,  nums1_right=2
  nums2_left=3,  nums2_right=4

  1 <= 4  BUT  3 <= 2? NO → nums2_left > nums1_right → lo = 2

  i=2, j=0
  nums1_left=2,  nums1_right=+inf
  nums2_left=-inf, nums2_right=3

  2 <= 3  AND  -inf <= +inf → valid partition!
  even total → (max(2,-inf) + min(+inf,3)) / 2.0 = (2+3)/2.0 = 2.5 ✓


PYTHON vs JAVA DIFFERENCES
---------------------------
- float('-inf') / float('inf')     Java's  Integer.MIN_VALUE / Integer.MAX_VALUE
- (m + n + 1) // 2                 Java's  (m + n + 1) / 2   (// is integer division)
- len(nums1)                        Java's  nums1.length
- self.findMedianSortedArrays(...)  Java's  findMedianSortedArrays(...)  (needs self in Python)

COMPLEXITY
-----------
Time:  O(log(min(m,n)))  — binary search on the smaller array only
Space: O(1)              — no extra storage
"""
