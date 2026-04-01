package org.example.bycicon;

import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Fetch_Categories {
    public static JSONObject Categories;
    private static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public static void fetch_Categories() {
        System.out.println("Starting to fetch categories...");

        Runnable task = () -> {
            try {
                // Full path including file extension
                String path = System.getProperty("user.home") + "/Bicycon/Data/Categories.json";
                String content = new String(Files.readAllBytes(Paths.get(path)));

                Categories = new JSONObject(content);

            } catch (FileNotFoundException e) {
                System.err.println("File not found!");
            } catch (Exception e) {
                e.printStackTrace();
            }
        };

        // fetch immediately, then every 3 seconds
        scheduler.scheduleAtFixedRate(task, 0, 20, TimeUnit.MINUTES);
    }
}