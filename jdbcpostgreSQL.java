import java.sql.*;
import java.io.*;
import java.util.Scanner;
import java.nio.charset.Charset; // check for non-ascii chars
import java.util.*;  // for hashtable to check for duplicate titles

/*
CSCE 315
9-27-2021 Lab
 */
public class jdbcpostgreSQL {

  //Commands to run this script
  //This will compile all java files in this directory
  //javac *.java 
  //This command tells the file where to find the postgres jar which it needs to execute postgres commands, then executes the code
  //Windows: java -cp ".;postgresql-42.2.8.jar" jdbcpostgreSQL
  //Mac/Linux: java -cp ".:postgresql-42.2.8.jar" jdbcpostgreSQL

  public static void populateDB(String tableName, String[] types, String fileName, Boolean[] allowEmpty, Boolean[] omit, Statement stmt){
    try{
      Scanner sc = new Scanner(new File(fileName));
      sc.useDelimiter("\t");
      String line = sc.nextLine();
      String[] lineArr = line.split("\t");

      String makeTableStatement = "CREATE TABLE " + tableName + " (";
      for(int i = 0; i < types.length; i++){
        if(!omit[i]){
          makeTableStatement += lineArr[i+1] + " " + types[i];
          if(i < types.length-1){
            makeTableStatement += ", ";
          }
        }
        
      }
      makeTableStatement +=")";

      int result;
      //Drop previous table
      result = stmt.executeUpdate("DROP TABLE "+ tableName); 

      //Make empty table
      result = stmt.executeUpdate(makeTableStatement); 

      String sqlStatement = "";
      String[] attrArr;
      
      boolean skip = false;
      Hashtable<String, String> duplicates = new Hashtable<String, String>();
      while(sc.hasNext()){
        line = sc.nextLine();
        lineArr = line.split("\t");

        // Possible amount that could be empty and not skip the entry
        int numEmpty=0;
        for(int i = 0; i < allowEmpty.length; i++){
          if(allowEmpty[i]){
            numEmpty++;
          }
        }
        // Skip if there are not enough attributes to match the types
        if(lineArr.length+numEmpty < types.length+1){
          skip = true;
          System.out.println("SKIP: ");
        }
        for(int i = 1; i < lineArr.length; i++){
          // Skip bad data
          if(lineArr[i] == "" || lineArr[i].contains("\'") || !Charset.forName("US-ASCII").newEncoder().canEncode(lineArr[i])){
            if(allowEmpty[i - 1]){
              if(types[i - 1] == "int"){
                lineArr[i] = "-1";
              }
              else{
                lineArr[i] = " ";
              }
            }
            else{
              skip = true;
              System.out.println("SKIP: " + lineArr[i]);
              break;
            }
          }
        }
        if(!skip){
          sqlStatement = "INSERT INTO "+tableName+" VALUES(";
          for(int i = 1; i < lineArr.length; i++){
            // Check if this attribute is collected
            if(omit[i-1]){
              continue;
            }
            // Check data type and handle appropriately
            if(types[i - 1] == "text" || types[i - 1] == "date"){
              if(tableName == "titles" && i == 3){
                // Special case duplicate titles
                if(duplicates.get(lineArr[i]) == null){
                  sqlStatement += "\'"+lineArr[i].replaceAll("\"", "") + "\'";
                  duplicates.put(lineArr[i], "yes");
                }
                else{
                  skip = true;
                }
              }
              else if(tableName == "principals" && i == 4){
                // Sometimes the job also has the character without a tab so we remove the character completely
                String[] checkBad = lineArr[i].split(" ", 3);
                if(checkBad.length>1){
                  sqlStatement += "\'"+checkBad[0].replaceAll("\"", "") + "\'";
                }
                else{
                  sqlStatement += "\'"+lineArr[i].replaceAll("\"", "") + "\'";
                }
              }
              else{
                sqlStatement += "\'"+lineArr[i].replaceAll("\"", "") + "\'";
              }
            }
            else if(types[i - 1] == "text[]"){
              attrArr = lineArr[i].split(",");
              sqlStatement += "ARRAY[";
              // Construct input array
              for(int j = 0; j < attrArr.length; j++){
                sqlStatement += "\'"+attrArr[j].replaceAll("[\"\\[\\]]", "").replaceAll("\"", "") + "\'";
                if(j < attrArr.length-1){
                  sqlStatement += ", ";
                }
              }
              sqlStatement += "]";
            }
            else{
              sqlStatement += lineArr[i];
            }
            if(i < lineArr.length-1){
              sqlStatement += ", ";
            }
          }
          sqlStatement += ")";
          if(!skip){
            System.out.println(sqlStatement);
            result = stmt.executeUpdate(sqlStatement);
          }
          
        }
        skip = false;
      }
      sc.close();

    }
    catch (Exception e){
      e.printStackTrace();
      System.err.println(e.getClass().getName()+": "+e.getMessage());
      System.exit(0);
    }
  }



  //MAKE SURE YOU ARE ON VPN or TAMU WIFI TO ACCESS DATABASE
  public static void main(String args[]) {
 
    //Building the connection with your credentials
     Connection conn = null;
     String teamNumber = "12";
     String sectionNumber = "906";
     String dbName = "csce315" + sectionNumber + "_" + teamNumber + "db";
     String dbConnectionString = "jdbc:postgresql://csce-315-db.engr.tamu.edu/" + dbName;
     String userName = "csce315" + sectionNumber + "_" + teamNumber + "user";
     String userPassword = "Password123";

    //Connecting to the database 
    try{
      conn = DriverManager.getConnection(dbConnectionString,userName, userPassword);
    }
    catch (Exception e){
      e.printStackTrace();
      System.err.println(e.getClass().getName()+": "+e.getMessage());
      System.exit(0);
    }

    System.out.println("Opened database successfully");
     
    try{
      //create a statement object
      Statement stmt = conn.createStatement();

      // Populate the Database
      String[] crewTypes = {"text", "text[]", "text[]"};
      Boolean[] crewEmpty = {false, false, false};
      Boolean[] crewOmit = {false, false, false};
      //populateDB("crew", crewTypes, "crew.csv", crewEmpty, crewOmit, stmt);

      String[] namesTypes = {"text", "text", "int", "int", "text[]"};
      Boolean[] namesEmpty = {false, false, false, true, false};
      Boolean[] namesOmit = {false, false, false, false};
      //populateDB("names", namesTypes, "names.csv", namesEmpty, namesOmit, stmt);

      String[] customerRatingsTypes = {"int", "int", "date", "text"};
      Boolean[] customerRatingsEmpty = {false, false, false, false};
      Boolean[] customerRatingsOmit = {false, false, false, false};
      //populateDB("customer_ratings", customerRatingsTypes, "customer_ratings.csv", customerRatingsEmpty, customerRatingsOmit, stmt);

      String[] titlesTypes = {"text", "text", "text", "int", "int", "int", "text[]", "int", "real", "int"};
      Boolean[] titlesEmpty = {false, false, false, true, true, false, false, false, false, false};
      Boolean[] titlesOmit = {false, false, false, true, true, false, false, false, false, false};
      //populateDB("titles", titlesTypes, "titles.csv", titlesEmpty, titlesOmit, stmt);

      String[] principalsTypes = {"text", "text", "text", "text", "text[]"};
      Boolean[] principalsEmpty = {false, false, false, true, true};
      Boolean[] principalsOmit = {false, false, false, false, false};
      populateDB("principals", principalsTypes, "principals.csv", principalsEmpty, principalsOmit, stmt);
    }
    catch (Exception e){
      e.printStackTrace();
      System.err.println(e.getClass().getName()+": "+e.getMessage());
      System.exit(0);
    }
    
    //closing the connection
    try{
      conn.close();
      System.out.println("Connection Closed.");
    }
    catch(Exception e) {
      System.out.println("Connection NOT Closed.");
    }
  }
}
