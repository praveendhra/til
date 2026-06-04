import java.util.Arrays;
import java.util.HashSet;

class Solution {
    public int removeDuplicates(int[] nums) {
        HashSet<Integer> uniqueNums = new HashSet<>();
        for (int i = 0; i < nums.length; i++) {
            uniqueNums.add(nums[i]);
        }

        // convert HashSet → Integer[] → sort → copy back into nums
        Integer[] sorted = uniqueNums.toArray(new Integer[0]);
        Arrays.sort(sorted);

        for (int i = 0; i < sorted.length; i++) {
            nums[i] = sorted[i];
        }

        return sorted.length;
    }
}