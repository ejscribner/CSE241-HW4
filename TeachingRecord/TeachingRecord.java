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
    static int enteredId;

    public static void main(String[] args) {
        Console console = System.console();
        boolean loggedIn = false;
        //create scanner
        Scanner scan = new Scanner(System.in);

        readLogin(scan, console, "Welcome to my cse241 hw4 project! Please log in below.!!!");
        int ctr = 0;
        while (!loggedIn) {
            try (Connection con = DriverManager.getConnection("jdbc:oracle:thin:@edgar1.cse.lehigh.edu:1521:cse241", username, password);
                 Statement stmt = con.createStatement()) {
                System.out.println("Connection Successful.\n");
                loggedIn = true;
                printResult1(scan, stmt);
                if (!printTeachingRecord(scan, stmt)) {
                    con.close();
                    System.exit(0);
                }

            } catch (SQLException sqe) {
                if (sqe.getErrorCode() == 28000) {
                    readLogin(scan, console, "Error: Your Account is Locked, Please Try Again");
                }
                if (sqe.getErrorCode() == 1017) {
                    readLogin(scan, console, "Error: Invalid Username or Password");
                }
                if (sqe.getMessage().contains("No suitable driver found")) {
                    readLogin(scan, console, "Error: Could Not Connect, Driver Not Found");
                }
                if (sqe.getErrorCode() == 17002) {
                    readLogin(scan, console, sqe.getMessage());
                }
            } catch (Exception e) {
                if (e.getMessage().contains("String index out of range")) {
                    System.out.println("Error: Username/Password not read properly.");
                    System.out.println("Exiting Program...");
                } else {
                    System.out.println("Error: " + e.getMessage());
                    System.out.println("Exiting Program...");
                }
                return;
            }
            ctr++;
            if (ctr > 5) {
                System.out.println("Error: Too many failed login attempts.");
                break;
            }
        }

    }

    static void readLogin(Scanner scan, Console console, String prompt) {
        System.out.println(prompt);
        username = null;
        System.out.print("Username: ");
        username = scan.next();
        password = null;
        password = new String(console.readPassword("Password: "));
        System.out.println("Connecting...");
    }

    static ResultSet generateSubstringQuery(Scanner scan, Statement stmt) {
        scan.nextLine();
        System.out.print("Please enter a search substring for name: ");
        String searchSub = scan.next();
        ResultSet result;
        String query;
        query = "select to_char(instructor.id, '00000')as \"ID\", NAME from INSTRUCTOR where NAME like \'%" + searchSub + "%\'";
        try {
            result = stmt.executeQuery(query);
        } catch (SQLException executeException) {
            System.out.println("Error: " + executeException.getMessage());
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
            System.out.println(); //pad top
            System.out.println(" ID    " + "|" + "  Name");
            System.out.println("---------------");
            do {
                System.out.println(result1.getString("ID") + " | " + result1.getString("Name"));
            } while (result1.next());
            System.out.println(); //pad bottom
        } catch (SQLException sqe) {
            System.out.println("SQl Error code: " + sqe.getErrorCode());
        } catch (Exception e) {
            System.out.println("Regular error msg: " + e.getMessage());
        }
    }

    static int readID(Scanner scan) {
        //flushes out scanner
        scan.nextLine();
        int id;
        System.out.print("Please enter an ID number to view teaching record: ");
        try {
            id = scan.nextInt();
            System.out.println("Showing Results For: " + id);
        } catch (InputMismatchException imE) {
            System.out.println("Error: Invalid ID, Try Again.");
            id = readID(scan);
        }
        if (id < 1 || id > 99312) {
            System.out.println("Error: Invalid ID, Try Again.");
            id = readID(scan);
        }
        return id;
    }

    static ResultSet generateInstructorQuery(Statement stmt, Scanner scan) {
        enteredId = readID(scan);
        String query;
        ResultSet result;
        query = "select c.DEPT_NAME as Department," +
                "       c.COURSE_ID as CNO," +
                "       c.TITLE as Title," +
                "       t.SEC_ID as Sec," +
                "       t.SEMESTER as Sem," +
                "       t.YEAR as Year," +
                "       (select count(*) from takes where TAKES.SEC_ID = t.SEC_ID and TAKES.COURSE_ID = t.COURSE_ID) as Enrollment" +
                "       from INSTRUCTOR i natural join TEACHES t left join COURSE c on c.COURSE_ID = t.COURSE_ID where id = " + enteredId +
                " order by c.DEPT_NAME, c.COURSE_ID, t.YEAR, t.SEMESTER desc";
        try {
            result = stmt.executeQuery(query);
        } catch (SQLException sqE) {
            sqE.printStackTrace();
            result = generateInstructorQuery(stmt, scan);
        }

        return result;
    }

    static boolean printTeachingRecord(Scanner scan, Statement stmt) {
        ResultSet teachingRecord;
        teachingRecord = generateInstructorQuery(stmt, scan);
        try {
            while (!teachingRecord.next()) {
                boolean idExists = false;
                idExists = checkId(enteredId, stmt);
                if (!idExists) {
                    System.out.println("Error: No instructors found for ID: " + enteredId);
                    System.out.println("Exiting Program...");
                    return false;
                } else {
                    System.out.println("Error: Instructor " + enteredId + " has taught zero courses");
                    System.out.println("Exiting Program...");
                    return false;
                }
            }
            ResultSetMetaData setMetaData = teachingRecord.getMetaData();

            String titleOutput = String.format(" %-14s %-6s %-26s %-5s %-10s %-8s %s",
                    setMetaData.getColumnLabel(1),
                    setMetaData.getColumnLabel(2),
                    setMetaData.getColumnLabel(3),
                    setMetaData.getColumnLabel(4),
                    setMetaData.getColumnLabel(5),
                    setMetaData.getColumnLabel(6),
                    setMetaData.getColumnLabel(7));
            System.out.println(); //pad top
            System.out.println(titleOutput);
            System.out.println("---------------------------------------------------------------------------------------");
            do {
                String dept = teachingRecord.getString("Department");
                int cNo = teachingRecord.getInt("CNO");
                String title = teachingRecord.getString("Title");
                int section = teachingRecord.getInt("Sec");
                String semester = teachingRecord.getString("Sem");
                String year = teachingRecord.getString("Year");
                String enrollment = teachingRecord.getString("Enrollment");

                String output = String.format(" %-14s %-6d %-26s %-5d %-10s %-8s %s", dept, cNo, title, section, semester, year, enrollment);
                System.out.println(output);
            } while (teachingRecord.next());
            System.out.println(); //pad bottom
        } catch (SQLException sqe) {
            System.out.println("Error: " + sqe.getMessage());
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
        return true;
    }

    static boolean checkId(int id, Statement stmt) {
        String query;
        ResultSet result = null;
        query = "select * from instructor where id = " + id;
        try {
            result = stmt.executeQuery(query);
            if (!result.next()) {
                return false;
            }
        } catch (SQLException sqE) {
            System.out.println("Error: " + sqE.getMessage());
        }
        return true;
    }

}
