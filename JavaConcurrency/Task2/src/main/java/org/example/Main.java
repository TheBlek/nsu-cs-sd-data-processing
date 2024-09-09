package org.example;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        var child = new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                System.out.println("Child " + i);
            }
        });
        child.start();
        child.join();
        for (int i = 0; i < 10; i++) {
            System.out.println("Parent " + i);
        }
    }
}