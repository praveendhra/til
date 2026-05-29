from typing import List

class Solution:
    def twoSum(self, nums: List[int], target: int) -> List[int]:
        seen = {} # hashmap
        for i, num in enumerate(nums):
            complement = target - num
            if complement in seen: # map.get(ele) != null
                return [seen[complement], i]
            seen[num] = i
        return []


"""
DATA STRUCTURES USED
---------------------
1. dict  (seen = {})
   - Python's built-in dictionary, equivalent to Java's HashMap
   - Stores key-value pairs: seen[key] = value
   - "key in dict"  -> checks if key exists        -> O(1)
   - dict[key]      -> gets the value for a key     -> O(1)
   - dict[key] = v  -> sets/updates a value         -> O(1)
   - Here: key = number, value = its index in nums

2. List[int]  (nums parameter and return type)
   - Python's built-in list, like a Java array but dynamic
   - Ordered, index-based: nums[0], nums[1], ...
   - List[int] is just a type hint — means "a list of integers"
   - We return a plain list: return [index1, index2]

PYTHON CONCEPTS USED
---------------------
- enumerate(nums)
    gives (index, value) on each loop iteration
    instead of:  for i in range(len(nums)): num = nums[i]
    cleaner way:  for i, num in enumerate(nums)

- "x in dict"
    checks if x is a KEY in the dict (not a value)
    e.g.  2 in {2: 0}  -> True

- Type hints  (List[int], -> List[int])
    optional in Python, just for readability
    Python won't throw an error if you pass the wrong type

SQUARE BRACKETS [ ] vs CURLY BRACES { }
-----------------------------------------
[ ]  -> list  (ordered, allows duplicates, index-based)
    my_list = [1, 2, 3]
    my_list[0]        -> 1
    use when: you need a sequence you can loop over or index into

{ }  -> dict  (key-value pairs, like HashMap)
    my_dict = {"a": 1, "b": 2}
    my_dict["a"]      -> 1
    use when: you need fast lookup by a key

{ }  -> set  (unordered, unique values only, like HashSet)
    my_set = {1, 2, 3}
    2 in my_set       -> True
    use when: you just need to check existence, no duplicates needed

( )  -> tuple  (ordered, allows duplicates, but IMMUTABLE)
    my_tuple = (1, 2, 3)
    my_tuple[0]       -> 1
    use when: data should not change (coordinates, RGB values, dict keys)

( )  -> also used for function calls and grouping expressions
    enumerate(nums)   -> calling a function
    (a + b) * c       -> grouping

QUICK RULE:
    need ordered sequence?                 -> list   [ ]
    need key -> value mapping?             -> dict   { key: value }
    need unique values / existence check?  -> set    { value }
    need ordered but immutable sequence?   -> tuple  ( )

NOTE:
    {}  alone = empty dict  (NOT a set)
    []  alone = empty list
    ()  alone = empty tuple
    set()    = empty set   (must use this, not {})
"""