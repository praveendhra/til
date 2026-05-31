class Solution:
    def intToRoman(self, num: int) -> str:
        values  = [1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1]
        symbols = ["M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"]

        result = ""

        for i in range(len(values)):
            while num >= values[i]:
                result += symbols[i]
                num -= values[i]

        return result


"""
WALKTHROUGH: num = 1994
------------------------
num=1994  >= 1000 → result="M",    num=994
num=994   >= 900  → result="MCM",  num=94
num=94    >= 90   → result="MCMXC",num=4
num=4     >= 4    → result="MCMXCIV", num=0

return "MCMXCIV" ✓


PYTHON vs JAVA DIFFERENCES
---------------------------
- result = ""                  Java's  new StringBuilder()
- result += symbols[i]         Java's  result.append(symbols[i])
                               Python strings can be concatenated with +=
                               (Java uses StringBuilder for efficiency; in Python it's fine here
                               since the output is always short — max ~15 chars)
- for i in range(len(values))  Java's  for(int i=0; i<values.length; i++)
- list  []                     Java's  int[] / String[]  arrays

ALTERNATIVELY — more Pythonic using zip():
------------------------------------------
for value, symbol in zip(values, symbols):
    while num >= value:
        result += symbol
        num -= value

zip(values, symbols) pairs them up:  (1000,"M"), (900,"CM"), ...
so you get both value and symbol directly without needing index i.
"""
