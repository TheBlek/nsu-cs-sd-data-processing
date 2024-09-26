package ru.nsu.kuklin;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        Thread[] children = new Thread[4];
        var strings = new String[]{"Hello", "my", "people", "!"};
        for (int childId = 0; childId < children.length; childId++) {
            strings[childId] = Integer.toString(childId);
            var local = strings.clone();
            var id = childId;
            children[childId] = new Thread(() -> {
                System.out.printf("Thread Id = %d: ", id);
                printStrings(local);
            });
            children[childId].start();
        }
    }
    private static void printStrings(String[] input) {
        for (var str : input) {
            System.out.printf("%s ", str);
        }
        System.out.println();
    }
}