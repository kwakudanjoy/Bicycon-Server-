package org.example.omart;

import Database.DatabaseManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.sql.SQLException;

@SpringBootApplication
public class OmartApplication {
    public static void main(String[] args) {
        try {
            DatabaseManager.init();
            DatabaseManager.Boot_DB();
            TryToBuy.start();
            System.out.println("Manual DB connection successful!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        SpringApplication.run(OmartApplication.class, args);
    }
}