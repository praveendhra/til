import java.util.ArrayList;
import java.util.List;

class Solution {
    public boolean isPalindrome(int x) {
        if(x<0) {
            return false;
        }
        if(x==0) {
            return true;
        }
        List<Integer> list = new ArrayList<Integer>();
        while(x>0) {
            list.add(x%10);
            x/=10;
        }
        for(int i=0,j=list.size()-1; i!=j; i++,j-- ){
            if(list.get(i) != list.get(j)) {
                return false;
            }
        }
        return true;
    }
}