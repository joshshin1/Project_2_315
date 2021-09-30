import java.sql.*;
import java.io.*;
import java.util.Scanner;
import java.nio.charset.Charset; // check for non-ascii chars

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

  public static void populateDB(String tableName, String[] types, String fileName, Boolean[] allowEmpty, Statement stmt){
    try{
      //create a statement object
      int result;
      String dropTableStatement = "DROP TABLE "+ tableName;

      Scanner sc = new Scanner(new File(fileName));
      sc.useDelimiter("\t");
      String line = sc.nextLine();
      String[] lineArr = line.split("\t");

      String makeTableStatement = "CREATE TABLE " + tableName + " (";
      //System.out.println(lineArr.length);
      for(int i=0; i<types.length; i++){
        makeTableStatement += lineArr[i+1] + " " + types[i];
        if(i < types.length-1){
          makeTableStatement += ", ";
        }
      }
      makeTableStatement +=")";

      result = stmt.executeUpdate(dropTableStatement); //Drop previous table
      result = stmt.executeUpdate(makeTableStatement); //Make empty table

      String sqlStatement = "";
      String[] attrArr;
      
      int cnt = 0;  //for testing
      boolean skip = false;
      while(sc.hasNext()){
        line = sc.nextLine();
        lineArr = line.split("\t");

        // Possible amount that could be empty and not skip the entry
        int numEmpty=0;
        for(int i=0; i<allowEmpty.length; i++){
          if(allowEmpty[i]){
            numEmpty++;
          }
        }
        // Skip if there are not enough attributes to match the types
        if(lineArr.length+numEmpty < types.length+1){
          skip = true;
          System.out.println("SKIP: ");
        }
        for(int i=1; i<lineArr.length; i++){
          // Skip bad data
          if(lineArr[i] == "" || lineArr[i].contains("\'") || !Charset.forName("US-ASCII").newEncoder().canEncode(lineArr[i])){
            if(allowEmpty[i-1]){
              if(types[i-1] == "int"){
                lineArr[i] = "-1";
              }
              else{
                lineArr[i] = " ";
              }
            }
            else{
              skip = true;
              System.out.println("SKIP: (" + lineArr[i]+")");
              break;
            }
          }
        }
        if(!skip){
          sqlStatement = "INSERT INTO "+tableName+" VALUES(";
          for(int i=1; i<lineArr.length; i++){
            // Check data type and handle appropriately
            if(types[i-1] == "text" || types[i-1] == "date"){
              sqlStatement += "\'"+lineArr[i].replaceAll("\"", "") + "\'";
            }
            else if(types[i-1] == "text[]"){
              attrArr = lineArr[i].split(",");
              sqlStatement += "ARRAY[";
              // Construct input array
              for(int j=0; j<attrArr.length;j++){
                sqlStatement += "\'"+attrArr[j].replaceAll("[\"\\[\\]]", "") + "\'";
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
          System.out.println(sqlStatement);
          result = stmt.executeUpdate(sqlStatement);
        }
        
        // Testing only first 100 because time
        cnt++;
        if(cnt > 100){
          break;
        }

        skip = false;
      }
      sc.close();

    } catch (Exception e){
      e.printStackTrace();
      System.err.println(e.getClass().getName()+": "+e.getMessage());
      System.exit(0);
    }
  }



  //MAKE SURE YOU ARE ON VPN or TAMU WIFI TO ACCESS DATABASE
  public static void main(String args[]) {
 
    //Building the connection with your credentials
    //TODO: update dbName, userName, and userPassword here
     Connection conn = null;
     String teamNumber = "12";
     String sectionNumber = "906";
     String dbName = "csce315" + sectionNumber + "_" + teamNumber + "db";
     String dbConnectionString = "jdbc:postgresql://csce-315-db.engr.tamu.edu/" + dbName;
     String userName = "csce315" + sectionNumber + "_" + teamNumber + "user";
     String userPassword = "Password123";

    //Connecting to the database 
    try {
      conn = DriverManager.getConnection(dbConnectionString,userName, userPassword);
    } catch (Exception e) {
      e.printStackTrace();
      System.err.println(e.getClass().getName()+": "+e.getMessage());
      System.exit(0);
    }

    System.out.println("Opened database successfully");
     
    try{
      //create a statement object
      Statement stmt = conn.createStatement();
      int result;

      //Running a query
      //TODO: update the sql command here
      //psql -h csce-315-db.engr.tamu.edu -U csce315[SectionNumber]_[TeamNumber]usercsce315[SectionNumber]_[TeamNumber]db

      //String tableName = "";


      String[] crewTypes = {"text", "text[]", "text[]"};
      Boolean[] crewEmpty = {false, false, false};
      populateDB("crew", crewTypes, "crew.csv", crewEmpty, stmt);

      String[] namesTypes = {"text", "text", "int", "int", "text[]"};
      Boolean[] namesEmpty = {false, false, false, true, false};
      populateDB("names", namesTypes, "names.csv", namesEmpty, stmt);

      String[] customerRatingsTypes = {"int", "int", "date", "text"};
      Boolean[] customerRatingsEmpty = {false, false, false, false};
      populateDB("customer_ratings", customerRatingsTypes, "customer_ratings.csv", customerRatingsEmpty, stmt);

      String[] titlesTypes = {"text", "text", "text", "int", "int", "int", "text[]", "int", "real", "int"};
      Boolean[] titlesEmpty = {false, false, false, true, true, false, false, true, false, false};
      populateDB("titles", titlesTypes, "titles.csv", titlesEmpty, stmt);

      String[] principalsTypes = {"text", "text", "text", "text", "text[]"};
      Boolean[] principalsEmpty = {false, false, false, true, true};
      populateDB("principals", principalsTypes, "principals.csv", principalsEmpty, stmt);
      

       
      //String sqlStatement = "INSERT INTO teammembers VALUES(\'Cranjis McBasketball\', 906,\'Monsters\', \'01/01/01\')";

      //send statement to DBMS
      //This executeQuery command is useful for data retrieval
      //ResultSet result = stmt.executeQuery(sqlStatement);
      //OR
      //This executeUpdate command is useful for updating data

      // result = stmt.executeUpdate(sqlStatement);






      // Old Way for reference
      /*
      String dropTableStatement = "DROP TABLE crew";
      String makeTableStatement = "CREATE TABLE crew (titleId text, directors text, writers text)";

      result = stmt.executeUpdate(dropTableStatement); //Drop previous table
      result = stmt.executeUpdate(makeTableStatement); //Make empty table

      String sqlStatement = "";

      Scanner sc = new Scanner(new File("crew.csv"));
      sc.useDelimiter("\t");
      int cnt = 0;
      String line = "";
      sc.nextLine();  // skip first line
      String[] lineArr;
      while(sc.hasNext()){
        line = sc.nextLine();
        lineArr = line.split("\t", 4);
        if(lineArr[1] == "" || lineArr[2] == "" || lineArr[3] == ""){
          System.out.println("SKIP");
        }
        else{
          sqlStatement = "INSERT INTO crew VALUES(\'"+lineArr[1]+"\', \'"+lineArr[2]+"\',\'"+lineArr[3]+"\')";
          result = stmt.executeUpdate(sqlStatement);
          for(int i=0; i<lineArr.length; i++){
            System.out.print(lineArr[i] + " ");
          }
          System.out.println();
        }
        
        // Testing only first 100 because time
        cnt++;
        if(cnt > 100){
          break;
        }
      }
      sc.close();
      */






      //OUTPUT
      //You will need to output the results differently depeninding on which function you use
      //System.out.println("--------------------Query Results--------------------");
      //while (result.next()) {
      // System.out.println(result.getString("column_name"));
      //}
      //OR
      //System.out.println(result);
    } catch (Exception e){
      e.printStackTrace();
      System.err.println(e.getClass().getName()+": "+e.getMessage());
      System.exit(0);
   }
    
    //closing the connection
    try {
      conn.close();
      System.out.println("Connection Closed.");
    } catch(Exception e) {
      System.out.println("Connection NOT Closed.");
    }//end try catch
  }//end main
}//end Class
