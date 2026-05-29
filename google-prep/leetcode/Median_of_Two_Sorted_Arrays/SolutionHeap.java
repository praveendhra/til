import java.util.PriorityQueue;

class SolutionHeap {
    public double findMedianSortedArrays(int[] nums1, int[] nums2) {
        PriorityQueue<Integer> minHeap = new PriorityQueue<>();

        for (int n : nums1) minHeap.add(n);
        for (int n : nums2) minHeap.add(n);

        int total = minHeap.size();

        if (total % 2 == 1) {
            for (int i = 0; i < total / 2; i++) minHeap.poll();
            return minHeap.poll();
        } else {
            for (int i = 0; i < total / 2 - 1; i++) minHeap.poll();
            double left = minHeap.poll();
            double right = minHeap.poll();
            return (left + right) / 2.0;
        }
    }
}

/*
CORE IDEA
----------
A min-heap always keeps the smallest element at the top.
Add all elements into one min-heap, then poll() until you reach the median position.

WALKTHROUGH: nums1=[1,3], nums2=[2]   total=3 (odd)
----------------------------------------------------
heap after adding all: [1, 2, 3]  (min at top)

median is at position  total/2 = 1  (0-indexed middle)
poll 1 time to skip  →  discard 1
poll once more       →  get 2

return 2.0 ✓

WALKTHROUGH: nums1=[1,2], nums2=[3,4]   total=4 (even)
-------------------------------------------------------
heap after adding all: [1, 2, 3, 4]

median is average of positions 1 and 2  (0-indexed)
poll total/2 - 1 = 1 time to skip  →  discard 1
poll  →  left  = 2
poll  →  right = 3

return (2 + 3) / 2.0 = 2.5 ✓

HOW poll() WORKS
-----------------
PriorityQueue in Java is a MIN-heap by default.
poll() always removes and returns the SMALLEST element.
After each poll, the heap restructures itself in O(log n).

COMPLEXITY
-----------
Time:  O((m+n) log(m+n))  — each add/poll is O(log n)
Space: O(m+n)              — storing all elements in the heap

Compare to binary search solution:
Time:  O(log(m+n))   ← faster
Space: O(1)          ← no extra storage

The heap approach is simpler to understand but doesn't meet
the O(log(m+n)) requirement in the problem. Binary search is needed for that.
*/
