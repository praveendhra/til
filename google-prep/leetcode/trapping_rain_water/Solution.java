class Solution {
    public int trap(int[] height) {
        int left = 0, right = height.length - 1;
        int leftMax = 0, rightMax = 0;
        int water = 0;

        while (left < right) {
            if (height[left] < height[right]) {
                if (height[left] >= leftMax) {
                    leftMax = height[left];              
                } else {
                    water += leftMax - height[left];     
                }
                left++;
            } else {
                if (height[right] >= rightMax) {
                    rightMax = height[right];            
                } else {
                    water += rightMax - height[right];   
                }
                right--;
            }
        }
        return water;
    }
}


/*
CORE IDEA — Two Pointers
--------------------------
Water trapped at any index = min(maxLeft, maxRight) - height[i]

Use two pointers. The side with the shorter max wall is the limiting factor.
If height[left] < height[right]:
  - right wall is guaranteed >= height[right]
  - so leftMax is the actual bottleneck → safe to compute water on left side

VISUAL: height = [0,1,0,2,1,0,1,3,2,1,2,1]

         #
     #   ##  #
  #  # # ######
  0  1 0 2 1 0 1 3 2 1 2 1

Water fills the valleys: total = 6 units ✓

WALKTHROUGH (first few steps):
--------------------------------
left=0  right=11  leftMax=0  rightMax=0  water=0

h[0]=0 < h[11]=1 → process left
  h[0]=0 >= leftMax=0 → leftMax=0,  left=1

h[1]=1 >= h[11]=1 → process right
  h[11]=1 >= rightMax=0 → rightMax=1, right=10

h[1]=1 < h[10]=2 → process left
  h[1]=1 >= leftMax=0 → leftMax=1,  left=2

h[2]=0 < h[10]=2 → process left
  h[2]=0 < leftMax=1 → water += 1-0=1,  left=3   (water=1)

... continues → total water = 6 ✓

COMPLEXITY
-----------
Time:  O(n)  — each element visited once
Space: O(1)  — only 4 variables
*/