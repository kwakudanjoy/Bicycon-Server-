package Database;

import org.example.bycicon.Fetch_Categories;
import org.example.bycicon.SHA256;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.print.DocFlavor;
import java.io.File;
import java.sql.*;

public class DatabaseManager {

    private static final String DB_NAME = "B_V1";
    private static final String URL_NO_DB = "jdbc:mysql://localhost:3306/?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String URL_WITH_DB = "jdbc:mysql://localhost:3306/" + DB_NAME + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String ROOT_USER = "root";
    private static final String ROOT_PASSWORD = "Vrd3115$23";
    private static final String APP_USER = "bicycon_admin";
    private static final String APP_PASSWORD = "Vrd3115$23";
    private static Connection mainConnection;

    /* Application directories */
    public static File BicyconProfile =
            new File(System.getProperty("user.home") + File.separator + "Bicycon" + File.separator + "Profile");

    public static File ProductImages =
            new File(System.getProperty("user.home") + File.separator + "Bicycon" + File.separator + "Products");

    private static File Data =
            new File(System.getProperty("user.home") + File.separator + "Bicycon" + File.separator + "Data");



    /* =========================================================
       BOOT DATABASE SYSTEM
       ========================================================= */
    public static void Boot_DB() throws SQLException {
        Fetch_Categories.fetch_Categories();
        createFileDirectory();
        create_DB_User();
        createDatabaseIfNotExists();
        startMainConnection();
        createAccountTable();
        CreateProductTable();
        System.out.println("Database Boot Completed");
    }



    /* =========================================================
       CREATE DATABASE USER
       ========================================================= */
    private static void create_DB_User() throws SQLException {

        try (Connection conn = DriverManager.getConnection(URL_NO_DB, ROOT_USER, ROOT_PASSWORD);
             Statement stm = conn.createStatement()) {
            String createUser =
                    "CREATE USER IF NOT EXISTS '" + APP_USER + "'@'localhost' IDENTIFIED BY '" + APP_PASSWORD + "'";
            stm.executeUpdate(createUser);
            String grant =
                    "GRANT ALL PRIVILEGES ON " + DB_NAME + ".* TO '" + APP_USER + "'@'localhost'";
            stm.executeUpdate(grant);
            stm.executeUpdate("FLUSH PRIVILEGES");
            System.out.println("Database user checked/created");

        }
    }



    /* =========================================================
       CREATE DATABASE
       ========================================================= */
    private static void createDatabaseIfNotExists() throws SQLException {

        try (Connection conn = DriverManager.getConnection(URL_NO_DB, ROOT_USER, ROOT_PASSWORD);
             Statement stm = conn.createStatement()) {
            String createDB = "CREATE DATABASE IF NOT EXISTS " + DB_NAME;
            stm.executeUpdate(createDB);
            System.out.println("Database checked/created: " + DB_NAME);
        }
    }

    /* =========================================================
       START MAIN CONNECTION
       ========================================================= */
    private static void startMainConnection() throws SQLException {

        mainConnection = DriverManager.getConnection(
                URL_WITH_DB,
                APP_USER,
                APP_PASSWORD
        );

        System.out.println("Connected to database: " + DB_NAME);
    }



    /* =========================================================
       CLOSE CONNECTION
       ========================================================= */
    public static void close() throws SQLException {
        if (mainConnection != null && !mainConnection.isClosed()) {
            mainConnection.close();
        }
    }



    /* =========================================================
       EXECUTE SQL
       ========================================================= */
    public static void executeSQL(String sql) throws SQLException {
        try (Statement stm = mainConnection.createStatement()) {
            stm.execute(sql);
        }
    }



    /* =========================================================
       GET CONNECTION
       ========================================================= */
    public static Connection getConnection() {
        return mainConnection;
    }



    /* =========================================================
       CREATE ACCOUNT TABLE
       ========================================================= */
    private static void createAccountTable() throws SQLException {

        String query = """
            CREATE TABLE IF NOT EXISTS Accounts_Table (
                id INT AUTO_INCREMENT PRIMARY KEY,
                UserID VARCHAR(20) NOT NULL UNIQUE,
                UserName VARCHAR(100) NOT NULL,
                UserPassword VARCHAR(255) NOT NULL,
                AccountComplete VARCHAR(3),
                ProfileUrl VARCHAR(225),
                Phone VARCHAR(225),
                Email VARCHAR(150),
                CountryCode VARCHAR(10),
                Account_Time_Stamp VARCHAR(225)
            )
            """;

        executeSQL(query);

        System.out.println("Accounts_Table checked/created");
    }

    private static void CreateProductTable () throws SQLException {

        String query = """
                CREATE TABLE IF NOT EXISTS Product_Table (
                  id INT AUTO_INCREMENT PRIMARY KEY,
                  OwnerID VARCHAR(20),
                  ProductID VARCHAR(20),
                  ProductName VARCHAR (2000),
                  ProductPrice VARCHAR (2000),
                  ProductCategory VARCHAR (20),
                  ProductDescription VARCHAR(2000),
                  ProductImageUrl VARCHAR(200)
                )
                """;

        executeSQL(query);
    }


    /* =========================================================
       CONFIRM LOGIN
       ========================================================= */
    public static String Confirm_Logins(String USER, String PASSWORD) throws SQLException {
        JSONObject Result = new JSONObject();

        String QUERY = """
            SELECT UserPassword
            FROM Accounts_Table
            WHERE UserID = ?
            """;

        try (PreparedStatement ps = getConnection().prepareStatement(QUERY)) {
            ps.setString(1, USER);
            ResultSet rst = ps.executeQuery();
            if (rst.next()) {

                String storedPassword = rst.getString("UserPassword");

                if (SHA256.hash(PASSWORD).equals(storedPassword)){
                    Result.put("status","OK");
                    return "OK";
                }else {
                    return "!OK";
                }
            }else {
                return "!USER";
            }
        }
    }



    /* =========================================================
       INSERT NEW ACCOUNT
       ========================================================= */
    public static String InsertNewAccount(JSONObject data) throws SQLException {

        String query = """
                INSERT INTO Accounts_Table
                (UserID, UserName, UserPassword, AccountComplete, Account_Time_Stamp)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (PreparedStatement stm = getConnection().prepareStatement(query)) {

            stm.setString(1, data.getString("User-ID"));
            stm.setString(2, data.getString("User-Name"));
            stm.setString(3, data.getString("User-Password"));
            stm.setString(4, "NO");
            stm.setString(5, data.getString("Time-Stamp"));
            stm.executeUpdate();

            return "OK";
        }
    }

    public static String CompleteAccount(JSONObject data) throws SQLException {
        String query = """
        UPDATE Accounts_Table
        SET AccountComplete = ?, Phone = ?, Email = ?, CountryCode = ?
        WHERE UserID = ?
    """;

        try (PreparedStatement stm = getConnection().prepareStatement(query)) {

            stm.setString(1, "YES"); // mark account complete
            stm.setString(2, data.getString("Phone"));
            stm.setString(3, data.getString("Email"));
            stm.setString(4, data.getString("CountryCode"));
            stm.setString(5, data.getString("UserId")); // WHERE condition

            int rows = stm.executeUpdate();
            if (rows > 0) {
                return "OK";
            } else {
                return "User not found";
            }
        }
    }

    // ============= Storing profile pic ==============

    public static String StoreProfileLocation(String UserID , String url){

        String query = """
                UPDATE Accounts_Table SET ProfileUrl = ? WHERE UserID = ?
                """;
        try (PreparedStatement stm = getConnection().prepareStatement(query)){
            stm.setString(1, url);
            stm.setString(2,UserID);

            int row = stm.executeUpdate();
            if (row > 0){
                return "OK";
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public static String StoreNewProduct(JSONObject Data) throws SQLException {


        String Query = """
                INSERT INTO Product_Table (
                OwnerID,
                ProductID,
                ProductName,
                ProductPrice,
                ProductCategory,
                ProductDescription,
                ProductImageUrl) 
                VALUES (?,?,?,?,?,?,?)
                """;

        try(PreparedStatement statement = getConnection().prepareStatement(Query)){
            statement.setString(1,Data.getString("owner"));
            statement.setString(2,Data.getString("ProdID"));
            statement.setString(3,Data.getString("name"));
            statement.setString(4,Data.getString("price"));
            statement.setString(5,Data.getString("Category"));
            statement.setString(6,Data.getString("Description"));
            statement.setString(7,Data.getString("ProdUrl"));

            statement.executeUpdate();
        }

        return "OK";
    }

    // GET ALL PRODUCT OF A RETAILER
    public static String GetAllMyProduct(String OwnerID) {
        JSONArray allProducts = new JSONArray();

        String query = """
        SELECT ProductID,
               ProductName,
               ProductPrice,
               ProductDescription,
               ProductImageUrl
        FROM Product_Table
        WHERE OwnerID = ?
    """;

        try (PreparedStatement stm = getConnection().prepareStatement(query)) {

            stm.setString(1, OwnerID);
            ResultSet rs = stm.executeQuery();

            while (rs.next()) { // ✅ FIX 1: loop through all rows
                JSONObject product = new JSONObject(); // ✅ FIX 2: create new object each time

                product.put("Id", rs.getString("ProductID"));
                product.put("name", rs.getString("ProductName"));
                product.put("price", rs.getString("ProductPrice"));
                product.put("description", rs.getString("ProductDescription"));
                product.put("Url", rs.getString("ProductImageUrl"));

                allProducts.put(product);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return allProducts.toString();
    }

    //Update my profile
    public static String updateMyProfile(String userID, String imageName) throws SQLException {

        String selectQuery = "SELECT ProfileUrl FROM Accounts_Table WHERE UserID = ?";
        String updateQuery = "UPDATE Accounts_Table SET ProfileUrl = ? WHERE UserID = ?";

        try (PreparedStatement stm = getConnection().prepareStatement(selectQuery)) {
            stm.setString(1, userID);
            ResultSet rs = stm.executeQuery();

            if (rs.next()) {
                String oldImage = rs.getString("ProfileUrl");
                System.out.println(oldImage );
                // 🔹 Try deleting old image (if exists)
                if (oldImage != null && !oldImage.isEmpty()) {
                    File file = new File(DatabaseManager.BicyconProfile + File.separator + oldImage);

                    if (file.exists()) {
                        boolean deleted = file.delete();
                        System.out.println("file deleted");
                        if (!deleted) {
                            System.out.println("Warning: Failed to delete old image");
                        }
                    }
                }

                // 🔹 Always update DB (don’t depend on file delete)
                try (PreparedStatement stm2 = getConnection().prepareStatement(updateQuery)) {
                    stm2.setString(1, imageName);
                    stm2.setString(2, userID);

                    int row = stm2.executeUpdate();
                    if (row > 0) {
                        return "OK";
                    }
                }
            }
        }

        return null;
    }

    public static String FetchMyData(String userID) throws SQLException {

        JSONObject result = new JSONObject();

        String query = """
            SELECT UserName, ProfileUrl, Phone, Email, AccountComplete
            FROM Accounts_Table
            WHERE UserID = ?
            """;

        try (PreparedStatement stm = getConnection().prepareStatement(query)) {

            stm.setString(1, userID);
            ResultSet rs = stm.executeQuery();

            if (rs.next()) {

                result.put("User-Name", rs.getString("UserName"));
                result.put("account_completed", rs.getString("AccountComplete"));

                String phone = rs.getString("Phone");
                if (phone != null) result.put("Phone", phone);

                String email = rs.getString("Email");
                if (email != null) result.put("Email", email);

                String profile = rs.getString("ProfileUrl");
                if (profile != null) result.put("profilePic", profile);

            }
        }

        return result.toString();
    }

    public static String GET_Products_PER_KEY(String key) throws SQLException {

        JSONArray products = new JSONArray();

        // ✅ Get categories safely from JSONObject
        JSONArray categoriesArray = Fetch_Categories.Categories.optJSONArray("Product_Categories");

        boolean found = false;

        if (categoriesArray != null) {
            for (int i = 0; i < categoriesArray.length(); i++) {
                if (categoriesArray.getString(i).equalsIgnoreCase(key)) {
                    found = true;
                    break;
                }
            }
        }

        // ❌ If category not found → return empty result
        if (!found) {
            System.out.println("Invalid category: " + key);
            return products.toString();
        }

        // ✅ Build query
        String query;
        boolean isAll = key.equalsIgnoreCase("All");

        if (isAll) {
            query = "SELECT * FROM Product_Table";
        } else {
            query = "SELECT * FROM Product_Table WHERE ProductCategory = ?";
        }

        try (PreparedStatement stm = getConnection().prepareStatement(query)) {

            // ✅ Set parameter only if needed
            if (!isAll) {
                stm.setString(1, key);
            }

            ResultSet rs = stm.executeQuery();

            while (rs.next()) {

                JSONObject product = new JSONObject();

                product.put("ImageUrl", rs.getString("ProductImageUrl"));
                product.put("Name", rs.getString("ProductName"));
                product.put("Price", rs.getString("ProductPrice"));
                product.put("Description", rs.getString("ProductDescription"));

                String retailerID = rs.getString("OwnerID");

                // ✅ Fetch retailer info
                String retailerQuery = """
                SELECT UserName, ProfileUrl
                FROM Accounts_Table
                WHERE UserID = ?
            """;

                try (PreparedStatement stm1 = getConnection().prepareStatement(retailerQuery)) {

                    stm1.setString(1, retailerID);
                    ResultSet rs1 = stm1.executeQuery();

                    if (rs1.next()) {
                        product.put("RetailerName", rs1.getString("UserName"));
                        product.put("RetailerID", retailerID);
                        product.put("profilePic", rs1.getString("ProfileUrl"));
                    }
                }

                // ✅ Add product to array
                products.put(product);
            }
        }

        return products.toString();
    }


    public static String UpdateEmail (String UserID , String NewEmail) throws SQLException {
        String Query = """
                UPDATE Accounts_Table SET Email = ?  WHERE UserID = ?
                """;

        try (PreparedStatement stm = getConnection().prepareStatement(Query)){

            stm.setString(1,NewEmail);
            stm.setString(2,UserID);

            int row = stm.executeUpdate();
            System.out.println("OK");
            if (row > 0){
                return "OK";
            }

        }
        return  "!OK";
    }

    public static String GET_Retailer_Email_Phone(String Retailer_ID) throws SQLException{
        JSONObject Result = new JSONObject();
        String Query = """
                SELECT Email,Phone FROM Accounts_Table WHERE UserID = ? 
                """;

        try (PreparedStatement stm = getConnection().prepareStatement(Query)){
            stm.setString(1,Retailer_ID);

            ResultSet RS = stm.executeQuery();
            if (RS.next()){
                Result.put("Email",RS.getString("Email"));
                Result.put("Phone",RS.getString("Phone"));
            }
        }

        return Result.toString();
    }

    public static String Delete_My_Product (String Product_ID) throws SQLException{
        String query = "SELECT ProductImageUrl FROM Product_Table WHERE ProductID = ?";

        try (PreparedStatement stm1 = getConnection().prepareStatement(query)){
            stm1.setString(1,Product_ID);
            ResultSet rs = stm1.executeQuery();
            if (rs.next()){
                String oldImage = rs.getString("ProductImageUrl");
                if (oldImage != null && !oldImage.isEmpty()){
                    File file = new File(DatabaseManager.ProductImages + File.separator + oldImage);

                    if (file.exists()){
                        boolean deleted = file.delete();
                        System.out.println("file deleted");
                        if (!deleted) {
                            System.out.println("Warning: Failed to delete old image");
                        }
                    }
                }
            }

        }

        String Query = """
                DELETE FROM Product_Table WHERE ProductID = ?
                """;
        try (PreparedStatement stm = getConnection().prepareStatement(Query)){
            stm.setString(1,Product_ID);
            if(stm.executeUpdate() > 0){
                return "OK";
            }else {
                return "!OK";
            }
        }
    }

    public static String Get_Category (String Product_ID) throws SQLException{
        JSONObject Result = new JSONObject();
        String Query = """
                SELECT ProductCategory FROM Product_Table WHERE ProductID = ?
                """;
        try (PreparedStatement stm = getConnection().prepareStatement(Query)){
            stm.setString(1,Product_ID);
            ResultSet RS = stm.executeQuery();
            if (RS.next()){
                Result.put("category",RS.getString("ProductCategory"));
            }
        }
        return Result.toString();
    }

    public static String Update_Prod_Data(JSONObject Data) throws SQLException {
        JSONObject Result = new JSONObject();
        String QUERY = """
        UPDATE Product_Table 
        SET ProductName = ?, ProductPrice = ?, ProductDescription = ?
        WHERE ProductID = ?
        """;

        try (PreparedStatement stm = getConnection().prepareStatement(QUERY)) {
            stm.setString(1, Data.getString("NewName"));
            stm.setDouble(2, Data.getDouble("NewPrice")); // if your column is numeric
            stm.setString(3, Data.getString("NewDescription"));
            stm.setString(4, Data.getString("ProductID"));

            int i = stm.executeUpdate();

            if (i > 0) {
                Result.put("status", "OK");
            } else {
                Result.put("status", "NOT-UPDATED"); // handles case where no rows matched
            }
        }

        return Result.toString();
    }

    public static String Update_Prod_Data_With_Image(String Image_Name, JSONObject Data) throws SQLException{
         String select_image_Query = """
                 SELECT ProductImageUrl FROM Product_Table WHERE ProductID = ?
                 """;
         String update_Query = """
                 UPDATE Product_Table SET ProductName = ?,ProductPrice = ?,ProductDescription = ?,ProductImageUrl = ?
                 WHERE ProductID = ?
                 """;

         try (PreparedStatement stm = getConnection().prepareStatement(select_image_Query)){
             stm.setString(1,Data.getString("ProductID"));
             ResultSet RS = stm.executeQuery();

             if (RS.next()) {
                 String oldImage = RS.getString("ProductImageUrl");
                 System.out.println(oldImage );
                 // 🔹 Try deleting old image (if exists)
                 if (oldImage != null && !oldImage.isEmpty()) {
                     File file = new File(DatabaseManager.ProductImages + File.separator + oldImage);

                     if (file.exists()) {
                         boolean deleted = file.delete();
                         System.out.println("file deleted");
                         if (!deleted) {
                             System.out.println("Warning: Failed to delete old image");
                         }
                     }
                 }

                 //UPDATE DB
                 try (PreparedStatement stm1 = getConnection().prepareStatement(update_Query)){
                     stm1.setString(1,Data.getString("NewName"));
                     stm1.setString(2,Data.getString("NewPrice"));
                     stm1.setString(3,Data.getString("NewDescription"));
                     stm1.setString(4,Image_Name);
                     stm1.setString(5,Data.getString("ProductID"));

                     int row = stm1.executeUpdate();
                     if (row > 0) {
                         return "OK";
                     }
                 }
             }
         }
        return null;
    }

    public static String GET_Retailer_Email_Profile_Phone(String Retailer_ID)throws SQLException{
        JSONObject Result = new JSONObject();
        String Query = """
                SELECT Email,Phone,ProfileUrl FROM Accounts_Table WHERE UserID = ? 
                """;
        try (PreparedStatement stm = getConnection().prepareStatement(Query)){
            stm.setString(1,Retailer_ID);
            ResultSet RS = stm.executeQuery();;
            if (RS.next()){
                Result.put("Email",RS.getString("Email"));
                Result.put("Phone",RS.getString("Phone"));
                Result.put("ProfilePic",RS.getString("ProfileUrl"));
            }
        }
        return Result.toString();
    }

    /* =========================================================
       CREATE DIRECTORIES
       ========================================================= */
    private static void createFileDirectory() {

        BicyconProfile.mkdirs();
        ProductImages.mkdirs();
        Data.mkdirs();
    }

}