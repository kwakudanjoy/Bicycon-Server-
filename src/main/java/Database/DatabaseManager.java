package Database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.example.bycicon.Fetch_Categories;
import org.example.bycicon.SHA256;
import org.example.bycicon.Search_Engine;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.File;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class DatabaseManager {

    /* =========================================================
       DYNAMIC CONSTANTS
       ========================================================= */
// 1. Get values from Environment (Cloud) or use your Local defaults
    private static final String DB_NAME = "B_V1";

    private static final String DB_HOST = System.getenv("DB_HOST") != null ? System.getenv("DB_HOST") : "localhost";
    private static final String DB_PORT = System.getenv("DB_PORT") != null ? System.getenv("DB_PORT") : "3306";

    // The "Main" credentials the app will use
    private static final String APP_USER = System.getenv("DB_USER") != null ? System.getenv("DB_USER") : "bicycon_admin";
    private static final String APP_PASSWORD = System.getenv("DB_PASSWORD") != null ? System.getenv("DB_PASSWORD") : "Vrd3115$23";

    // The "Root" credentials (ONLY used locally)
    private static final String ROOT_USER = "root";
    private static final String ROOT_PASSWORD = "Vrd3115$23";

    // The JDBC URLs
    private static final String URL_NO_DB = "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

    private static final String URL_WITH_DB = System.getenv("DB_URL") != null
            ? System.getenv("DB_URL")
            : "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";


    /* =========================================================
       DIRECTORIES
       ========================================================= */
    public static File BicyconProfile =
            new File(System.getProperty("user.home") + "/Bicycon/Profile");

    public static File ProductImages =
            new File(System.getProperty("user.home") + "/Bicycon/Products");

    public static File Data =
            new File(System.getProperty("user.home") + "/Bicycon/Data");

    /* =========================================================
       BOOT SYSTEM
       ========================================================= */
    public static void Boot_DB() throws SQLException {
        Fetch_Categories.fetch_Categories();
        Fetch_Categories.FetchCountryToCurrencyMap();
        createFileDirectory();

        // ONLY attempt to create DB/Users if we are running locally
        if (System.getenv("DB_URL") == null) {
            System.out.println("Local environment detected. Running setup...");
            createDatabaseIfNotExists();
            create_DB_User();
        } else {
            System.out.println("Cloud environment detected. Skipping Root setup.");
        }

        initPool();

        // These are safe to run everywhere because they use "IF NOT EXISTS"
        createAccountTable();
        CreateProductTable();
        CreatePlaceOrderTable();

        System.out.println("Database Boot Completed");
    }


    /* =========================================================
   CREATE DATABASE (SAFE)
   ========================================================= */
    private static void createDatabaseIfNotExists() throws SQLException {
        // If DB_URL exists, we are on Render. STOP HERE.
        if (System.getenv("DB_URL") != null) return;

        try (Connection conn = DriverManager.getConnection(URL_NO_DB, ROOT_USER, ROOT_PASSWORD);
             Statement stm = conn.createStatement()) {
            stm.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DB_NAME);
            System.out.println("Database ready: " + DB_NAME);
        }
    }

    private static void create_DB_User() throws SQLException {
        // If DB_URL exists, we are on Render. STOP HERE.
        if (System.getenv("DB_URL") != null) return;

        try (Connection conn = DriverManager.getConnection(URL_NO_DB, ROOT_USER, ROOT_PASSWORD);
             Statement stm = conn.createStatement()) {

            String createUser = "CREATE USER IF NOT EXISTS '" + APP_USER + "'@'localhost' IDENTIFIED BY '" + APP_PASSWORD + "'";
            String grant = "GRANT ALL PRIVILEGES ON " + DB_NAME + ".* TO '" + APP_USER + "'@'localhost'";

            stm.executeUpdate(createUser);
            stm.executeUpdate(grant);
            stm.executeUpdate("FLUSH PRIVILEGES");
            System.out.println("Local user created.");
        }
    }

    /* =========================================================
       INIT CONNECTION POOL (MAIN SYSTEM)
       ========================================================= */
    private static HikariDataSource dataSource;

    public static void initPool() {
        // 1. Create the configuration object
        HikariConfig config = new HikariConfig();

        // 2. Set the credentials using our Dynamic Variables
        // These will automatically use Render values if present, or Localhost if not.
        config.setJdbcUrl(URL_WITH_DB);
        config.setUsername(APP_USER);
        config.setPassword(APP_PASSWORD);

        // 3. Pool Tuning (Kept exactly as you had it)
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setIdleTimeout(30000);
        config.setConnectionTimeout(10000);
        config.setMaxLifetime(1800000);

        // 4. Initialize the DataSource
        dataSource = new HikariDataSource(config);

        // 5. Success Message
        System.out.println("Connection pool started successfully.");
        System.out.println("Connected to: " + URL_WITH_DB);
    }

    /* =========================================================
       GET CONNECTION (POOL SAFE)
       ========================================================= */
    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    /* =========================================================
       EXECUTE SQL (SAFE)
       ========================================================= */
    public static void executeSQL(String sql) throws SQLException {
        try (Connection conn = getConnection();
             Statement stm = conn.createStatement()) {
            stm.execute(sql);
        }
    }

    /* =========================================================
       SHUTDOWN
       ========================================================= */
    public static void shutdown() {
        if (dataSource != null) {
            dataSource.close();
        }
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
                CurrencyCode VARCHAR(10),
                Country VARCHAR(20),
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
                  ProductPrice INT,
                  ProductCategory VARCHAR (20),
                  ProductDescription VARCHAR(2000),
                  ProductImageUrl VARCHAR(200),
                  ProductTimeStamp VARCHAR(200),
                  TryToBuyCount INT
                )
                """;

        executeSQL(query);
    }

    private static void CreatePlaceOrderTable() throws SQLException {
        String Query = """
                CREATE TABLE IF NOT EXISTS Order_Table(
                id INT AUTO_INCREMENT PRIMARY KEY,
                OwnerID VARCHAR(200),
                OrderID VARCHAR(5),
                ProductID VARCHAR(20),
                Date_Of_Order VARCHAR(200),
                Time_Of_Order VARCHAR(200),
                Quantity INT,
                Amount_Per_Product INT,
                Total_Amount INT,
                CustomerPhone VARCHAR(20),
                Order_Status VARCHAR(10)
                )
                """;
        executeSQL(Query);
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

        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(QUERY)) {
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

        try (Connection conn = getConnection();
                PreparedStatement stm = conn.prepareStatement(query)) {

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
        SET AccountComplete = ?, Phone = ?, Email = ?, CountryCode = ? , CurrencyCode = ?,Country = ?
        WHERE UserID = ?
    """;

        try (Connection conn = getConnection();
                PreparedStatement stm = conn.prepareStatement(query)) {

            stm.setString(1, "YES"); // mark account complete
            stm.setString(2, data.getString("Phone"));
            stm.setString(3, data.getString("Email"));
            stm.setString(4, data.getString("CountryCode"));
            stm.setString(5, data.getString("countrisocode"));
            stm.setString(6, data.getString("countryName"));
            stm.setString(7, data.getString("UserId")); // WHERE condition


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
        try (Connection conn = getConnection();
                PreparedStatement stm = conn.prepareStatement(query)){
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

    public static String StoreNewProduct(JSONObject data) throws SQLException {

        Connection conn = getConnection();

        String owner = data.getString("owner");

        String insertQuery = """
        INSERT INTO Product_Table (
            OwnerID,
            ProductID,
            ProductName,
            ProductPrice,
            ProductCategory,
            ProductDescription,
            ProductImageUrl,
            ProductTimeStamp,
            TryToBuyCount
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, 0)
    """;

        try {
            conn.setAutoCommit(false);

            // 🔒 lock retailer
            try (PreparedStatement lockStmt = conn.prepareStatement(
                    "SELECT UserID FROM Accounts_Table WHERE UserID = ? FOR UPDATE"
            )) {
                lockStmt.setString(1, owner);
                lockStmt.executeQuery();
            }

            // 🔍 count products
            int count;
            try (PreparedStatement countStmt = conn.prepareStatement(
                    "SELECT COUNT(*) FROM Product_Table WHERE OwnerID = ?"
            )) {
                countStmt.setString(1, owner);
                ResultSet rs = countStmt.executeQuery();

                rs.next();
                count = rs.getInt(1);
            }

            if (count >= 20) {
                conn.rollback();
                return "LIMIT_REACHED";
            }

            // ✅ insert product (ONLY ONCE)
            try (PreparedStatement stm = conn.prepareStatement(insertQuery)) {

                stm.setString(1, owner);
                stm.setString(2, data.getString("ProdID"));
                stm.setString(3, data.getString("name"));
                stm.setInt(4, data.getInt("price"));
                stm.setString(5, data.getString("Category"));
                stm.setString(6, data.getString("Description"));
                stm.setString(7, data.getString("ProdUrl"));
                stm.setString(8, String.valueOf(LocalDateTime.now()));

                stm.executeUpdate();
            }

            conn.commit();
            return "OK";

        } catch (Exception e) {
            conn.rollback();
            throw new RuntimeException(e);

        } finally {
            conn.setAutoCommit(true);
        }
    }

    // GET ALL PRODUCT OF A RETAILER
    public static String GetAllMyProduct(String OwnerID) {
        JSONArray allProducts = new JSONArray();
        String query = """
                SELECT Product_Table.ProductID,
                       Product_Table.ProductName,
                       Product_Table.ProductPrice,
                       Product_Table.ProductDescription,
                       Product_Table.ProductImageUrl,
                       Product_Table.ProductTimeStamp,
                       Accounts_Table.CurrencyCode
                       FROM Product_Table
                       INNER JOIN Accounts_Table ON UserID = Product_Table.OwnerID
                       WHERE OwnerID = ?
                """;

        try (Connection conn = getConnection();
                PreparedStatement stm = conn.prepareStatement(query)) {

            stm.setString(1, OwnerID);
            try ( ResultSet rs = stm.executeQuery()){

                while (rs.next()) { // ✅ FIX 1: loop through all rows
                    JSONObject product = new JSONObject(); // ✅ FIX 2: create new object each time

                    product.put("Id", rs.getString("ProductID"));
                    product.put("name", rs.getString("ProductName"));
                    product.put("price", rs.getInt("ProductPrice"));
                    product.put("description", rs.getString("ProductDescription"));
                    product.put("Url", rs.getString("ProductImageUrl"));
                    product.put("postedAt", Search_Engine.PostedAt(LocalDateTime.parse(rs.getString("ProductTimeStamp"))));
                    product.put("currencyCode",Fetch_Categories.CountryToCurrencyMap.getString(rs.getString("CurrencyCode")));
                    allProducts.put(product);
                }
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

        try (Connection conn = getConnection();
                PreparedStatement stm = conn.prepareStatement(selectQuery)) {
            stm.setString(1, userID);
            ResultSet rs = stm.executeQuery();

            if (rs.next()) {
                String oldImage = rs.getString("ProfileUrl");
                // 🔹 Try deleting old image (if exists)
                if (oldImage != null && !oldImage.isEmpty()) {
                    File file = new File(DatabaseManager.BicyconProfile + File.separator + oldImage);

                    if (file.exists()) {
                        boolean deleted = file.delete();
                        if (!deleted) {
                            System.out.println("Warning: Failed to delete old image");
                        }
                    }
                }

                // 🔹 Always update DB (don’t depend on file delete)
                try (Connection conn1 = getConnection();
                        PreparedStatement stm2 = conn1.prepareStatement(updateQuery)) {
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
            SELECT UserName, ProfileUrl, Phone, Email, AccountComplete,CurrencyCode,Country
            FROM Accounts_Table
            WHERE UserID = ?
            """;

        try (Connection conn = getConnection();
                PreparedStatement stm = conn.prepareStatement(query)) {

            stm.setString(1, userID);
            ResultSet rs = stm.executeQuery();

            if (rs.next()) {

                result.put("User-Name", rs.getString("UserName"));
                result.put("account_completed", rs.getString("AccountComplete"));
                result.put("CountryisoCode",rs.getString("CurrencyCode"));
                result.put("CountryName",rs.getString("Country"));

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

        // ✅ Validate category
        JSONArray categoriesArray = Fetch_Categories.Categories.optJSONArray("Product_Categories");
        boolean isAll = key.equalsIgnoreCase("All");
        boolean found = false;

        if (isAll) {
            found = true;
        } else if (categoriesArray != null) {
            for (int i = 0; i < categoriesArray.length(); i++) {
                if (categoriesArray.getString(i).equalsIgnoreCase(key)) {
                    found = true;
                    break;
                }
            }
        }

        if (!found) {
            System.err.println("Invalid category: " + key);
            return products.toString();
        }

        // ✅ Optimized query (JOIN + GROUP + SCORE)
        String query = isAll
                ? """
        SELECT 
            p.ProductID,
            p.ProductName,
            p.ProductPrice,
            p.ProductDescription,
            p.ProductImageUrl,
            p.ProductTimeStamp,
            p.TryToBuyCount,
            p.OwnerID,

            a.UserName,
            a.ProfileUrl,
            a.CurrencyCode,

            COALESCE(o.BuyCount, 0) AS BuyCount

        FROM Product_Table p

        LEFT JOIN (
            SELECT ProductID, COUNT(*) AS BuyCount
            FROM Order_Table
            GROUP BY ProductID
        ) o ON p.ProductID = o.ProductID

        LEFT JOIN Accounts_Table a
            ON p.OwnerID = a.UserID

        ORDER BY ((COALESCE(o.BuyCount, 0) * 3) + p.TryToBuyCount) DESC
        """
                : """
        SELECT 
            p.ProductID,
            p.ProductName,
            p.ProductPrice,
            p.ProductDescription,
            p.ProductImageUrl,
            p.ProductTimeStamp,
            p.TryToBuyCount,
            p.OwnerID,

            a.UserName,
            a.ProfileUrl,
            a.CurrencyCode,

            COALESCE(o.BuyCount, 0) AS BuyCount

        FROM Product_Table p

        LEFT JOIN (
            SELECT ProductID, COUNT(*) AS BuyCount
            FROM Order_Table
            GROUP BY ProductID
        ) o ON p.ProductID = o.ProductID

        LEFT JOIN Accounts_Table a
            ON p.OwnerID = a.UserID

        WHERE p.ProductCategory = ?

        ORDER BY ((COALESCE(o.BuyCount, 0) * 3) + p.TryToBuyCount) DESC
        """;

        try (Connection conn = getConnection();
                PreparedStatement stm = conn.prepareStatement(query)) {

            if (!isAll) {
                stm.setString(1, key);
            }

            ResultSet rs = stm.executeQuery();

            while (rs.next()) {

                JSONObject product = new JSONObject();

                // 🔹 Basic product info
                product.put("ImageUrl", rs.getString("ProductImageUrl"));
                product.put("Name", rs.getString("ProductName"));
                product.put("Price", rs.getInt("ProductPrice"));
                product.put("Description", rs.getString("ProductDescription"));
                product.put("prodID", rs.getString("ProductID"));

                // 🔹 Time formatting
                product.put("postedAt",
                        Search_Engine.PostedAt(
                                LocalDateTime.parse(rs.getString("ProductTimeStamp"))
                        )
                );

                // 🔹 Retailer info (from JOIN)
                product.put("RetailerName", rs.getString("UserName"));
                product.put("RetailerID", rs.getString("OwnerID"));
                product.put("profilePic", rs.getString("ProfileUrl"));
                product.put("currencyCode",Fetch_Categories.CountryToCurrencyMap.getString(rs.getString("CurrencyCode")));

                // 🔹 Scoring system
                int buyCount = rs.getInt("BuyCount");
                int tryCount = rs.getInt("TryToBuyCount");
                int score = (buyCount * 3) + tryCount;

                product.put("BuyCount", buyCount);
                product.put("TryToBuyCount", tryCount);
                product.put("Score", score);

                products.put(product);
            }
        }

        return products.toString();
    }


    public static String UpdateEmail (String UserID , String NewEmail) throws SQLException {
        String Query = """
                UPDATE Accounts_Table SET Email = ?  WHERE UserID = ?
                """;

        try (Connection conn = getConnection();
                PreparedStatement stm = conn.prepareStatement(Query)){

            stm.setString(1,NewEmail);
            stm.setString(2,UserID);

            int row = stm.executeUpdate();
            if (row > 0){
                return "OK";
            }

        }
        return  "!OK";
    }

    public static String UpdatePhone (String UserID , String NewPhone) throws SQLException{
        String Query = """
                UPDATE Accounts_Table SET Phone = ?  WHERE UserID = ?
                """;

        try (Connection conn = getConnection();
                PreparedStatement stm = conn.prepareStatement(Query)){

            stm.setString(1,NewPhone);
            stm.setString(2,UserID);

            int row = stm.executeUpdate();
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

        try (Connection conn = getConnection();
                PreparedStatement stm = conn.prepareStatement(Query)){
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

        try (Connection conn = getConnection();
                PreparedStatement stm1 = conn.prepareStatement(query)){
            stm1.setString(1,Product_ID);
            ResultSet rs = stm1.executeQuery();
            if (rs.next()){
                String oldImage = rs.getString("ProductImageUrl");
                if (oldImage != null && !oldImage.isEmpty()){
                    File file = new File(DatabaseManager.ProductImages + File.separator + oldImage);

                    if (file.exists()){
                        boolean deleted = file.delete();
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
        try (Connection conn = getConnection();
                PreparedStatement stm = conn.prepareStatement(Query)){
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
        try (Connection conn = getConnection();
                PreparedStatement stm = conn.prepareStatement(Query)){
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

        try (Connection conn = getConnection();
                PreparedStatement stm = conn.prepareStatement(QUERY)) {
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

         try (Connection conn = getConnection();
                 PreparedStatement stm = conn.prepareStatement(select_image_Query)){
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
                 try (Connection conn1 = getConnection();
                         PreparedStatement stm1 = conn1.prepareStatement(update_Query)){
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
        try (Connection conn = getConnection();
                PreparedStatement stm = conn.prepareStatement(Query)){
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

    public static String PlaceOrder(JSONObject Data) throws SQLException{
        String findOwnerQuery = """
                SELECT OwnerID FROM Product_Table WHERE ProductID = ?
                """;
        try (Connection conn = getConnection();
                PreparedStatement stm = conn.prepareStatement(findOwnerQuery)){
            stm.setString(1, Data.getString("ProductId"));

            ResultSet RS = stm.executeQuery();
            if (RS.next()){
                String Owner = RS.getString("OwnerID");

                if (Owner != null){
                    String insertNewProduct = """
                            INSERT INTO Order_Table (
                            OwnerID,
                            OrderID,
                            ProductID,
                            Date_Of_Order,
                            Time_Of_Order,
                            Quantity,
                            Amount_Per_Product,
                            Total_Amount,
                            CustomerPhone
                            )
                            VALUES(?,?,?,?,?,?,?,?,?)
                            """;

                    try (PreparedStatement stm1 = getConnection().prepareStatement(insertNewProduct)){
                        stm1.setString(1,Owner);
                        stm1.setString(2,SHA256.hash(Data.getString("ProductId") + Data.getInt("Quantity") + LocalDateTime.now()).substring(0,5));
                        stm1.setString(3,Data.getString("ProductId"));
                        stm1.setString(4,String.valueOf(LocalDate.now()));
                        stm1.setString(5,String.valueOf(LocalTime.now()));
                        stm1.setInt(6,Data.getInt("Quantity"));
                        stm1.setInt(7,Data.getInt("ProductPrice"));
                        stm1.setInt(8,(Data.getInt("Quantity") * Data.getInt("ProductPrice")));
                        stm1.setString(9,Data.getString("CustomerPhone"));

                        stm1.execute();

                        return "OK";
                    }
                }
            }
        }

        return null;
    }

    public static String GET_MY_ORDERS(String User_ID) throws SQLException{
        JSONArray Orders = new JSONArray();
        int index = 1;
        String Query = """
            SELECT o.*, 
                   a.CurrencyCode, 
                   p.ProductName
            FROM Order_Table o
            INNER JOIN Accounts_Table a ON o.OwnerID = a.UserID
            INNER JOIN Product_Table p ON o.ProductID = p.ProductID
            WHERE o.OwnerID = ?
            """;
        try (Connection conn = getConnection();
                PreparedStatement stm = conn.prepareStatement(Query)){
            stm.setString(1,User_ID);
            ResultSet RS = stm.executeQuery();

            while (RS.next()){
                JSONObject Order = new JSONObject();
                Order.put("index",index++);
                Order.put("owner",RS.getString("OwnerID"));
                String ProductID = RS.getString("ProductID");
                Order.put("productId",ProductID);
                Order.put("orderId",RS.getString("OrderID"));
                Order.put("date",RS.getString("Date_Of_Order"));
                Order.put("time",RS.getString("Time_Of_Order"));
                Order.put("quantity",RS.getInt("Quantity"));
                Order.put("amountPerProduct",RS.getInt("Amount_Per_Product"));
                Order.put("totalAmount",RS.getInt("Total_Amount"));
                Order.put("customerPhone",RS.getString("CustomerPhone"));
                Order.put("status", RS.getString("Order_Status"));
                Order.put("currencyCode",Fetch_Categories.CountryToCurrencyMap.getString(RS.getString("currencyCode")));
                Order.put("productName",RS.getString("ProductName"));
                Orders.put(Order);
            }
        }
        return Orders.toString();
    }

    public static String SET_ORDER_STATUS(JSONObject Data) throws SQLException{

        String Query = """
                UPDATE Order_Table SET Order_Status = ? WHERE OrderID = ?
                """;

        try (Connection conn = getConnection();
                PreparedStatement stm = conn.prepareStatement(Query)){
            stm.setString(1,Data.getString("status"));
            stm.setString(2,Data.getString("OrderID"));

            stm.executeUpdate();

            return "OK";
        }
    }

    //Database code to fetch Retailer mete data when account is searched on google

    /* =========================================================
       CREATE DIRECTORIES
       ========================================================= */
    private static void createFileDirectory() {

        BicyconProfile.mkdirs();
        ProductImages.mkdirs();
        Data.mkdirs();
    }

}