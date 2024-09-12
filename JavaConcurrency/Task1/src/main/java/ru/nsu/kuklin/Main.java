package ru.nsu.kuklin;

public class Main {
    public static void main(String[] args) {
        new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                System.out.println("Child " + i);
            }
        }).start();
        for (int i = 0; i < 10; i++) {
            System.out.println("Parent " + i);
        }
    }
}