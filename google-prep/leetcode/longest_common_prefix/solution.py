from typing import List

class Solution:
    def longestCommonPrefix(self, strs: List[str]) -> str:
        result = ""

        for j in range(len(strs[0])):
            c = strs[0][j]

            for i in range(1, len(strs)):
                if j >= len(strs[i]) or strs[i][j] != c:
                    return result

            result += c

        return result


"""
WALKTHROUGH: strs = ["flower", "flow", "flight"]
--------------------------------------------------
j=0: c='f'  → flow[0]='f' ✓  flight[0]='f' ✓  → result="f"
j=1: c='l'  → flow[1]='l' ✓  flight[1]='l' ✓  → result="fl"
j=2: c='o'  → flow[2]='o' ✓  flight[2]='i' ✗  → return "fl" ✓

WALKTHROUGH: strs = ["dog", "racecar", "car"]
----------------------------------------------
j=0: c='d'  → racecar[0]='r' ✗  → return "" ✓


PYTHON vs JAVA DIFFERENCES
---------------------------
- strs[0][j]                   Java's  strs[0].charAt(j)
                               Python strings are directly indexable like arrays
- len(strs[i])                 Java's  strs[i].length()
- range(1, len(strs))          Java's  for(int i=1; i<strs.length; i++)
- result += c                  Java's  result.append(c)
                               Python str += is fine here (output is short)

ALTERNATIVELY — more Pythonic using zip():
------------------------------------------
for chars in zip(*strs):          # groups chars at each position across all strings
    if len(set(chars)) == 1:      # set removes duplicates — if all same, size is 1
        result += chars[0]
    else:
        break

zip(*strs) on ["flower","flow","flight"] gives:
  ('f','f','f'), ('l','l','l'), ('o','o','i'), ...
  set('o','o','i') = {'o','i'} → size 2 → not all same → stop

COMPLEXITY
-----------
Time:  O(S)  where S = total characters across all strings
Space: O(1)  — result at most length of strs[0]
"""
