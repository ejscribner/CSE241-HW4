/*
 *	Elliot Scribner
 *	CSE 241
 *	Feb 15 2019
 */

import java.io.Console;
import java.sql.*;
import java.util.Scanner;

public class TeachingRecord {
    static String password;
    static String username;

    public static void main(String[] args) {
        Console console = System.console();
        boolean loggedIn = false;
        //create scanner
        Scanner scan = new Scanner(System.in);


        readLogin(scan, console, "Welcome to my cse241 hw4 project! Please log in below.");
        while(!loggedIn) {
            try (Connection con = DriverManager.getConnection("jdbc:oracle:thin:@edgar1.cse.lehigh.edu:1521:cse241", username, password);
                 Statement stmt = con.createStatement())
            {
                System.out.println("Connection Succesful.");
                loggedIn = true;
                String query = generateSubstringQuery(scan);
                ResultSet result1;
                result1 = stmt.executeQuery(query);
                if(!result1.next()) {
                    boolean queryIsEmpty = true;
                    while(queryIsEmpty) {
                        System.out.println("Empty Result.");
                        query = generateSubstringQuery(scan);
                        result1 = stmt.executeQuery(query);
                    }
                }
                System.out.println(" ID    " + "|" + "  Name");
                System.out.println("---------------");
                do {
                    System.out.println(result1.getString("ID") + " | " + result1.getString("Name"));
                } while(result1.next());
            }
            catch (Exception e) {
                System.out.println("1");
                if(e.getMessage() == "No suitable driver found for jdbc:oracle:thin:@edgar1.cse.lehigh.edu:1521:cse241") {
                    System.out.println("2");
                    readLogin(scan, console, "Error: Could Not Connect, Driver Not Found");
                    loggedIn = false;
                }
                System.out.println("3");
                if(e.getMessage() == "ORA-01017: invalid username/password; logon denied") {
                    System.out.println("4");
                    readLogin(scan, console, "Error: Could Not Connect, Invalid Password");
                    loggedIn = false;
                }
                System.out.println("5");
                if(e.getMessage() == "ORA-00942: table or view does not exist") {
                    System.out.println("6");
                    System.out.println("Error: Invalid Query, table or view does not exist.");
                }
                System.out.println("7");
                if(e.getMessage() == "ORA-28000: The account is locked.") {
                    System.out.println("8");
                    readLogin(scan, console, "Error: Your Account is Locked");
                    loggedIn = false;
                }
                System.out.println("8");
                //find more error cases by connecting to different database, database that is down, etc
                //query not in dtabase
                System.out.println("Message: " + e.getMessage());
                System.out.println("Error: Could Not Connect, We Dont Know Why");
                break;
            }
        }

    }

    static void readLogin(Scanner scan, Console console, String prompt) {
        System.out.println(prompt);
        System.out.print("Username: ");
        username = scan.next();
        password = new String(console.readPassword("Password: "));
        System.out.println("Connecting...");
    }

    static String generateSubstringQuery(Scanner scan) {
        System.out.println("Please enter a search substring for name: ");
        String searchSub = scan.next();
        String query;
        query = "select to_char(instructor.id, '00000')as \"ID\", NAME from INSTRUCTOR where NAME like \'%" + searchSub + "%\'";
        return query;
        //should add stmt.excecute here and handle exception seperately
    }
} 
