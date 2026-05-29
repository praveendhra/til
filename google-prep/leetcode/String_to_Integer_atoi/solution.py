class Solution:
    def myAtoi(self, s: str) -> int:
        i = 0
        n = len(s)
        result = 0
        sign = 1
        INT_MAX = 2**31 - 1   # 2147483647
        INT_MIN = -(2**31)    # -2147483648

        # Step 1: skip leading whitespace
        while i < n and s[i] == ' ':
            i += 1

        # Step 2: check for sign
        if i < n and s[i] in ('+', '-'):
            sign = -1 if s[i] == '-' else 1
            i += 1

        # Step 3: read digits until non-digit or end of string
        while i < n and s[i].isdigit():
            result = result * 10 + int(s[i])
            i += 1
            # Step 4: clamp early to avoid overflow
            if result * sign > INT_MAX: return INT_MAX
            if result * sign < INT_MIN: return INT_MIN

        return result * sign


"""
WALKTHROUGH: s = "   -42abc"
------------------------------
i=0: ' ' → skip       i=1
i=1: ' ' → skip       i=2
i=2: ' ' → skip       i=3
i=3: '-' → sign=-1    i=4
i=4: '4' → result = 0*10 + 4 = 4    i=5
i=5: '2' → result = 4*10 + 2 = 42   i=6
i=6: 'a' → not a digit, stop

return 42 * -1 = -42 ✓

WALKTHROUGH: s = "2147483648"  (INT_MAX + 1, should clamp)
-----------------------------------------------------------
result builds up to 2147483648
result * sign(1) > INT_MAX → return 2147483647 ✓

WALKTHROUGH: s = "abc"  (no digits)
-------------------------------------
i=0: 'a' → not space, not sign → digit loop never runs
result=0, sign=1 → return 0 ✓


PYTHON vs JAVA DIFFERENCES
---------------------------
- s[i].isdigit()            Java's  Character.isDigit(s.charAt(i))
- int(s[i])                 Java's  s.charAt(i) - '0'
                            Python can directly convert char to int with int()
                            Java has no int('5'), needs ASCII subtraction trick
- s[i] in ('+', '-')        Java's  s.charAt(i) == '+' || s.charAt(i) == '-'
- 2**31 - 1                 Java's  Integer.MAX_VALUE   (Python has no int overflow!)
- -(2**31)                  Java's  Integer.MIN_VALUE
- len(s)                    Java's  s.length()

NOTE on overflow:
  Java int overflows silently (wraps around) → must clamp manually
  Python integers never overflow (arbitrary size) → still clamp manually
  because the problem requires returning within 32-bit int range

COMPLEXITY
-----------
Time:  O(n)  — single pass through the string
Space: O(1)  — no extra storage
"""
