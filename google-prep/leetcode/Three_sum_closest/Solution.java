import java.util.Arrays;

class Solution {
    public int threeSumClosest(int[] nums, int target) {
        Arrays.sort(nums);
        int closest = nums[0] + nums[1] + nums[2];  

        for (int i = 0; i < nums.length - 2; i++) {
            int left = i + 1;
            int right = nums.length - 1;

            while (left < right) {
                int sum = nums[i] + nums[left] + nums[right];

                if (Math.abs(sum - target) < Math.abs(closest - target)) {
                    closest = sum;
                }

                if (sum < target) {
                    left++;  
                } else if (sum > target) {
                    right--;  
                } else {
                    return sum;  
                }
            }
        }

        return closest;
    }
}

/*
CORE IDEA — Sort + Two Pointers
---------------------------------
Sort the array. Fix one element (i), use two pointers on the rest.
Moving left pointer right → increases sum
Moving right pointer left → decreases sum

WALKTHROUGH: nums=[-1,2,1,-4], target=1
-----------------------------------------
After sort: [-4, -1, 1, 2]
closest = -4 + -1 + 1 = -4

i=0  nums[i]=-4   left=1  right=3
  sum = -4 + -1 + 2 = -3   |(-3-1)|=4  |(-4-1)|=5  → closest=-3
  sum=-3 < target=1 → left++
  left=2  right=3
  sum = -4 + 1 + 2 = -1    |(-1-1)|=2  |(-3-1)|=4  → closest=-1
  sum=-1 < target=1 → left++
  left=3  right=3 → left not < right → stop

i=1  nums[i]=-1   left=2  right=3
  sum = -1 + 1 + 2 = 2     |(2-1)|=1   |(-1-1)|=2  → closest=2
  sum=2 > target=1 → right--
  left=2  right=2 → stop

return 2 ✓

COMPLEXITY
-----------
Time:  O(n²)  — one outer loop O(n), two-pointer inner loop O(n)
Space: O(1)   — no extra storage (sort is in-place)
*/