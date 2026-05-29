import java.util.HashMap;

class Solution {
    public int[] twoSum(int[] nums, int target) {
       HashMap<Integer, Integer> seen = new HashMap<>();
       for(int i=0; i<nums.length; i++) {
        int complement = target - nums[i];
        if(seen.get(complement) != null) {
            return new int[]{seen.get(complement), i};
        }
        seen.put(nums[i], i);
       }
       return new int[]{};
    }
}

/*
This method of complement mainly relies on the fact that when we 
needed to backtrack, we store the diff and the index for reference which we use
later for that purpose.
*/
