package ru.nsu.kuklin;

import java.io.Serial;
import java.util.ArrayList;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class Main {
    public static void main(String[] args) {
        int threadCount = 32;
        long totalTermCount = 10_000_000_000L;
        long termCountPerThread = totalTermCount / (long)threadCount;
        var workers = new ArrayList<SeriesCalculator>(threadCount);
        CyclicBarrier calculationBarrier = new CyclicBarrier(threadCount + 1);
        for (long i = 0; i < threadCount; i++) {
            workers.add(new SeriesCalculator(i * termCountPerThread, (i + 1) * termCountPerThread, calculationBarrier));
        }
        for (final Runnable worker : workers) {
            new Thread(worker).start();
        }
        try {
            calculationBarrier.await();
        } catch (Exception e) {
            System.out.println("Failed to wait on barrier: " + calculationBarrier);
        }
        double res = 0;
        for (final SeriesCalculator worker : workers) {
            res += worker.getResult();
        }
        System.out.printf("Result: %.15f\n", res * 4);
    }
}