package org.example.bycicon;

import Database.DatabaseManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.sql.SQLException;

@SpringBootApplication
public class ByciconApplication {
    public static void main(String[] args) {
        try {
            DatabaseManager.Boot_DB();
            TryToBuy.start();
            System.out.println("Manual DB connection successful!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        SpringApplication.run(ByciconApplication.class, args);
    }
}
