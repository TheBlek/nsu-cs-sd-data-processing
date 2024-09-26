package ru.nsu.kuklin;

import java.util.ArrayList;
import java.util.concurrent.CyclicBarrier;

public class Main {
    public static void main(String[] args) {
        int threadCount = 32;
        long totalTermCount = 100_000_000L;
        long termCountPerThread = totalTermCount / (long)threadCount;
        var workers = new ArrayList<SeriesCalculator>(threadCount);
        var threads = new ArrayList<Thread>(threadCount);
        CyclicBarrier calculationBarrier = new CyclicBarrier(threadCount);
        CyclicBarrier endOfLifeBarrier = new CyclicBarrier(threadCount + 1);
        for (long i = 0; i < threadCount; i++) {
            workers.add(new SeriesCalculator(
                i * termCountPerThread,
                termCountPerThread,
                threadCount * termCountPerThread,
                calculationBarrier,
                endOfLifeBarrier
            ));
        }
        for (final Runnable worker : workers) {
            threads.add(new Thread(worker));
            threads.get(threads.size() - 1).start();
        }
        System.out.println("Started working");
        Runtime.getRuntime().addShutdownHook(
            new Thread() {
                @Override
                public void run() {
                    System.out.println("Shutdown hook run!");
                    for (final Thread thread : threads) {
                        thread.interrupt();
                    }
                    try {
                        endOfLifeBarrier.await();
                    } catch (Exception e) {
                        System.out.println("Failed to wait for threads to end calculation");
                    }
                    double res = 0;
                    for (final SeriesCalculator worker : workers) {
                        res += worker.getResult();
                    }
                    System.out.printf("Result: %.15f\n", res * 4);
                }
            }
        );
    }
}