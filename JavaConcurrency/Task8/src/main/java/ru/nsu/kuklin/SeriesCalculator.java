package ru.nsu.kuklin;

import java.util.concurrent.CyclicBarrier;

public class SeriesCalculator implements Runnable {
    public SeriesCalculator(long startIndex, long blockSize, long stride, CyclicBarrier calculationBarrier, CyclicBarrier eofBarrier) {
        this.startIndex = startIndex;
        this.blockSize = blockSize;
        this.stride = stride;
        this.calculationBarrier = calculationBarrier;
        this.endOfLifeBarrier = eofBarrier;
    }

    public double getResult() {
        return result;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            long endIndex = startIndex + blockSize;
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
                calculationBarrier.await();
            } catch (Exception ignored) {
            }
            startIndex += stride;
        }

        try {
            endOfLifeBarrier.await();
        } catch (Exception e) {
            System.out.println("Failed to wait for end of life barrier");
        }
    }

    private double result = 0;
    private long startIndex;
    private long blockSize;
    private long stride;
    private CyclicBarrier calculationBarrier;
    private CyclicBarrier endOfLifeBarrier;
}
