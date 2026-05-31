class Solution {
    public String convert(String s, int numRows) {
        if (numRows == 1 || numRows >= s.length()) return s;

        // one StringBuilder per row
        StringBuilder[] rows = new StringBuilder[numRows];
        for (int i = 0; i < numRows; i++) rows[i] = new StringBuilder();

        int currentRow = 0;
        boolean goingDown = false;

        for (char c : s.toCharArray()) {
            rows[currentRow].append(c);
            // flip direction at top or bottom row
            if (currentRow == 0 || currentRow == numRows - 1) {
                goingDown = !goingDown;
            }
            currentRow += goingDown ? 1 : -1;
        }

        // read all rows top to bottom
        StringBuilder result = new StringBuilder();
        for (StringBuilder row : rows) result.append(row);
        return result.toString();
    }
}

/*
CORE IDEA
----------
Simulate the zigzag by placing each character into a "bucket" for its row.
Use a direction flag that flips when you hit row 0 or the last row.
Then just read all buckets top to bottom.

VISUAL: s="PAYPALISHIRING", numRows=3

Row 0:  P . . A . . H . . N       → "PAHN"
Row 1:  . A . . P . . L . . S . . I . . I . . G   → "APLSIIG"
Row 2:  . . Y . . . . I . . . . R       → "YIR"

Reading row by row: "PAHN" + "APLSIIG" + "YIR" = "PAHNAPLSIIGYIR" ✓

WALKTHROUGH: s="PAYPALISHIRING", numRows=3
-------------------------------------------
char  row  direction
P     0    flip→down,  row=1
A     1    down,       row=2
Y     2    flip→up,    row=1
P     1    up,         row=0
A     0    flip→down,  row=1
L     1    down,       row=2
I     2    flip→up,    row=1
S     1    up,         row=0
H     0    flip→down,  row=1
I     1    down,       row=2
R     2    flip→up,    row=1
I     1    up,         row=0
N     0    flip→down,  row=1
G     1    down,       row=2

Row 0: P A H N
Row 1: A P L S I I G
Row 2: Y I R

Result: "PAHNAPLSIIGYIR" ✓

EDGE CASES
-----------
numRows=1 → no zigzag possible, return s as-is
numRows >= s.length() → each char is in its own row, return s as-is

COMPLEXITY
-----------
Time:  O(n)  — single pass through the string
Space: O(n)  — storing all chars in row buckets
*/