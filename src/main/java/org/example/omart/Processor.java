package org.example.omart;


import Database.DatabaseManager;
import jakarta.servlet.MultipartConfigElement;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.boot.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.util.unit.DataSize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api")
public class Processor {

    @PostMapping("/process")
    public String process(@RequestBody String POST_DATA) throws ExecutionException, InterruptedException, SQLException { //DATA RECEIVED AS STRING

        JSONObject DATA = new JSONObject(POST_DATA); // PASSING STRINGED JSON TO JSON
        String INSTRUCTION = DATA.getString("INSTRUCTION"); //RETRIEVING INSTRUCTION


            switch (INSTRUCTION) {
                case "CHECK-EXISTING-EMAIL"->{
                    String email = DATA.getString("retailer-email");
                    return DatabaseManager.CHECK_EXISTING_EMAIL(email);
                }

                case "REGISTER-NEW-RETAILER" ->{
                    DATA.remove("INSTRUCTION");
                    String ID = SHA256.hash(DATA.getString("email")).substring(0,5) + "@bicycon";
                    String status = DatabaseManager.InsertNewAccount(ID,DATA);
                    if(status .equals("OK")){
                        DATA.put("id",ID);
                        DATA.put("currency",Fetch_Config_Data.CountryToCurrencyMap.getString(DATA.getString("iso2")));
                    }
                    return DATA.toString();
                }

                case "GET-CATEGORIES" ->{
                    return Fetch_Config_Data.Categories.toString();
                }

                case "GET-PRODUCT" ->{
                    String Key = DATA.getString("KeySearch");
                    return DatabaseManager.GET_Products_PER_KEY(Key);
                }

                case "GET-MY-PRODUCTS" -> {
                    String OutPut = DatabaseManager.GetAllMyProduct(DATA.getString("User_id"));
                    if(OutPut != null){
                        return OutPut;
                    }else {
                        JSONObject Result = new JSONObject();
                        Result.put("Status", "null");
                        return Result.toString();
                    }
                }

                case "UPDATE-MY-PHONE"->{
                    JSONObject Result = new JSONObject();
                    String newPhone = DATA.getString("new_Phone");
                    String UserID = DATA.getString("UserID");

                    if(DatabaseManager.UpdatePhone(UserID,newPhone).equals("OK")){
                        Result.put("status","OK");
                        Result.put("New_Phone",newPhone);
                    }
                    return Result.toString();
                }

                case "GET-RETAILER-EMAIL & PHONE" ->{
                    String Retailer_ID = DATA.getString("RetailerID");
                    String ProductID = DATA.getString("productId");
                    String Result = DatabaseManager.GET_Retailer_Email_Phone(Retailer_ID);
                    TryToBuy.addToProducts(ProductID);
                    if(Result != null){
                        return Result;
                    }
                    return null;
                }

                case "DELETE-MY-PRODUCT"->{
                    JSONObject Result = new JSONObject();
                    String Product_ID = DATA.getString( "ProdID");
                    if (DatabaseManager.Delete_My_Product(Product_ID).equals("OK")){
                        Result.put("status","OK");
                    }
                    return Result.toString();
                }

                case "GET-PRODUCT-CATEGORY"->{
                    String ProductID = DATA.getString("ProductID");
                    String Result = DatabaseManager.Get_Category(ProductID);
                    if(Result != null){
                        return Result;
                    }
                    return null;
                }

                case "GET-PROD-BASE-ON-ID" -> {
                    JSONObject Result = new JSONObject();
                    String Retailer_ID = DATA.getString("Retailer_ID");
                    JSONArray obj1 = new JSONArray(DatabaseManager.GetAllMyProduct(Retailer_ID)); // ✅ FIX
                    JSONObject obj2 = new JSONObject(DatabaseManager.GET_Retailer_Email_Profile_Phone(Retailer_ID));
                    Result.put("Account_Products", obj1);
                    Result.put("Account_Info", obj2);
                    return Result.toString();
                }

                case "UPDATE-PROD-DATA"->{
                    DATA.remove("INSTRUCTION");
                    return DatabaseManager.Update_Prod_Data(DATA);
                }

                case "PLACE-ORDER"->{
                    JSONObject Result = new JSONObject();
                    String ProdID = SHA256.hash(DATA.getString("ProductId") + DATA.getInt("Quantity") + LocalDateTime.now()).substring(0,5);
                    if (Objects.equals(DatabaseManager.PlaceOrder(DATA), "OK")){
                       Result.put("status","OK");
                       Result.put("orderID",ProdID);
                    }
                    return Result.toString();
                }

                case "GET-MY-ORDERS"->{
                    String UserID = DATA.getString("User_id");
                    return DatabaseManager.GET_MY_ORDERS(UserID);
                }

                case "SET-ORDER-STATUS"->{
                    String Result = DatabaseManager.SET_ORDER_STATUS(DATA);
                    JSONObject ResultJ = new JSONObject();
                    if (Result.equals("OK")){
                        ResultJ.put("status","OK");
                    }
                    return ResultJ.toString();
                }

                case "GET-COUNTRY-CURRENCY-CODE"->{
                    JSONObject Result = new JSONObject();
                    String currencyCode = Fetch_Config_Data.CountryToCurrencyMap.getString(DATA.getString("countryISO"));
                    Result.put("currencyCode",currencyCode);
                    return Result.toString();
                }

                case "SEARCH"->{
                    String input = DATA.getString("input");
                    return Search_Engine.Search(input);
                }

                case "INFLATE-TRY-TO-BUY"->{
                    JSONObject JResult = new JSONObject();
                    String productID = DATA.getString("productID");
                    String Result = TryToBuy.addToProducts(productID);
                    if (Result == "OK"){
                        JResult.put("status","OK");
                    }
                    return JResult.toString();
                }

                case "GET-REVENUE-DATA" ->{
                    return GetRevenueData.GetData(DATA);
                }

                case "INSERT-NEW-STORE" ->{
                    JSONObject response = new JSONObject();
                    String StoreID = SHA256.hash(DATA.getString("name") + DATA.getString("owner") + LocalDateTime.now()).substring(0,5) + "-" + DATA.getString("owner");
                    String Response = DatabaseManager.INSERT_New_Store(DATA,StoreID);
                    if (Response.equals("OK")){
                        response.put("status","OK");
                    }
                    return response.toString();
                }

                case "GET-MY-STORES" ->{
                    return DatabaseManager.GET_MY_STORES(DATA);
                }

                case "PING" -> {
                    JSONObject PING = new JSONObject();
                    PING.put("status", "OK");
                    return PING.toString();
                }

                default -> {
                    JSONObject PING = new JSONObject();
                    PING.put("status", "!OK");
                    return PING.toString();
                }
            }

    }

    //Completing account
    @PostMapping("/profile")
    private String CompleteCreatedAccount(@RequestParam("file")MultipartFile File , @RequestParam("UserId") String UserId){
        //get username id
        JSONObject uploadResult = new JSONObject();
        try{

            String userId = UserId;
            String FileName = userId+ "-Profile" + File.getOriginalFilename();
            Path path = Paths.get(DatabaseManager.BicyconProfile.getAbsolutePath() , FileName);
            Files.write(path,File.getBytes());

            //storing profile location in database
            if (DatabaseManager.StoreProfileLocation(userId,FileName).equals("OK")){
                uploadResult.put("status","OK");
            }

        } catch (IOException e){
            e.printStackTrace();
        }
        return uploadResult.toString();
    }

    @PostMapping("/file")
    private String NewProduct(@RequestParam("file")MultipartFile File , @RequestParam("Data")String data) throws ExecutionException, InterruptedException, SQLException, IOException {

            JSONObject Data = new JSONObject(data);
            String INSTRUCTION = Data.getString("INSTRUCTION");
           JSONObject uploadResult = new JSONObject();

            switch (INSTRUCTION){
                case "UPLOAD-PROFILE"->{
                    String User_ID = Data.getString("User_Id");
                    String FileName = File.getOriginalFilename();
                    String safeFileName = UUID.randomUUID() + "-" + FileName.replaceAll("\\s+", "_");

                    Path path = Paths.get(DatabaseManager.BicyconProfile.getAbsolutePath(),safeFileName);

                    Files.write(path,File.getBytes());

                    if (DatabaseManager.StoreProfileLocation(User_ID,safeFileName).equals("OK")){
                        uploadResult.put("status","OK");
                        uploadResult.put("Url",safeFileName);
                    }

                    System.out.println(uploadResult);
                    return  uploadResult.toString();
                }

                case "UPLOAD-NEW-PROD"->{
                    String ProdID = SHA256.hash(Data.getString("owner") + Data.getString("Category") + LocalDateTime.now()).substring(0,5);
                    String FileName = File.getOriginalFilename();
                    String safeFileName = UUID.randomUUID() + "-" + FileName.replaceAll("\\s+", "_");
                    Path path = Paths.get(DatabaseManager.ProductImages.getAbsolutePath(),safeFileName);

                    Files.write(path,File.getBytes());

                    Data.remove("INSTRUCTION");
                    Data.put("ProdID",ProdID);
                    Data.put("ProdUrl",safeFileName);

                    String Result = DatabaseManager.StoreNewProduct(Data);

                    if (Result .equals("OK")){
                        uploadResult.put("status","OK");
                    }else if (Result.equals("LIMIT_REACHED")){
                        uploadResult.put("status","LIMIT_REACHED");
                    }

                    return uploadResult.toString();
                }

                case "UPDATE-PROFILE-PIC" ->{
                    String User_ID = Data.getString("UserID");
                    String FileName = File.getOriginalFilename();
                    String safeFileName = UUID.randomUUID() + "-" + FileName.replaceAll("\\s+", "_");
                    Path path = Paths.get(DatabaseManager.BicyconProfile.getAbsolutePath(),safeFileName);

                    Files.write(path,File.getBytes());

                    //deleting old profile form database and inserting
                    if (DatabaseManager.updateMyProfile(User_ID,safeFileName).equals("OK")){
                        uploadResult.put("status","OK");
                        uploadResult.put("url",safeFileName);
                    }

                    return uploadResult.toString();
                }

                case "UPDATE-PROD-DATA"->{
                    JSONObject Result = new JSONObject();
                    String FileName = File.getOriginalFilename();
                    String safeFileName = UUID.randomUUID() + "-" + FileName.replaceAll("\\s+", "_");
                    Path path = Paths.get(DatabaseManager.ProductImages.getAbsolutePath(),safeFileName);

                    Files.write(path,File.getBytes());

                    Data.remove("INSTRUCTION");

                    String storedResult = DatabaseManager.Update_Prod_Data_With_Image(safeFileName,Data);
                    if (storedResult.equals("OK")){
                        Result.put("status", "OK");
                    }
                    return Result.toString();
                }

                default -> {
                    JSONObject PING = new JSONObject();
                    PING.put("status", "!OK");
                    return PING.toString();
                }
            }

    }


    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        factory.setMaxFileSize(DataSize.ofMegabytes(100));
        factory.setMaxRequestSize(DataSize.ofMegabytes(100));
        return factory.createMultipartConfig();
    }

}
