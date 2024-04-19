import java.sql.Connection; // Importing necessary SQL classes for database connection
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

public class AclPapersDatabase {
    private static final Scanner s = new Scanner(System.in); // Scanner object for user input
    private static Connection c; // Database connection object

    public static void main(String[] a) { // Main method
        try {
            connect(); // Establishing database connection
            executeSQL(); // Executing SQL script
            menu(); // Displaying main menu
        } catch (SQLException e) { // Handling SQL exceptions
            System.out.println("Error: " + e.getMessage());
        } finally {
            try {
                if (c != null && !c.isClosed()) {
                    c.close(); // Closing database connection
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    private static void connect() throws SQLException { // Method to connect to the database
        System.out.print("Enter Oracle username: "); // Prompting user for Oracle username
        String u = s.nextLine(); // Reading username input
        System.out.print("Enter Oracle password: "); // Prompting user for Oracle password
        String p = s.nextLine(); // Reading password input

        String dbURL = "using dburl"; // Database URL

        c = DriverManager.getConnection(dbURL, u, p); // Establishing connection with the database
        System.out.println("Connected successfully."); // Connection success message
    }

    private static void executeSQL() { // Method to execute SQL script
        System.out.print("Enter paper.sql script file location: "); // Prompting user for script file location
        String loc = s.nextLine(); // Reading script file location input
        try {
            String sql = new String(Files.readAllBytes(Paths.get(loc))); // Reading SQL script file
            Statement st = c.createStatement(); // Creating statement for SQL execution
            st.execute(sql); // Executing SQL script
            System.out.println("SQL script executed."); // Success message
        } catch (IOException | SQLException e) { // Handling IO and SQL exceptions
            System.out.println("Error executing SQL script: " + e.getMessage());
        }
    }

    private static void menu() { // Method to display main menu
        String o;
        do {
            System.out.println("\n1. View table contents"); // Displaying menu options
            System.out.println("2. Search by PUBLICATIONID");
            System.out.println("3. Search by attributes");
            System.out.println("4. Exit");
            System.out.print("Choose option: ");
            o = s.nextLine(); // Reading user input

            switch (o) { // Menu option selection
                case "1":
                    view(); // Viewing table contents
                    break;
                case "2":
                    searchId(); // Searching by PUBLICATIONID
                    break;
                case "3":
                    searchAttr(); // Searching by attributes
                    break;
                case "4":
                    System.out.println("Exiting."); // Exiting the program
                    break;
                default:
                    System.out.println("Invalid option. Try again."); // Invalid option message
            }
        } while (!o.equals("4")); // Loop until user chooses to exit
    }

    private static void view() { // Method to view table contents
        try {
            System.out.println("PUBLICATIONS (Yes/No):"); // Prompting user for table choice
            String pub = s.nextLine().toLowerCase(); // Reading user input
            System.out.println("AUTHORS (Yes/No):"); // Prompting user for table choice
            String auth = s.nextLine().toLowerCase(); // Reading user input

            if (pub.equals("yes")) { // Viewing PUBLICATIONS table
                PreparedStatement ps = c.prepareStatement("SELECT * FROM PUBLICATIONS");
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    System.out.println(rs.getInt(1) + ", " +
                            rs.getString(2) + ", " +
                            rs.getInt(3) + ", " +
                            rs.getString(4) + ", " +
                            rs.getString(5));
                }
                rs.close();
                ps.close();
            }
            if (auth.equals("yes")) { // Viewing AUTHORS table
                PreparedStatement ps = c.prepareStatement("SELECT * FROM AUTHORS");
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    System.out.println(rs.getInt(1) + ", " + rs.getString(2));
                }
                rs.close();
                ps.close();
            }
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
        }
    }

    private static void searchId() { // Method to search by PUBLICATIONID
        try {
            System.out.print("Enter PUBLICATIONID: "); // Prompting user for input
            int id = Integer.parseInt(s.nextLine()); // Reading user input
            PreparedStatement ps = c.prepareStatement(
                "SELECT p.*, (SELECT COUNT(*) FROM AUTHORS a WHERE a.PUBLICATIONID = p.PUBLICATIONID) AS COUNT " +
                "FROM PUBLICATIONS p WHERE p.PUBLICATIONID = ?");
            ps.setInt(1, id); // Setting parameter value
            ResultSet rs = ps.executeQuery(); // Executing query
            if (rs.next()) {
                System.out.println("PUBLICATIONID: " + rs.getInt(1)); // Displaying results
                System.out.println("TITLE: " + rs.getString(2));
                System.out.println("YEAR: " + rs.getInt(3));
                System.out.println("TYPE: " + rs.getString(4));
                System.out.println("SUMMARY: " + rs.getString(5));
                System.out.println("AUTHOR COUNT: " + rs.getInt("COUNT"));
            } else {
                System.out.println("No results found."); // No results message
            }
            rs.close();
            ps.close();
        } catch (NumberFormatException e) { // Handling input format exception
            System.out.println("Invalid input. PUBLICATIONID must be a number.");
        } catch (SQLException e) { // Handling SQL exception
            System.out.println("SQL Error: " + e.getMessage());
        }
    }

    private static void searchAttr() { // Method to search by attributes
        try {
            System.out.print("AUTHOR: "); // Prompting user for input
            String auth = s.nextLine(); // Reading user input
            System.out.print("TITLE: "); // Prompting user for input
            String tit = s.nextLine(); // Reading user input
            System.out.print("YEAR: "); // Prompting user for input
            String y = s.nextLine(); // Reading user input
            System.out.print("TYPE: "); // Prompting user for input
            String ty = s.nextLine(); // Reading user input

            String sql = "SELECT p.PUBLICATIONID, p.TITLE, p.YEAR, p.TYPE, p.SUMMARY, a.AUTHOR " +
                         "FROM PUBLICATIONS p LEFT JOIN AUTHORS a ON p.PUBLICATIONID = a.PUBLICATIONID WHERE ";
            StringBuilder con = new StringBuilder();
            List<String> p = new ArrayList<>();

            if (!auth.isEmpty()) { // Building SQL query conditions
                con.append("a.AUTHOR LIKE ? ");
                p.add("%" + auth + "%");
            }
            if (!tit.isEmpty()) {
                if (con.length() > 0) con.append("AND ");
                con.append("p.TITLE LIKE ? ");
                p.add("%" + tit + "%");
            }
            if (!y.isEmpty()) {
                if (con.length() > 0) con.append("AND ");
                con.append("p.YEAR = ? ");
                p.add(y);
            }
            if (!ty.isEmpty()) {
                if (con.length() > 0) con.append("AND ");
                con.append("p.TYPE LIKE ? ");
                p.add("%" + ty + "%");
            }

            if (con.length() == 0) { // No valid inputs specified
                System.out.println("No valid inputs.");
                return;
            }

            PreparedStatement ps = c.prepareStatement(sql + con.toString());
            for (int i = 0; i < p.size(); i++) {
                ps.setString(i + 1, p.get(i)); // Setting parameter values
            }

            ResultSet rs = ps.executeQuery(); // Executing query
            boolean f = false;
            while (rs.next()) {
                f = true;
                System.out.println("PUBLICATIONID: " + rs.getInt(1) + ", " + // Displaying results
                                   "TITLE: " + rs.getString(2) + ", " +
                                   "YEAR: " + rs.getInt(3) + ", " +
                                   "TYPE: " + rs.getString(4) + ", " +
                                   "SUMMARY: " + rs.getString(5) + ", " +
                                   "AUTHOR: " + rs.getString(6));
            }
            if (!f) {
                System.out.println("No results found."); // No results message
            }
            rs.close();
            ps.close();
        } catch (NumberFormatException e) { // Handling input format exception
            System.out.println("Invalid input. YEAR must be a number.");
        } catch (SQLException e) { // Handling SQL exception
            System.out.println("SQL Error: " + e.getMessage());
        }
    }
}
