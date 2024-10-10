package ru.nsu.kuklin;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.locks.ReentrantLock;

public class Main {
    static volatile boolean isParent = true;
    public static void main(String[] args) {
        var locks = new ReentrantLock[] {
            new ReentrantLock(),
            new ReentrantLock(),
            new ReentrantLock()
        };
        var barrier = new CyclicBarrier(2);
        var child = new Thread(() -> {
            int lock1 = 0;
            int lock2 = 1;
            locks[lock1].lock();
            try {
                barrier.await();
            } catch (Exception ignored) {}
            for (int i = 0; i < 10; i++) {
                locks[lock2].lock();
                System.out.println("Child " + i);
                locks[lock1].unlock();
                lock1 = (lock1 + 1) % 3;
                lock2 = (lock2 + 1) % 3;
            }
        });
        child.start();
        int lock1 = 2;
        int lock2 = 0;
        locks[lock1].lock();
        try {
            barrier.await();
        } catch (Exception ignored) {}
        for (int i = 0; i < 10; i++) {
            locks[lock2].lock();
            System.out.println("Parent " + i);
            locks[lock1].unlock();
            lock1 = (lock1 + 1) % 3;
            lock2 = (lock2 + 1) % 3;
        }
    }
}