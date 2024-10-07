package ru.nsu.kuklin;

public class Main {
    public static void main(String[] args) {
        var founder = new Founder(new Company(20));
        founder.start();
    }
}