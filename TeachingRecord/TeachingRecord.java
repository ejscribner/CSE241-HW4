/*
 *	Elliot Scribner
 *	CSE 241
 *	Feb 15 2019
 */

import java.io.Console;
import java.sql.*;
import java.util.InputMismatchException;
import java.util.Scanner;

public class TeachingRecord {
    static String password;
    static String username;

    public static void main(String[] args) {
        Console console = System.console();
        boolean loggedIn = false;
        //create scanner
        Scanner scan = new Scanner(System.in);

        readLogin(scan, console, "Welcome to my cse241 hw4 project! Please log in below.!!!");

        while(!loggedIn) {
            try (Connection con = DriverManager.getConnection("jdbc:oracle:thin:@edgar1.cse.lehigh.edu:1521:cse241", username, password);
                 Statement stmt = con.createStatement())
            {
                System.out.println("Connection Succesful.\n");
                loggedIn = true;
                printResult1(scan, stmt);
                printTeachingRecord(scan, stmt);
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
                if(sqe.getErrorCode() == 17002) {
                    readLogin(scan, console, sqe.getMessage());
                }
                break;
            }
            catch (Exception e) {
                //find more error cases by connecting to different database, database that is down, etc
                //query not in dtabase
                System.out.println("Message: " + e.getMessage());
                System.out.println("Error: Could Not Connect, We Dont Know Why");
                e.printStackTrace();
                break; //infinite looping on account locked for some reason, need to set logged in = true on success'
                //not re-trying con after first failure, remove break and fix.
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
        ResultSet result;
        try {
            result = stmt.executeQuery(query);
        } catch (SQLException executeException) {
            System.out.println("Error - " + executeException.getMessage());
            result = generateSubstringQuery(scan, stmt);
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
            System.out.println("Regular error msg: " + e.getMessage()); //.next or .getstring throwing regular exception here
        }
    }

    static int readID(Scanner scan) {
        //flushes out scanner (double check errors)
        scan.nextLine();
        int id;
        System.out.print("Please enter an ID number to view teaching record: ");
        try {
            id = scan.nextInt();
            System.out.println("input: " + id); //print out results call
        } catch (InputMismatchException imE) {
            System.out.println("Error: Invalid ID, Try Again.");
            id = readID(scan);
        }
        if(id < 1 || id > 99312) {
            System.out.println("Error: Invalid ID, Try Again.");
            id = readID(scan);
        }
        return id;
    }

    static ResultSet generateInstructorQuery(Statement stmt, Scanner scan) {
        int id = readID(scan);
        String query;
        ResultSet result;
        query = "select c.DEPT_NAME as Department,\n" +
                "       c.COURSE_ID as CNO,\n" +
                "       c.TITLE as Title,\n" +
                "       t.SEC_ID as Sec,\n" +
                "       t.SEMESTER as Sem,\n" +
                "       t.YEAR as Year,\n" +
                "       (select count(*) from takes where TAKES.SEC_ID = t.SEC_ID and TAKES.COURSE_ID = t.COURSE_ID) as Enrollment\n" +
                "       from INSTRUCTOR i natural join TEACHES t left join COURSE c on c.COURSE_ID = t.COURSE_ID where id = " + id +
                " order by c.DEPT_NAME, c.COURSE_ID, t.YEAR, t.SEMESTER desc";
        try {
            result = stmt.executeQuery(query);
            if(!result.next()) {
                //**************** should be handled here?
                System.out.println("1Error: Empty Teaching Record, Instructor " + id + "has taught zero courses"); //clean up, add the guys name
            }
        } catch (SQLException sqE) {
            //fix errors, must catch empty return and instructor not existing
            sqE.printStackTrace();
            result = generateInstructorQuery(stmt, scan);
        }

        return result;
    }

    static void printTeachingRecord(Scanner scan, Statement stmt) {
        ResultSet teachingRecord;
        teachingRecord = generateInstructorQuery(stmt, scan);
        try {
            while (!teachingRecord.next()) {
                //**************** or should be handled here?
                System.out.println("2Error: Empty Teaching Record, Instructor **id**"  +   "has taught zero courses"); //clean up, add the guys name
    //how to check if instructor exists or not, and how to check if they have any courses or not
                //prob needs sql statements
                System.out.println("3Error: Empty Result. Try again."); //creates infinite on id 92668
                teachingRecord = generateInstructorQuery(stmt, scan); //passing same id
            }
            System.out.println(" Department     CNO   Title    Sec   Sem   Year   Enrollment" ); //fix get metadata and formatting
            System.out.println("-------------------------------------------------------------");
            do {
                System.out.println(teachingRecord.getString("Department") + " | "
                        + teachingRecord.getInt("CNO") + " | "
                        + teachingRecord.getString("Title") + " | "
                        + teachingRecord.getInt("Sec") + " | "
                        + teachingRecord.getString("Sem") + " | "
                        + teachingRecord.getString("Year") + " | "
                        + teachingRecord.getString("Enrollment") + " | "
                );
            } while(teachingRecord.next());
        } catch (SQLException sqe) {
            System.out.println("SQl Error code: " + sqe.getErrorCode());
        } catch (Exception e) {
            System.out.println("Regular error msg: " + e.getMessage()); //.next or .getstring throwing regular exception here
        }
    }
} 
