package ru.nsu.kuklin;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.ThreadLocalRandom;

public class Philosopher implements Runnable {
    public Philosopher(ReentrantLock leftFork, ReentrantLock rightFork, ReentrantLock lackey, Condition forksFreed) {
        this.leftFork = leftFork;
        this.rightFork = rightFork;
        this.lackey = lackey;
        this.forksFreed = forksFreed;
    }

    private void log(String message) {
        System.out.println(Thread.currentThread().getName() + " " + message);
    }

    @Override
    public void run() {
        var random = ThreadLocalRandom.current();
        while (!Thread.currentThread().isInterrupted()) {
            lackey.lock();
            var takenLeft = leftFork.tryLock();
            var takenRight = rightFork.tryLock();
            if (!(takenLeft && takenRight)) {
                if (takenLeft) {
                    leftFork.unlock();
                }
                if (takenRight) {
                    rightFork.unlock();
                }
                try {
                    forksFreed.await();
                } catch (Exception ignored) {}
                lackey.unlock();
                continue;
            }
            lackey.unlock();

            try {
                log("is eating");
                Thread.sleep(random.nextInt(1000, 5000));
            } catch (InterruptedException ignored) {}

//             Why waiting forever? Who tf holds it? himself?
            lackey.lock();
            rightFork.unlock();
            leftFork.unlock();
            forksFreed.signalAll();
            lackey.unlock();

            try {
                log("is thinking");
                Thread.sleep(random.nextInt(1000, 5000));
            } catch (InterruptedException ignored) {}
        }
    }

    ReentrantLock leftFork;
    ReentrantLock rightFork;
    ReentrantLock lackey;
    Condition forksFreed;
}
