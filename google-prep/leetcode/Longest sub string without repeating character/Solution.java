import java.util.HashSet;
import java.util.Set;

class Solution {
    public int lengthOfLongestSubstring(String s) {
        // window = characters currently in the window [left, right]
        Set<Character> window = new HashSet<>();
        int left = 0, maxLen = 0;

        for (int right = 0; right < s.length(); right++) {
            // if duplicate found, shrink window from left until it's gone
            while (window.contains(s.charAt(right))) {
                window.remove(s.charAt(left));
                left++;
            }
            window.add(s.charAt(right));
            // window size = right - left + 1
            maxLen = Math.max(maxLen, right - left + 1);
        }
        return maxLen;
    }
}

/*
WALKTHROUGH: s = "abcabcbb"

right=0  char='a'  window={}       → add 'a'  window={a}      size=1  max=1
right=1  char='b'  window={a}      → add 'b'  window={a,b}    size=2  max=2
right=2  char='c'  window={a,b}    → add 'c'  window={a,b,c}  size=3  max=3
right=3  char='a'  window={a,b,c}  → 'a' duplicate!
                     remove s[left=0]='a', left=1  window={b,c}
                   → add 'a'  window={b,c,a}  size=3  max=3
right=4  char='b'  window={b,c,a}  → 'b' duplicate!
                     remove s[left=1]='b', left=2  window={c,a}
                   → add 'b'  window={c,a,b}  size=3  max=3
right=5  char='c'  → same, left moves to 3, window={a,b,c}  max=3
right=6  char='b'  → shrink, window={b,c}  max=3
right=7  char='b'  → shrink, window={b}    max=3

Answer: 3 ("abc") ✓
*/