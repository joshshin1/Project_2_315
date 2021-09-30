import java.sql.*;
import java.io.*;
import java.util.Scanner;

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

  public static void populateDB(String tableName, String[] types, String fileName, Statement stmt){
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
        System.out.println(makeTableStatement);
        makeTableStatement += lineArr[i+1] + " " + types[i];
        if(i < types.length-1){
          makeTableStatement += ", ";
        }
      }
      makeTableStatement +=")";

      result = stmt.executeUpdate(dropTableStatement); //Drop previous table
      result = stmt.executeUpdate(makeTableStatement); //Make empty table

      String sqlStatement = "";
      
      int cnt = 0;  //for testing
      boolean skip = false;
      while(sc.hasNext()){
        line = sc.nextLine();
        lineArr = line.split("\t");
        for(int i=1; i<lineArr.length; i++){  //start at 1 because idk what the first slot is
          if(lineArr[i] == "" || lineArr[i].contains("\'")){
            skip = true;
            System.out.println("SKIP: " + lineArr[i]);
            break;
          }
        }
        if(!skip){
          sqlStatement = "INSERT INTO "+tableName+" VALUES(";
          for(int i=1; i<lineArr.length; i++){
            if(types[i-1] == "text"){  //check to see if I need single quotes
              sqlStatement += "\'"+lineArr[i] + "\'";
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
      String[] crewTypes = {"text", "text", "text"};
      populateDB("crew", crewTypes, "crew.csv", stmt);
      String[] namesTypes = {"text", "text", "int", "int", "text"};
      populateDB("names", namesTypes, "names.csv", stmt);

      

       
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
