/*
 *	Elliot Scribner
 *	CSE 241
 *	Feb 15 2019
 */

import java.io.Console;
import java.net.IDN;
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
                System.out.println("Connection Succesful.\n");
                loggedIn = true;
                printResult1(scan, stmt);
            } catch (SQLException sqe) {
                System.out.println("code: " + sqe.getErrorCode());
                System.out.println("msg: " + sqe.getMessage());
                if(sqe.getErrorCode() == 28000) {
                    readLogin(scan, console, "Error: Your Account is Locked, Please Try Again");
                }
                if(sqe.getErrorCode() == 1017) {
                    readLogin(scan, console, "Error: Invalid Username or Password");
                }
                if(sqe.getMessage().contains("No suitable driver found")) {
                    readLogin(scan, console, "Error: Could Not Connect, Driver Not Found");
                }
                break;
            }
            catch (Exception e) {
                //find more error cases by connecting to different database, database that is down, etc
                //query not in dtabase
                System.out.println("Message: " + e.getMessage());
                System.out.println("Error: Could Not Connect, We Dont Know Why");
                break; //infinite looping on account locked for some reason, need to set logged in = true on success
            }
            int ctr = 0;
            ctr++;
            if (ctr > 5) {
                System.out.println("Too many failed login attempts.");
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

    static ResultSet generateSubstringQuery(Scanner scan, Statement stmt) {
        System.out.print("Please enter a search substring for name: ");
        String searchSub = scan.next();
        String query;
        query = "select to_char(instructor.id, '00000')as \"ID\", NAME from INSTRUCTOR where NAME like \'%" + searchSub + "%\'";
        ResultSet result = null;
        try {
            result = stmt.executeQuery(query);
        } catch (Exception executeException) {
            System.out.println("Err Msg: " + executeException.getMessage());
        }
        return result;
    }

    static void printResult1(Scanner scan, Statement stmt) {
        ResultSet result1;
        result1 = generateSubstringQuery(scan, stmt);
        try {
            while (!result1.next()) {
                System.out.println("Error: Empty Result. Try again.");
                result1 = generateSubstringQuery(scan, stmt);
            }
            System.out.println(" ID    " + "|" + "  Name");
            System.out.println("---------------");
            do {
                System.out.println(result1.getString("ID") + " | " + result1.getString("Name"));
            } while(result1.next());
        } catch (SQLException sqe) {
            System.out.println("SQl Error code: " + sqe.getErrorCode());
        } catch (Exception e) {
            System.out.println("Regular error msg: " + e.getMessage());
        }
    }
} 
