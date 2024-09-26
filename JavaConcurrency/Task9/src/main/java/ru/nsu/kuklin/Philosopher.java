package ru.nsu.kuklin;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.ThreadLocalRandom;

public class Philosopher implements Runnable {
    public Philosopher(ReentrantLock leftFork, ReentrantLock rightFork) {
        this.leftFork = leftFork;
        this.rightFork = rightFork;
    }

    @Override
    public void run() {
        var random = ThreadLocalRandom.current();
        while (!Thread.currentThread().isInterrupted()) {
            leftFork.lock();
            if (!rightFork.tryLock()) {
                leftFork.unlock();
                try {
                    Thread.sleep(random.nextInt(10, 50));
                } catch (Exception ignored) {}
                continue;
            }

            try {
                System.out.println(Thread.currentThread().getName() + " is eating now");
                Thread.sleep(random.nextInt(1000, 5000));
            } catch (InterruptedException ignored) {
            } finally {
                rightFork.unlock();
                leftFork.unlock();
            }
            try {
                System.out.println(Thread.currentThread().getName() + " is thinking now");
                Thread.sleep(random.nextInt(1000, 5000));
            } catch (InterruptedException ignored) {}
        }
    }

    ReentrantLock leftFork;
    ReentrantLock rightFork;
}
