import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class Solution {
    public int removeElement(int[] nums, int val) {
        List<Integer> sortedNoValNums = new ArrayList<>();
        for(int i=0; i<nums.length; i++) {
            if(nums[i] != val) {
                sortedNoValNums.add(nums[i]);
            }
        }
        Collections.sort(sortedNoValNums);
        for(int i=0; i<sortedNoValNums.size(); i++) {
            nums[i] = sortedNoValNums.get(i);
        }
        return sortedNoValNums.size();
    }
}