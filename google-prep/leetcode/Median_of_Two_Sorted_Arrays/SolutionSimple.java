
class SolutionSimple {
    public double findMedianSortedArrays(int[] nums1, int[] nums2) {
        // Step 1: merge both sorted arrays into one sorted array
        int[] merged = new int[nums1.length + nums2.length];
        int i = 0, j = 0, k = 0;

        while (i < nums1.length && j < nums2.length) {
            if (nums1[i] <= nums2[j])
                merged[k++] = nums1[i++];
            else
                merged[k++] = nums2[j++];
        }
        while (i < nums1.length) merged[k++] = nums1[i++];
        while (j < nums2.length) merged[k++] = nums2[j++];

        // Step 2: find median from merged array
        int total = merged.length;
        if (total % 2 == 1) {
            return merged[total / 2];               // odd: middle element
        } else {
            return (merged[total / 2 - 1] + merged[total / 2]) / 2.0;  // even: avg of two middles
        }
    }
}

/*
WALKTHROUGH: nums1=[1,3], nums2=[2]
------------------------------------
Step 1 — merge like merging two sorted piles of cards:
    i=0 j=0: 1 vs 2 → take 1  merged=[1]
    i=1 j=0: 3 vs 2 → take 2  merged=[1,2]
    i=1 j=1: j done → take 3  merged=[1,2,3]

Step 2 — find median:
    total=3 (odd) → middle index = 3/2 = 1
    merged[1] = 2
    return 2.0 ✓

WALKTHROUGH: nums1=[1,2], nums2=[3,4]
--------------------------------------
Step 1 — merge:
    1 vs 3 → take 1
    2 vs 3 → take 2
    j still has [3,4] → take 3, 4
    merged=[1,2,3,4]

Step 2 — find median:
    total=4 (even) → two middles at index 1 and 2
    merged[1]=2, merged[2]=3
    return (2+3)/2.0 = 2.5 ✓

HOW THE MERGE WORKS (two-pointer)
-----------------------------------
Think of it like merging two sorted piles of cards face up.
Always pick the smaller top card from either pile.
When one pile runs out, dump the rest of the other pile.

    nums1: [1, 3, 5]
    nums2: [2, 4, 6]
    
    pick 1 (nums1 smaller)  → [1]
    pick 2 (nums2 smaller)  → [1,2]
    pick 3 (nums1 smaller)  → [1,2,3]
    pick 4 (nums2 smaller)  → [1,2,3,4]
    pick 5 (nums1 smaller)  → [1,2,3,4,5]
    pick 6 (nums2 smaller)  → [1,2,3,4,5,6]

MEDIAN RULE
------------
    odd total  [1,2,3]      → middle element       = 2
    even total [1,2,3,4]    → avg of two middles   = (2+3)/2 = 2.5

COMPLEXITY
-----------
Time:  O(m+n)   — visit every element once
Space: O(m+n)   — store merged array

(The binary search in Solution.java does O(log(m+n)) but is much harder to follow.
 Start here, then look at that once this is clear.)
*/
