package com.yelp;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public class DataLoad {



    //public static List<Attribute> attributes = new ArrayList();
    public static void main(String[] args) throws IOException, SQLException, ClassNotFoundException, ParseException {

        DriverManager.registerDriver(new oracle.jdbc.OracleDriver());
        Connection con = null;
        String location = "jdbc:oracle:thin:@localhost:1521:XE";
        try {
            con = DriverManager.getConnection(location, "system", "newpassword");
        } catch (SQLException e) {
            e.printStackTrace();


        }
        Business(con);
        User(con);
        Review(con);
        con.close();
    }
    public static void Review(Connection con) throws IOException,SQLException,ClassNotFoundException{

        JSONParser jsonParser = new JSONParser();
        FileReader file = new FileReader("/Users/ishwaryarajasekaran/YelpDataset-CptS451/yelp_review.json");
        BufferedReader bufferedReader = new BufferedReader(file);
        Statement statement = null;
        statement = con.createStatement();

        PreparedStatement usersSQL = null;
        String line = null;
        System.out.println("Inserting Reviews Records to the DB..");
        PreparedStatement reviewSQL = null;
        try {

            while ((line = bufferedReader.readLine()) != null) {

                JSONObject obj = (JSONObject) new JSONParser().parse(line);
                String user_id = (String) obj.get("user_id");
                String review_id = (String) obj.get("review_id");
                long stars = (long) obj.get("stars");
                String datee = (String) obj.get("date");
                String text = (String) obj.get("text");
                String type = (String) obj.get("type");
                String business_id = (String) obj.get("business_id");

                if (reviewSQL == null) {
                    String sql = "INSERT INTO REVIEWS VALUES(?,?,?,?,?,?,?)";
                    reviewSQL = con.prepareStatement(sql);
                }
                reviewSQL.setString(1, user_id);
                reviewSQL.setString(2,review_id);
                reviewSQL.setLong(3,stars);
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM");
                java.util.Date parsed = format.parse(datee.toString());
                reviewSQL.setDate(4,new Date(parsed.getTime()));
               reviewSQL.setString(5,text);
               reviewSQL.setString(6,type);
               reviewSQL.setString(7,business_id);
                reviewSQL.executeUpdate();

                String votes = (String) obj.get("votes").toString();
                PreparedStatement Votes_SQL = con.prepareStatement("INSERT INTO VOTES_REVIEW(REVIEW_ID,USER_ID,BUSINESS_ID,FUNNY,USEFUL,COOL) VALUES (?,?,?,?,?,?)");
                try{

                    JSONObject votes_obj = (JSONObject) new JSONParser().parse(votes);

                    long cool =(long) votes_obj.get("cool");
                    long useful = (long) votes_obj.get("useful");
                    long funny = (long) votes_obj.get("funny");

                    Votes_SQL.setString(1,review_id);
                    Votes_SQL.setString(2,user_id);
                    Votes_SQL.setString(3,business_id);
                    Votes_SQL.setLong(4,funny);
                    Votes_SQL.setLong(5,useful);
                    Votes_SQL.setLong(6,cool);
                   // Votes_SQL.setLong(5,(funny+cool+useful));
                    Votes_SQL.executeUpdate();
                }
                finally {
                    Votes_SQL.close();
                }
            }

            reviewSQL.close();
            bufferedReader.close();
        }
        catch (Exception e){
                e.printStackTrace();

        }
    }

    public static void User(Connection con) throws  IOException,SQLException,ClassNotFoundException{

        JSONParser jsonParser = new JSONParser();
        FileReader file = new FileReader("/Users/ishwaryarajasekaran/YelpDataset-CptS451/yelp_user.json");
        BufferedReader bufferedReader = new BufferedReader(file);
        Statement statement = null;
        statement = con.createStatement();

       // statement.executeUpdate("DELETE VOTES_USER");
       // statement.executeUpdate("DELETE YELP_USERS");
        PreparedStatement usersSQL = null;
        String line = null;
        System.out.println("Inserting User Records to the DB..");
        PreparedStatement userSQL = null;
        try {
            long yelp_users = 0;
            while((line = bufferedReader.readLine()) != null){

                JSONObject obj = (JSONObject) new JSONParser().parse(line);
                String date = (String) obj.get("yelping_since");
                long review_count = (long)obj.get("review_count");
                String user_name = (String) obj.get("name");
                String user_id = (String) obj.get("user_id");
                long fans = (long) obj.get("fans");
                double average_stars = (double) obj.get("average_stars");
                String user_type = (String) obj.get("type");
                JSONArray elite = (JSONArray) obj.get("elite");
                JSONArray friends = (JSONArray) obj.get("friends");

                if (userSQL == null) {
                    String sql = "INSERT INTO YELP_USERS VALUES(?,?,?,?,?,?,?,?,?)";
                    userSQL = con.prepareStatement(sql);
                }
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM");
                java.util.Date parsed = format.parse(date.toString());
                userSQL.setDate(1,new Date(parsed.getTime()));
                userSQL.setLong(2,review_count);
                userSQL.setString(3,user_name);
                userSQL.setString(4,user_id);
                userSQL.setLong(5,fans);
                userSQL.setDouble(6,average_stars);
                userSQL.setString(7,user_type);
                userSQL.setString(8, String.valueOf(elite));
                userSQL.setInt(9, friends.size());

                userSQL.executeUpdate();


                String votes = (String) obj.get("votes").toString();
                PreparedStatement Votes_SQL = con.prepareStatement("INSERT INTO VOTES_USER(USER_ID,FUNNY,USEFUL,COOL,COUNT_OF_VOTES) VALUES (?,?,?,?,?)");
                try{

                    JSONObject votes_obj = (JSONObject) new JSONParser().parse(votes);

                    long cool =(long) votes_obj.get("cool");
                    long useful = (long) votes_obj.get("useful");
                    long funny = (long) votes_obj.get("funny");

                    Votes_SQL.setString(1,user_id);
                    Votes_SQL.setLong(2,funny);
                    Votes_SQL.setLong(3,useful);
                    Votes_SQL.setLong(4,cool);
                    Votes_SQL.setLong(5,(funny+cool+useful));
                    Votes_SQL.executeUpdate();
                }
                finally {
                    Votes_SQL.close();
                }
            }

            userSQL.close();
            bufferedReader.close();
        }
        catch (Exception e){
            e.printStackTrace();

        }
    }
    public static void Business(Connection con) throws IOException, SQLException, ClassNotFoundException, ParseException {
        class Attribute {
            String business_id;
            String attribute;
            public Attribute(String business_id, String attribute) {
                this.business_id = business_id;
                this.attribute = attribute;
            }
        }
        JSONParser jsonParser = new JSONParser();
        FileReader file = new FileReader("/Users/ishwaryarajasekaran/YelpDataset-CptS451/yelp_business.json");
        BufferedReader bufferedReader = new BufferedReader(file);
        Statement statement = null;
        statement = con.createStatement();



        PreparedStatement businessSQL = null;
        PreparedStatement attributeSQL = null;
        String line = null;
        ArrayList mainCategories = new ArrayList();
        ArrayList subCategories = new ArrayList();
        List<Attribute> attributes = new ArrayList();

        HashSet<String> mainCategoriesHash = new HashSet();
        mainCategoriesHash.add("Active Life");
        mainCategoriesHash.add("Arts & Entertainment");
        mainCategoriesHash.add("Automotive");
        mainCategoriesHash.add("Car Rental");
        mainCategoriesHash.add("Cafes");
        mainCategoriesHash.add("Beauty & Spas");
        mainCategoriesHash.add("Convenience Stores");
        mainCategoriesHash.add("Dentists");
        mainCategoriesHash.add("Doctors");
        mainCategoriesHash.add("Drugstores");
        mainCategoriesHash.add("Department Stores");
        mainCategoriesHash.add("Education");
        mainCategoriesHash.add("Event Planning & Services");
        mainCategoriesHash.add("Flowers & Gifts");
        mainCategoriesHash.add("Food");
        mainCategoriesHash.add("Health & Medical");
        mainCategoriesHash.add("Home Services");
        mainCategoriesHash.add("Home & Garden");
        mainCategoriesHash.add("Hospitals");
        mainCategoriesHash.add("Hotels & Travel");
        mainCategoriesHash.add("Hardware Stores");
        mainCategoriesHash.add("Grocery");
        mainCategoriesHash.add("Medical Centers");
        mainCategoriesHash.add("Nurseries & Gardening");
        mainCategoriesHash.add("Nightlife");
        mainCategoriesHash.add("Restaurants");
        mainCategoriesHash.add("Shopping");
        mainCategoriesHash.add("Transportation");
        System.out.println("Inserting Business Records to the DB..");
        try {
            long business_count =0;
            while ((line = bufferedReader.readLine()) != null) {
                JSONObject obj = (JSONObject) new JSONParser().parse(line);
                JSONArray json_extra = new JSONArray();

                String business_id = (String) obj.get("business_id");
                String full_address = (String) obj.get("full_address");
                boolean open = (boolean) obj.get("open");

                JSONArray arr = (JSONArray) obj.get("categories");
                PreparedStatement Bus_Cat = con.prepareStatement("INSERT INTO Business_To_Category(Business_Id,Category) VALUES (?,?)");
                PreparedStatement Bus_Sub_Cat = con.prepareStatement("INSERT INTO Business_To_Sub_Category(Business_Id,Sub_Category) VALUES (?,?)");
                try{
                    for (int i = 0; i < arr.size(); i++) {
                        String category = (String) arr.get(i);
                        if (mainCategoriesHash.contains(category)) {


                            Bus_Cat.setString(1, business_id);
                            Bus_Cat.setString(2, category);
                            Bus_Cat.executeUpdate();

                        }
                        else {
                           Bus_Sub_Cat.setString(1,business_id);
                           Bus_Sub_Cat.setString(2,category);
                           Bus_Sub_Cat.executeUpdate();
                    }
                    }
                }
                finally {
                    Bus_Cat.close();
                    Bus_Sub_Cat.close();
                }


                String city = (String) obj.get("city");
                long review_count = (long) obj.get("review_count");
                String business_name = (String) obj.get("name");
                JSONArray neighborhoods = (JSONArray) obj.get("neighborhoods");
                double longitude = (double) obj.get("longitude");
                String state = (String) obj.get("state");
                double stars = (double) obj.get("stars");
                double latitude = (double) obj.get("latitude");
                String type = (String) obj.get("type");

                JSONObject attributes1 = (JSONObject) obj.get("attributes");
                Iterator<?> keys1 = attributes1.keySet().iterator();
                while (keys1.hasNext()) {
                    String key1 = (String) keys1.next();
                    StringBuilder sb1 = new StringBuilder(key1);
                    if (attributes1.get(key1) instanceof JSONObject) {
                        JSONObject attributes2 = (JSONObject) attributes1.get(key1);
                        Iterator<?> keys2 = attributes2.keySet().iterator();
                        while (keys2.hasNext()) {
                            String key2 = (String) keys2.next();
                            StringBuilder sb2 = new StringBuilder(key2);
                            sb2.append("_");
                            sb2.append(attributes2.get(key2));
                            attributes.add(new Attribute(business_id, sb1.toString() + "_" + sb2.toString()));
                        }
                    }
                    else {
                        sb1.append("_");
                        sb1.append(attributes1.get(key1));
                        attributes.add(new Attribute(business_id, sb1.toString()));
                    }
                }
                if (businessSQL == null)
                {
                    String sql = "INSERT INTO BUSINESS VALUES(?,?,?,?,?,?,?,?,?,?,?,?)";
                    businessSQL = con.prepareStatement(sql);
                }
                businessSQL.setString(1,business_id);
                businessSQL.setString(2,full_address);
                businessSQL.setBoolean(3,open);
                businessSQL.setString(4,city);
                businessSQL.setLong(5,review_count);
                businessSQL.setString(6,business_name);
                businessSQL.setString(7, String.valueOf(neighborhoods));
                businessSQL.setDouble(8,longitude);
                businessSQL.setString(9,state);
                businessSQL.setDouble(10,stars);
                businessSQL.setDouble(11,latitude);
                businessSQL.setString(12,type);
                businessSQL.executeUpdate();

            }

            System.out.println("Insert data into Attribute table...");
             String sql = "INSERT INTO BUSINESS_TO_ATTRIBUTE(BUSINESS_ID,ATTRIBUTE_NAME) VALUES (?, ?)";
            PreparedStatement preparedStatement = con.prepareStatement(sql);

            for (Attribute a: attributes) {

                preparedStatement.setString(1, a.business_id);
                preparedStatement.setString(2, a.attribute);
                preparedStatement.executeUpdate();
            }
            preparedStatement.close();

            businessSQL.close();
            bufferedReader.close();

            } catch (Exception e){
                e.printStackTrace();

        }



    }

}

