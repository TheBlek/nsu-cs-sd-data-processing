package ru.nsu.kuklin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Main {

    private static final int LINES_PER_SCREEN = 25;
    private static final List<String> lines = new ArrayList<>();

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java SimpleHttpClient <URL>");
            return;
        }

        try {
            Thread readerThread = getReaderThread(args[0]);

            var currentLine = 0;
            while (readerThread.isAlive() || currentLine < lines.size()) {
                synchronized (lines) {
                    if (lines.size() > currentLine) {
                        System.out.println(lines.get(currentLine));
                        currentLine++;
                    }
                }

                if (currentLine % LINES_PER_SCREEN == LINES_PER_SCREEN - 1) {
                    System.out.print("Press enter to scroll down...");
                    while (System.in.read() != '\n') {
                        System.out.println("Iteration");
                    }
                    currentLine++;
                }
            }

            readerThread.join();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static Thread getReaderThread(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

        Thread readerThread = new Thread(() -> {
            try {
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    synchronized (lines) {
                        lines.add(inputLine); // Save the line to the list
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                connection.disconnect();
            }
        });

        readerThread.start();
        return readerThread;
    }
}
