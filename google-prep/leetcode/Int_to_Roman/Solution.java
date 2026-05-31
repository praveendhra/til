class Solution {
    public String intToRoman(int num) {
        int[]    values  = {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
        String[] symbols = {"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};

        StringBuilder result = new StringBuilder();

        for (int i = 0; i < values.length; i++) {
            // keep subtracting current value as long as num is big enough
            while (num >= values[i]) {
                result.append(symbols[i]);
                num -= values[i];
            }
        }

        return result.toString();
    }
}

/*
CORE IDEA — Greedy
-------------------
Roman numerals are built greedily: always use the largest symbol that fits.
Pre-store all values including the subtractive pairs (4=IV, 9=IX, 40=XL, etc.)
then subtract them off one by one.

ROMAN NUMERAL RULES
--------------------
I=1   V=5   X=10   L=50   C=100   D=500   M=1000

Subtractive pairs (the tricky ones):
  IV=4   IX=9   XL=40   XC=90   CD=400   CM=900

WALKTHROUGH: num = 1994
------------------------
i=0  values[0]=1000  1994>=1000 → append "M",  num=994
i=1  values[1]=900   994>=900  → append "CM", num=94
i=4  values[4]=100   94<100   → skip
i=5  values[5]=90    94>=90   → append "XC", num=4
i=10 values[10]=5    4<5      → skip
i=11 values[11]=4    4>=4     → append "IV", num=0

result = "M" + "CM" + "XC" + "IV" = "MCMXCIV" ✓

WALKTHROUGH: num = 58
----------------------
i=2  values[2]=500  58<500 → skip
i=6  values[6]=50   58>=50 → append "L",  num=8
i=8  values[8]=10   8<10   → skip
i=10 values[10]=5   8>=5   → append "V",  num=3
i=12 values[12]=1   3>=1   → append "I",  num=2
                    2>=1   → append "I",  num=1
                    1>=1   → append "I",  num=0

result = "LVIII" ✓

COMPLEXITY
-----------
Time:  O(1)  — num is bounded (1 to 3999), fixed number of iterations
Space: O(1)  — output length is also bounded
*/