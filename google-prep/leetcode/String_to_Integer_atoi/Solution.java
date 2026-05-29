class Solution {
    public int myAtoi(String s) {
        int i = 0, n = s.length();
        long result = 0;
        int sign = 1;

        // Step 1: skip leading whitespace
        while (i < n && s.charAt(i) == ' ') i++;

        // Step 2: check for sign
        if (i < n && (s.charAt(i) == '+' || s.charAt(i) == '-')) {
            sign = (s.charAt(i) == '-') ? -1 : 1;
            i++;
        }

        // Step 3: read digits until non-digit or end of string
        while (i < n && Character.isDigit(s.charAt(i))) {
            result = result * 10 + (s.charAt(i) - '0');
            i++;
            // Step 4: clamp early to avoid overflow
            if (result * sign > Integer.MAX_VALUE) return Integer.MAX_VALUE;
            if (result * sign < Integer.MIN_VALUE) return Integer.MIN_VALUE;
        }

        return (int) result * sign;
    }
}

/*
WHAT IS ATOI?
--------------
atoi = "ascii to integer"
Converts a string like "  -42abc" → -42
Rules:
  1. Skip leading spaces
  2. Optional + or - sign
  3. Read digits, stop at first non-digit
  4. Clamp to [-2147483648, 2147483647] if too large/small
  5. Return 0 if no valid number found

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
INT_MAX = 2147483647
result builds up to 2147483648
result * sign(1) > INT_MAX → return Integer.MAX_VALUE = 2147483647 ✓

WALKTHROUGH: s = "abc"  (no digits)
-------------------------------------
i=0: 'a' → not space, not sign, not digit → digit loop never runs
result = 0, sign = 1 → return 0 ✓

BUGS IN ORIGINAL CODE
----------------------
1. res = 1               should be  res = 0
2. condition used &&     impossible: a char can't be both < 48 AND > 57
                         should be  || (less than '0' OR greater than '9')
3. Only checked charAt(0) — doesn't handle leading spaces or sign
4. Never actually parsed the digits
5. No overflow handling

'0' in ASCII = 48,  '9' in ASCII = 57
s.charAt(i) - '0'  converts char digit to int:  '5' - '0' = 53 - 48 = 5

COMPLEXITY
-----------
Time:  O(n)  — single pass through the string
Space: O(1)  — no extra storage
*/
