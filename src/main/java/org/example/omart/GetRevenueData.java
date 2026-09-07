package org.example.omart;

import Database.DatabaseManager;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class GetRevenueData {

    public static String GetData(JSONObject data) throws SQLException {
        JSONObject Result = new JSONObject();


        //Get the strings
        String fromDateStr = data.getString("fromDate");
        String toDataStr = data.getString("toDate");

        // 2. Define the format (matches "yyyy-MM-dd")
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;

        // 3. Parse to a proper Date object
        LocalDate fromDate = LocalDate.parse(fromDateStr,formatter);
        LocalDate toDate = LocalDate.parse(toDataStr,formatter);

        //SQL query to fetch order data from the database
        String SqlQuery = """
             
                SELECT
                OrderID ,
                ProductID ,
                Date_Of_Order, 
                Total_Amount,
                Order_Status FROM Order_Table
                WHERE OwnerID = ?
                """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stm = conn.prepareCall(SqlQuery)){
            stm.setString(1,data.getString("userid"));

            ResultSet rs = stm.executeQuery();
            while (rs.next()) {
                String dateStr = rs.getString("Date_Of_Order");
                LocalDate selectedDate = LocalDate.parse(dateStr, formatter);

                // Range Check
                if (!selectedDate.isBefore(fromDate) && !selectedDate.isAfter(toDate)) {
                    String dateKey = String.valueOf(selectedDate);
                    JSONArray dataPerDate;

                    // 1. Create the data row for THIS specific order
                    JSONObject row = new JSONObject();
                    row.put("orderID", rs.getString("OrderID"));
                    row.put("prodID", rs.getString("ProductID"));
                    row.put("orderTotal", rs.getString("Total_Amount"));
                    row.put("orderStatus", rs.getString("Order_Status"));

                    // 2. Check if we already started a list for this date
                    if (Result.has(dateKey)) {
                        // Get the existing list and add the new row
                        dataPerDate = Result.getJSONArray(dateKey);
                        dataPerDate.put(row);
                    } else {
                        // Create a brand new list for this new date
                        dataPerDate = new JSONArray();
                        dataPerDate.put(row);
                        Result.put(dateKey, dataPerDate);
                    }
                }
            }
        }

        return Result.toString();
    }
}
