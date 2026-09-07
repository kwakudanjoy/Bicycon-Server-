package store_Template;

import Database.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
public class RetailerData {

    public String retailerName;
    public String retailerId;
    public String retailerEmail;
    public String retailerPhone;
    public String retailerProfile;

    public RetailerData(String retailerID) throws SQLException {

        String DB_Query = """
                SELECT * FROM Accounts_Table WHERE UserID = ?
                """;

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement stm = conn.prepareStatement(DB_Query)) {
            stm.setString(1, retailerID);

            ResultSet RS = stm.executeQuery();
            if (RS.next()) {
                this.retailerName = RS.getString("BusinessName");
                this.retailerId = RS.getString("UserID");
                this.retailerEmail = RS.getString("Email");
                this.retailerPhone = RS.getString("Phone");

                String profile = RS.getString("ProfileUrl");
                if (profile != null) this.retailerProfile = profile;
            }
        }
    }

    public String getRetailerId() { return retailerId; }
}