import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Student {

    public static Connection getConnection(String username, String password) throws SQLException {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            System.out.println("Oracle JDBC Driver Registered!");
        } catch (ClassNotFoundException e) {
            System.out.println("Where is your Oracle JDBC Driver?");
            e.printStackTrace();
            return null;
        }
        
        Connection connection = null;
        try {
            connection = DriverManager.getConnection("dburl_from_your_base", username, password);
            if (connection != null) {
                System.out.println("You are connected to the database!");
            } else {
                System.out.println("Failed to make connection!");
            }
        } catch (SQLException e) {
            System.out.println("Connection Failed! Check output console");
            e.printStackTrace();
            throw e;
        }

        return connection;
    }

    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Connection closed successfully.");
            } catch (SQLException ex) {
                System.out.println("Error closing connection");
                ex.printStackTrace();
            }
        }
    }
}
