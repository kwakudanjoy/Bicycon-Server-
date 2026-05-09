package org.example.bycicon;

import Database.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TryToBuy {

    private static final ConcurrentLinkedQueue<String> productIds = new ConcurrentLinkedQueue<>();
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static boolean started = false;
    private static boolean errorLogged = false;

    public static void start() {
        if (started) return;
        started = true;

        scheduler.scheduleAtFixedRate(() -> {
            String dbQuery = "UPDATE Product_Table SET TryToBuyCount = TryToBuyCount + 1 WHERE ProductID = ?";

            try (Connection conn = DatabaseManager.getConnection();
                    PreparedStatement statement = conn.prepareStatement(dbQuery)) {

                int batchSize = 0;

                while (!productIds.isEmpty() && batchSize < 100) {
                    String id = productIds.poll();
                    if (id != null) {
                        statement.setString(1, id);
                        statement.addBatch();
                        batchSize++;
                    }
                }

                if (batchSize > 0) {
                    statement.executeBatch();
                }

            } catch (SQLException e) {
                if (!errorLogged) {
                    System.err.println("Database error: " + e.getMessage());
                    errorLogged = true;
                }
            }

        }, 0, 1, TimeUnit.SECONDS);
    }

    public static String addToProducts(String productID) {
        productIds.add(productID);
        return ("OK");
    }
}
