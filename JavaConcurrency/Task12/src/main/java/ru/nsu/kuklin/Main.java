package ru.nsu.kuklin;

import java.util.Scanner;
import java.util.concurrent.locks.ReentrantLock;

public class Main {
    public static void main(String[] args) {
        Node head = new Node();
        head.next = head;
        head.prev = head;
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(5000);
                } catch (Exception e) {
                    return;
                }
                head.sort();
            }
        }).start();
        Scanner in = new Scanner(System.in);
        while (in.hasNextLine()) {
            String next = in.nextLine();
            if (next.compareTo("") == 0) {
                head.print();
            } else {
                head.add(next);
            }
        }
    }
}