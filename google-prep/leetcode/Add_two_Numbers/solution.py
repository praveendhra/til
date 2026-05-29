from typing import Optional

# Definition for singly-linked list.
# class ListNode:
#     def __init__(self, val=0, next=None):
#         self.val = val
#         self.next = next

class Solution:
    def addTwoNumbers(self, l1: Optional[ListNode], l2: Optional[ListNode]) -> Optional[ListNode]:
        dummy = ListNode(0)
        curr = dummy
        carry = 0

        while l1 or l2 or carry:
            total = carry
            if l1:
                total += l1.val
                l1 = l1.next
            if l2:
                total += l2.val
                l2 = l2.next
            carry = total // 10
            curr.next = ListNode(total % 10)
            curr = curr.next

        return dummy.next


"""
WALKTHROUGH EXAMPLE
--------------------
l1 = [2,4,3]  ->  represents 342  (stored in reverse)
l2 = [5,6,4]  ->  represents 465  (stored in reverse)
expected output: 807  ->  [7,0,8]

Iteration 1 — ones place:
    total = 0 (carry)
    total += l1.val(2)  ->  total = 2
    total += l2.val(5)  ->  total = 7
    carry = 7 // 10 = 0
    digit = 7 % 10  = 7  ->  ListNode(7)
    l1->[4,3],  l2->[6,4]

Iteration 2 — tens place:
    total = 0 (carry)
    total += l1.val(4)  ->  total = 4
    total += l2.val(6)  ->  total = 10
    carry = 10 // 10 = 1   <- overflow! carry 1 forward
    digit = 10 % 10  = 0  ->  ListNode(0)
    l1->[3],  l2->[4]

Iteration 3 — hundreds place:
    total = 1 (carry from previous)
    total += l1.val(3)  ->  total = 4
    total += l2.val(4)  ->  total = 8
    carry = 8 // 10 = 0
    digit = 8 % 10  = 8  ->  ListNode(8)
    l1->None,  l2->None

Loop ends — both lists empty, carry is 0.
Result: 7 -> 0 -> 8  =  807 ✓


DATA STRUCTURES USED
---------------------
1. ListNode  (linked list node)
   - Each node has:  .val  (the digit)  and  .next  (pointer to next node)
   - Like a chain:   node1 -> node2 -> node3 -> None
   - No index-based access — you must walk it with .next

2. Optional[ListNode]  (type hint)
   - Means the value can be a ListNode OR None
   - Optional[X]  is shorthand for  Union[X, None]
   - Python won't enforce it, just for readability

PYTHON CONCEPTS USED
---------------------
- "while l1 or l2 or carry"
    truthy check: a ListNode object is truthy, None is falsy
    same as Java's:  while (l1 != null || l2 != null || carry != 0)

- "//"  integer division  (floor division)
    10 // 10  ->  1      (no decimal, like Java's  10 / 10)
    7  // 10  ->  0
    compare to "/" which gives float:  10 / 10  ->  1.0

- "%"  modulo (remainder)
    10 % 10  ->  0
    7  % 10  ->  7

- "if l1:"  vs  "if l1 is not None:"
    both work — "if l1:" is more Pythonic
    falsy values in Python: None, 0, [], {}, ""
"""
