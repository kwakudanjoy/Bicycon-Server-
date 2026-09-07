package org.example.omart;

import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Fetch_Config_Data {
    public static String PRODUCTION_FOLDER = "OMart";
    public static JSONObject Categories;
    public static JSONObject CountryToCurrencyMap;
    public static JSONObject DataBaseConfigs;
    public static JSONObject SERVER_ENDPOINTS;
    private static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public static void fetch_Categories() {
        System.out.println("Starting to fetch subscription data...");

        Runnable task = () -> {
            try {
                // Full path including file extension
                String categoryPath = System.getProperty("user.home") + "/"+ PRODUCTION_FOLDER +"/Data/Categories.json";
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
            String countryToCurrencyMapPath = System.getProperty("user.home") + "/"+ PRODUCTION_FOLDER + "/Data/countryToCurrencyMap.json";
            String countryToCurrencyMapContent = new String(Files.readAllBytes(Paths.get(countryToCurrencyMapPath)));
            CountryToCurrencyMap = new JSONObject(countryToCurrencyMapContent);

        } catch (FileNotFoundException e) {
            System.err.println("country Map File not found!");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public static void FetchDataBaseConfigs() {

        System.out.println("Starting to fetch Data base config data...");

        Runnable task = () -> {

            try {

                String categoryPath =
                        System.getProperty("user.home")
                                + "/" + PRODUCTION_FOLDER + "/Data/databaseConfigs.json";

                String categoryContent =
                        new String(Files.readAllBytes(Paths.get(categoryPath)));

                DataBaseConfigs = new JSONObject(categoryContent);

            } catch (Exception e) {
                System.err.println("country Database file not found! 676667777");
                e.printStackTrace();
            }
        };

        // LOAD IMMEDIATELY
        task.run();

        // THEN refresh every 20 minutes
        scheduler.scheduleAtFixedRate(
                task,
                20,
                20,
                TimeUnit.MINUTES
        );
    }

    public static void Fetch_Server_url(){
        System.out.println("Starting to fetch subscription data...");

        Runnable task = () -> {
            try {
                // Full path including file extension
                String URLPath = System.getProperty("user.home") + "/" + PRODUCTION_FOLDER + "/Data/serverIp.json";
                String content = new String(Files.readAllBytes(Paths.get(URLPath)));
                SERVER_ENDPOINTS = new JSONObject(content);

            } catch (FileNotFoundException e) {
                System.err.println("SERVER ENDPOINTS FILE NOT FOUND");
            } catch (Exception e) {
                e.printStackTrace();
            }
        };

        // fetch immediately, then every 3 seconds
        scheduler.scheduleAtFixedRate(task, 0, 20, TimeUnit.MINUTES);
    }
}