package ru.nsu.kuklin;

import java.util.concurrent.locks.ReentrantLock;

public class Main {
    static volatile boolean isParent = true;
    public static void main(String[] args) {
        var lock = new ReentrantLock(true);
        var child = new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                lock.lock();
                System.out.println("Child " + i);
                lock.unlock();
            }
        });
        lock.lock();
        child.start();
        System.out.println("Parent " + 0);
        lock.unlock();
        for (int i = 1; i < 10; i++) {
            lock.lock();
            System.out.println("Parent " + i);
            lock.unlock();
        }
    }
}