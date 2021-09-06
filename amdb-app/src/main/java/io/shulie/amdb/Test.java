/*
 * Copyright 2021 Shulie Technology, Co.Ltd
 * Email: shulie@shulie.io
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.shulie.amdb;

/**
 * Definition for singly-linked list.
 * public class ListNode {
 * int val;
 * ListNode next;
 * ListNode() {}
 * ListNode(int val) { this.val = val; }
 * ListNode(int val, ListNode next) { this.val = val; this.next = next; }
 * }
 */
class Solution {
    public ListNode reverseBetween(ListNode head, int left, int right) {
        ListNode leftParent = null;
        ListNode left0 = null;
        ListNode rightParent = null;
        ListNode right0 = null;
        ListNode currentNode = head;
        while (currentNode.next != null) {
            currentNode = currentNode.next;
            if (currentNode.val == right) {
                right0 = currentNode;
                rightParent = head;
                break;
            }
            if (currentNode.val == left) {
                left0 = currentNode;
                leftParent = head;
            }
        }
        if (leftParent != null && left0 != null && rightParent != null && right0 != null) {
            leftParent.next = right0;
            rightParent.next = left0;
        }
        return head;
    }
}

class ListNode {
    int val;
    ListNode next;

    ListNode() {
    }

    ListNode(int val) {
        this.val = val;
    }

    ListNode(int val, ListNode next) {
        this.val = val;
        this.next = next;
    }
}
