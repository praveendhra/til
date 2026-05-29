/**
 * Definition for singly-linked list.
 * public class ListNode {
 *     int val;
 *     ListNode next;
 *     ListNode() {}
 *     ListNode(int val) { this.val = val; }
 *     ListNode(int val, ListNode next) { this.val = val; this.next = next; }
 * }
 */
class Solution {
    public ListNode addTwoNumbers(ListNode l1, ListNode l2) {
        // dummy is a throwaway head node so we never need special logic for the first node.
        // curr is our pointer that we advance as we build the result list.
        ListNode dummy = new ListNode(0);
        ListNode curr = dummy;

        // carry holds the overflow from the previous digit (0 or 1).
        // e.g. 7 + 5 = 12 → digit=2, carry=1 into next step
        int carry = 0;

        // keep going as long as either list has digits, or there's a leftover carry.
        // e.g. 99 + 1 = 100 → after both lists finish, carry=1 still needs a new node.
        while (l1 != null || l2 != null || carry != 0) {
            // start sum with carry from previous step
            int sum = carry;

            // add l1's digit if it still has nodes
            if (l1 != null) {
                sum += l1.val;
                l1 = l1.next;
            }

            // add l2's digit if it still has nodes
            if (l2 != null) {
                sum += l2.val;
                l2 = l2.next;
            }

            // sum can be 0-19 (max: 9+9+1 carry = 19)
            carry = sum / 10;        // 0 or 1 — passed to next iteration
            curr.next = new ListNode(sum % 10);  // store the ones digit as a new node
            curr = curr.next;
        }

        // dummy.next is the actual head (we skip the placeholder dummy node)
        return dummy.next;
    }
}

/*
WALKTHROUGH EXAMPLE
--------------------
l1 = [2,4,3]  →  represents 342  (stored in reverse)
l2 = [5,6,4]  →  represents 465  (stored in reverse)
expected output: 807  →  [7,0,8]

Iteration 1 — ones place:
    sum = 0 (carry)
    sum += l1.val(2)  →  sum = 2
    sum += l2.val(5)  →  sum = 7
    carry = 7/10 = 0
    digit = 7%10 = 7  →  node(7)
    l1→[4,3],  l2→[6,4]

Iteration 2 — tens place:
    sum = 0 (carry)
    sum += l1.val(4)  →  sum = 4
    sum += l2.val(6)  →  sum = 10
    carry = 10/10 = 1   ← overflow! carry 1 forward
    digit = 10%10 = 0  →  node(0)
    l1→[3],  l2→[4]

Iteration 3 — hundreds place:
    sum = 1 (carry from previous)
    sum += l1.val(3)  →  sum = 4
    sum += l2.val(4)  →  sum = 8
    carry = 8/10 = 0
    digit = 8%10 = 8  →  node(8)
    l1→null,  l2→null

Loop ends — both lists empty, carry is 0.
Result: 7 → 0 → 8  =  807 ✓

Key insight: handle one column at a time, just like pen-and-paper addition.
Never reconstruct the full number — avoids integer overflow for large inputs.
*/