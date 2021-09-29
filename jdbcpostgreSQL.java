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
      String tableName = "";

       
      //String sqlStatement = "INSERT INTO teammembers VALUES(\'Cranjis McBasketball\', 906,\'Monsters\', \'01/01/01\')";

      //send statement to DBMS
      //This executeQuery command is useful for data retrieval
      //ResultSet result = stmt.executeQuery(sqlStatement);
      //OR
      //This executeUpdate command is useful for updating data

      // result = stmt.executeUpdate(sqlStatement);






      // CREW.CSV
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






      //OUTPUT
      //You will need to output the results differently depeninding on which function you use
      System.out.println("--------------------Query Results--------------------");
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
