package ru.nsu.kuklin;

import java.util.concurrent.CyclicBarrier;

public class SeriesCalculator implements Runnable {
    public SeriesCalculator(long startIndex, long endIndex, CyclicBarrier barrier) {
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.barrier = barrier;
    }

    public double getResult() {
        return result;
    }

    @Override
    public void run() {
        for (long i = startIndex; i < endIndex; i += 2) {
            double term = 1 / (double) (2 * i + 1);
            result += term;
            term = 1 / (double) (2 * i + 3);
            result -= term;
        }
        if ((endIndex - startIndex) % 2 == 1) {
            result += 1 / (double) (2 * (endIndex - 1) + 3);
        }
        try {
            barrier.await();
        } catch (Exception ignored) {}
    }

    private double result = 0;
    private long startIndex;
    private long endIndex;
    private CyclicBarrier barrier;
}
