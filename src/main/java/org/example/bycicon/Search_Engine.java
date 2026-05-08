package org.example.bycicon;

import Database.DatabaseManager;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class Search_Engine {

    // =========================
    // TIME FORMAT UTILITY
    // =========================
    public static String PostedAt(LocalDateTime createdAt) {
        LocalDateTime now = LocalDateTime.now();

        long seconds = ChronoUnit.SECONDS.between(createdAt, now);
        if (seconds < 60) return "Just now";

        long minutes = ChronoUnit.MINUTES.between(createdAt, now);
        if (minutes < 60) return minutes + " minutes ago";

        long hours = ChronoUnit.HOURS.between(createdAt, now);
        if (hours < 24) return hours + " hours ago";

        long days = ChronoUnit.DAYS.between(createdAt, now);
        if (days < 7) return days + " days ago";

        long weeks = days / 7;
        if (days < 30) return weeks + " weeks ago";

        long months = days / 30;
        if (days < 365) return months + " months ago";

        long years = days / 365;
        return years + " years ago";
    }

    // =========================
    // MAIN SEARCH FUNCTION
    // =========================
    public static String Search(String input) throws SQLException {

        JSONArray products = new JSONArray();

        String pattern = "%" + input.trim() + "%";

        String query = """
            SELECT 
                p.ProductID,
                p.ProductName,
                p.ProductPrice,
                p.ProductDescription,
                p.ProductImageUrl,
                p.ProductTimeStamp,
                p.TryToBuyCount,
                p.OwnerID,
                p.ProductCategory,

                a.UserName,
                a.ProfileUrl,
                a.UserID,
                a.CurrencyCode,

                COALESCE(o.BuyCount, 0) AS BuyCount,

                (
                    (CASE WHEN a.UserID LIKE ? THEN 2 ELSE 0 END) +
                    (CASE WHEN a.UserName LIKE ? THEN 2 ELSE 0 END) +
                    (CASE WHEN p.ProductName LIKE ? THEN 3 ELSE 0 END) +
                    (CASE WHEN p.ProductCategory LIKE ? THEN 2 ELSE 0 END) +
                    (CASE WHEN p.ProductDescription LIKE ? THEN 1 ELSE 0 END)
                ) AS SearchScore

            FROM Product_Table p

            LEFT JOIN Accounts_Table a 
                ON p.OwnerID = a.UserID

            LEFT JOIN (
                SELECT ProductID, COUNT(*) AS BuyCount
                FROM Order_Table
                GROUP BY ProductID
            ) o ON p.ProductID = o.ProductID

            WHERE 
                a.UserID LIKE ?
                OR a.UserName LIKE ?
                OR p.ProductName LIKE ?
                OR p.ProductCategory LIKE ?
                OR p.ProductDescription LIKE ?

            ORDER BY 
                (SearchScore + (COALESCE(o.BuyCount,0) * 3) + p.TryToBuyCount) DESC
        """;

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement stm = conn.prepareStatement(query)) {

            // =========================
            // SET PARAMETERS (ONLY 10)
            // =========================

            // scoring (5)
            for (int i = 1; i <= 5; i++) {
                stm.setString(i, pattern);
            }

            // filtering (5)
            for (int i = 6; i <= 10; i++) {
                stm.setString(i, pattern);
            }

            ResultSet rs = stm.executeQuery();

            while (rs.next()) {

                JSONObject product = new JSONObject();

                // =========================
                // PRODUCT DATA
                // =========================
                product.put("prodID", rs.getString("ProductID"));
                product.put("Name", rs.getString("ProductName"));
                product.put("Price", rs.getInt("ProductPrice"));
                product.put("Description", rs.getString("ProductDescription"));
                product.put("ImageUrl", rs.getString("ProductImageUrl"));

                product.put("postedAt",
                        PostedAt(LocalDateTime.parse(rs.getString("ProductTimeStamp")))
                );

                // =========================
                // RETAILER DATA
                // =========================
                product.put("RetailerName", rs.getString("UserName"));
                product.put("RetailerID", rs.getString("UserID"));
                product.put("profilePic", rs.getString("ProfileUrl"));
                product.put("currencyCode", Fetch_Categories.CountryToCurrencyMap.get(rs.getString("CurrencyCode")));

                // =========================
                // STATS
                // =========================
                int buyCount = rs.getInt("BuyCount");
                int tryCount = rs.getInt("TryToBuyCount");
                int searchScore = rs.getInt("SearchScore");

                int finalScore = searchScore + (buyCount * 3) + tryCount;

                product.put("BuyCount", buyCount);
                product.put("TryToBuyCount", tryCount);
                product.put("SearchScore", searchScore);
                product.put("FinalScore", finalScore);

                products.put(product);
            }

        }

        return products.toString();
    }
}