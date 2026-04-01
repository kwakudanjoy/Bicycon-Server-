package org.example.bycicon;


import Database.DatabaseManager;
import jakarta.servlet.MultipartConfigElement;

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
import java.util.UUID;
import java.util.concurrent.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api")
public class Processor {

    private final ExecutorService threadPool = new ThreadPoolExecutor(4,
            12,
            60L,
            TimeUnit.SECONDS
            ,new ArrayBlockingQueue<>(100),
            new ThreadPoolExecutor.CallerRunsPolicy());

    @PostMapping("/process")
    public String process(@RequestBody String POST_DATA) throws SQLException, ExecutionException, InterruptedException { //DATA RECEIVED AS STRING

        JSONObject DATA = new JSONObject(POST_DATA); // PASSING STRINGED JSON TO JSON
        String INSTRUCTION = DATA.getString("INSTRUCTION"); //RETRIEVING INSTRUCTION

        Future<String> result = threadPool.submit(() -> {
          //  System.out.println(DATA);

            switch (INSTRUCTION) {
                case "SIGN-UP" -> {

                    JSONObject Result = new JSONObject();
                    String Name = DATA.getString("Name");
                    String Password = DATA.getString("Password");
                    String TimeStamp = LocalDateTime.now().toString();

                    //computing id
                    String ID = SHA256.hash(Name + Password + TimeStamp).substring(0,5) + "@bicycon";
                    String NewPassword = SHA256.hash(Password);

                    JSONObject newAccount = new JSONObject();
                    newAccount.put("User-ID",ID);
                    newAccount.put("User-Name",Name);
                    newAccount.put("User-Password",NewPassword);
                    newAccount.put("Time-Stamp",TimeStamp);

                    String outPut = DatabaseManager.InsertNewAccount(newAccount);
                    if (outPut.equals("OK")){
                        Result.put("User-Name",Name);
                        Result.put("User-ID", ID);
                        Result.put("account_completed","NO");
                    }

                    return Result.toString();
                }

                case "SIGN-IN" -> {
                    System.out.println(DATA);
                    String userID = DATA.getString("SignInId");
                    String password = DATA.getString("SignInPassword");

                    String loginResult = DatabaseManager.Confirm_Logins(userID, password);

                    JSONObject result1;

                    switch (loginResult) {
                        case "OK" -> {
                            result1 = new JSONObject(DatabaseManager.FetchMyData(userID));
                            result1.put("status", "OK");
                            result1.put("User-ID",userID);
                        }
                        case "!OK" -> {
                            result1 = new JSONObject();
                            result1.put("status", "!OK");
                        }
                        case "!USER" -> {
                            result1 = new JSONObject();
                            result1.put("status", "!USER");
                        }
                        default -> {
                            result1 = new JSONObject();
                            result1.put("status", "ERROR");
                        }
                    }
                    System.out.println(result1);
                    return result1.toString();
                }


                case "COMPLETE-ACCOUNT"->{

                    DATA.remove("INSTRUCTION");
                    if (DatabaseManager.CompleteAccount(DATA).equals("OK")){
                        JSONObject RESULT = new JSONObject();
                        RESULT.put("status", "OK");
                        RESULT.put("Email" , DATA.getString("Email"));
                        RESULT.put("Phone",DATA.getString("Phone"));
                        return RESULT.toString();


                    }else {
                        JSONObject PING = new JSONObject();
                        PING.put("status", "!OK");
                        return PING.toString();

                    }
                }

                case "GET-CATEGORIES" ->{
                    return Fetch_Categories.Categories.toString();
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
                        return null;
                    }
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
        });

        return result.get();
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
    private String NewProduct(@RequestParam("file")MultipartFile File , @RequestParam("Data")String data) throws ExecutionException, InterruptedException {
       Future<String> result = threadPool.submit(()->{
            JSONObject Data = new JSONObject(data);
            System.out.println(Data);
            String INSTRUCTION = Data.getString("INSTRUCTION");
           JSONObject uploadResult = new JSONObject();

            switch (INSTRUCTION){
                case "UPLOAD-PROFILE"->{
                    System.out.println(Data);
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
                    System.out.println(Data);
                    String ProdID = SHA256.hash(Data.getString("owner") + Data.getString("Category") + LocalDateTime.now()).substring(0,5);
                    String FileName = File.getOriginalFilename();
                    String safeFileName = UUID.randomUUID() + "-" + FileName.replaceAll("\\s+", "_");
                    Path path = Paths.get(DatabaseManager.ProductImages.getAbsolutePath(),safeFileName);

                    Files.write(path,File.getBytes());

                    Data.remove("INSTRUCTION");
                    Data.put("ProdID",ProdID);
                    Data.put("ProdUrl",safeFileName);

                    if (DatabaseManager.StoreNewProduct(Data) .equals("OK")){
                        uploadResult.put("status","OK");
                    }

                    return uploadResult.toString();
                }

                case "UPDATE-PROFILE" ->{
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

                default -> {
                    JSONObject PING = new JSONObject();
                    PING.put("status", "!OK");
                    return PING.toString();
                }
            }

        });

        return result.get();
    }


    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        factory.setMaxFileSize(DataSize.ofMegabytes(100));
        factory.setMaxRequestSize(DataSize.ofMegabytes(100));
        return factory.createMultipartConfig();
    }

}
