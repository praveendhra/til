class Solution {
    public String longestCommonPrefix(String[] strs) {
        StringBuilder result = new StringBuilder("");

        for (int j = 0; j < strs[0].length(); j++) {
            char c = strs[0].charAt(j);  

            for (int i = 1; i < strs.length; i++) {
                if (j >= strs[i].length() || strs[i].charAt(j) != c) {
                    return result.toString();
                }
            }
            result.append(c);  
        }

        return result.toString();
    }
}

/*
CORE IDEA — vertical scan
--------------------------
Think of the strings stacked on top of each other like a grid.
Scan column by column (left to right).
The moment any string has a different char (or runs out), stop.

    f l o w e r
    f l o w
    f l i g h t
    ^     ^
    |     stop here — 'o' != 'i'
    all match

Answer: "fl" ✓

WALKTHROUGH: strs = ["flower", "flow", "flight"]
--------------------------------------------------
j=0: c='f'  → flow[0]='f' ✓  flight[0]='f' ✓  → append 'f'
j=1: c='l'  → flow[1]='l' ✓  flight[1]='l' ✓  → append 'l'
j=2: c='o'  → flow[2]='o' ✓  flight[2]='i' ✗  → return "fl"

Answer: "fl" ✓

WALKTHROUGH: strs = ["dog", "racecar", "car"]
----------------------------------------------
j=0: c='d'  → racecar[0]='r' ✗  → return ""

Answer: "" ✓

EDGE CASES HANDLED
-------------------
- One string shorter than strs[0]:
    j >= strs[i].length() catches this before charAt() throws IndexOutOfBoundsException
- All strings identical → full string returned
- No common prefix → empty string returned

COMPLEXITY
-----------
Time:  O(S)  where S = total characters across all strings (worst case all identical)
Space: O(1)  — result at most length of strs[0]
*/