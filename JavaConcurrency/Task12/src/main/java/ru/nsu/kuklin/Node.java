package ru.nsu.kuklin;

public class Node {
    public Node prev;
    public Node next;
    public String string;

    synchronized void add(String line) {
        Node newNode = new Node();
        newNode.string = line;
        newNode.prev = this;
        newNode.next = next;
        next.prev = newNode;
        next = newNode;
    }

    synchronized void print() {
        var cur = this.next;
        while (cur != this) {
            System.out.println(cur.string);
            cur = cur.next;
        }
    }

    synchronized void sort() {
        boolean changed = true;
        while (changed) {
            changed = false;
            var cur = this.next;
            while (cur != this && cur.next != this) {
                if (cur.string.compareTo(cur.next.string) > 0) {
                    /* cur.prev -> cur -> cur.next -> cur.next.next */
                    /* a -> b -> c -> d */
                    /* a -> c -> b -> d */
                    var a = cur.prev;
                    var b = cur;
                    var c = cur.next;
                    var d = cur.next.next;
                    a.next = c;
                    c.prev = a;
                    c.next = b;
                    b.prev = c;
                    b.next = d;
                    d.prev = b;
                    changed = true;
                }
                cur = cur.next;
            }
        }
    }
}
