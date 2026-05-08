package org.example.bycicon;

import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Fetch_Categories {
    public static JSONObject Categories;
    public static JSONObject CountryToCurrencyMap;
    private static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public static void fetch_Categories() {
        System.out.println("Starting to fetch categories...");

        Runnable task = () -> {
            try {
                // Full path including file extension
                String categoryPath = System.getProperty("user.home") + "/Bicycon/Data/Categories.json";
                String categoryContent = new String(Files.readAllBytes(Paths.get(categoryPath)));
                Categories = new JSONObject(categoryContent);

            } catch (FileNotFoundException e) {
                System.err.println("Category File not found!");
            } catch (Exception e) {
                e.printStackTrace();
            }
        };

        // fetch immediately, then every 3 seconds
        scheduler.scheduleAtFixedRate(task, 0, 20, TimeUnit.MINUTES);
    }
    public static void FetchCountryToCurrencyMap (){
        try {
            String countryToCurrencyMapPath = System.getProperty("user.home") + "/Bicycon/Data/countryToCurrencyMap.json";
            String countryToCurrencyMapContent = new String(Files.readAllBytes(Paths.get(countryToCurrencyMapPath)));
            CountryToCurrencyMap = new JSONObject(countryToCurrencyMapContent);

        } catch (FileNotFoundException e) {
            System.err.println("country Map File not found!");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}