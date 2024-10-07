package ru.nsu.kuklin;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        Thread child = new Thread(() -> {
            long start = System.currentTimeMillis();
            while (!Thread.currentThread().isInterrupted()) {
                System.out.println("Please let me live");
            }
            long end = System.currentTimeMillis();
            System.out.printf("Alive for %d ms", end - start);
        });
        child.start();
        Thread.sleep(2000);
        child.interrupt();
        child.join();
    }
}