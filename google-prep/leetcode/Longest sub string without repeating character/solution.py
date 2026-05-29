class Solution:
    def lengthOfLongestSubstring(self, s: str) -> int:
        window = set()
        left = 0
        max_len = 0

        for right in range(len(s)):
            while s[right] in window:
                window.remove(s[left])
                left += 1
            window.add(s[right])
            max_len = max(max_len, right - left + 1)

        return max_len


"""
WALKTHROUGH: s = "abcabcbb"

right=0  char='a'  window=set()    -> add 'a'  window={'a'}        size=1  max=1
right=1  char='b'  window={'a'}    -> add 'b'  window={'a','b'}    size=2  max=2
right=2  char='c'  window={'a','b'}-> add 'c'  window={'a','b','c'}size=3  max=3
right=3  char='a'  -> 'a' in window!
                      remove s[left=0]='a', left=1  window={'b','c'}
                   -> add 'a'  window={'b','c','a'}  size=3  max=3
right=4  char='b'  -> 'b' in window!
                      remove s[left=1]='b', left=2  window={'c','a'}
                   -> add 'b'  window={'c','a','b'}  size=3  max=3
...
Answer: 3 ("abc") ✓


PYTHON vs JAVA DIFFERENCES
---------------------------
- "s[right] in window"       same as Java's  window.contains(s.charAt(right))
- "s[right]"                 same as Java's  s.charAt(right)
                             Python strings are directly indexable
- "for right in range(len(s))"  same as Java's  for(int right=0; right<s.length(); right++)
- set()                      same as Java's  new HashSet<>()
- window.add(x)              same in both
- window.remove(x)           same in both
- max(a, b)                  same as Java's  Math.max(a, b)  — built-in in Python, no import needed

DATA STRUCTURES USED
---------------------
- set  (window)
    unordered collection of unique values
    "x in set"       -> O(1) lookup  (way faster than  "x in list"  which is O(n))
    set.add(x)       -> O(1)
    set.remove(x)    -> O(1)
    written as  set()  for empty set  (NOT {}  which creates an empty dict)

- str  (s)
    strings in Python are directly indexable:  s[0], s[1], ...
    no need for  s.charAt(i)  like Java

- int  (left, right, max_len)
    plain integers — no type declaration needed in Python
"""
