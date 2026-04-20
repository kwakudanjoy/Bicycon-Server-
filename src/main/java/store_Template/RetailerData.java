package store_Template;

import Database.DatabaseManager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
public class RetailerData {

    private String retailerName;
    private String retailerId;
    private String retailerEmail;
    private String retailerPhone;
    private String retailerProfile;

    public RetailerData(String retailerID) throws SQLException {

        String DB_Query = """
                SELECT * FROM Accounts_Table WHERE UserID = ?
                """;

        try (PreparedStatement stm = DatabaseManager.getConnection().prepareStatement(DB_Query)) {
            stm.setString(1, retailerID);

            ResultSet RS = stm.executeQuery();
            if (RS.next()) {
                this.retailerName = RS.getString("UserName");
                this.retailerId = RS.getString("UserID");
                this.retailerEmail = RS.getString("Email");
                this.retailerPhone = RS.getString("Phone");

                String profile = RS.getString("ProfileUrl");
                if (profile != null) this.retailerProfile = profile;
            }
        }
    }

    public String getRetailerName() { return retailerName; }
    public String getRetailerId() { return retailerId; }
    public String getRetailerEmail() { return retailerEmail; }
    public String getRetailerPhone() { return retailerPhone; }
    public String getRetailerProfile() { return retailerProfile; }
}