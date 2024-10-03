package ru.nsu.kuklin;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Main {
    public static void main(String[] args) {
        var widgetStorageCapacity = 100;
        var widgetSemaphores = new Semaphore[3];
        // Storage is empty at the start
        for (int i = 0; i < widgetSemaphores.length; i++) {
            widgetSemaphores[i] = new Semaphore(widgetStorageCapacity);
            widgetSemaphores[i].drainPermits();
        }
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1000);
                    widgetSemaphores[0].release();
                    System.out.println("Produced widget A");
                } catch (Exception e) {
                    System.out.println("Exception? " + e);
                }
            }
        }).start();
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(2000);
                    widgetSemaphores[1].release();
                    System.out.println("Produced widget B");
                } catch (Exception e) {
                    System.out.println("Exception? " + e);
                }
            }
        }).start();
        new Thread(() -> {
            while (true) {
                try {
                    widgetSemaphores[0].acquire();
                    widgetSemaphores[1].acquire();
                    Thread.sleep(3000);
                    widgetSemaphores[2].release();
                    System.out.println("Produced widget C");
                } catch (Exception e) {
                    System.out.println("Exception? " + e);
                }
            }
        }).start();
    }
}