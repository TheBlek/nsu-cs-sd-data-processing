package ru.nsu.kuklin;

import java.util.ArrayList;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.locks.ReentrantLock;

public class Main {
    public static void main(String[] args) {
        final var philosopherCount = 5;
        var forks = new ArrayList<ReentrantLock>(philosopherCount);
        for (int i = 0; i < philosopherCount; i++) {
            forks.add(new ReentrantLock());
        }
        var philosophers = new Thread[philosopherCount];
        for (int i = 0; i < philosopherCount; i++) {
            philosophers[i] = new Thread(new Philosopher(forks.get(i), forks.get((i + 1) % philosopherCount)), "philosopher " + i);
            philosophers[i].start();
        }
        try {
            Thread.sleep(30000);
        } catch (Exception ignored) {}
        // No more spaghetti :(
        for (final var philosopher : philosophers) {
            philosopher.interrupt();
        }
    }
}