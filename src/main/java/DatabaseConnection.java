package main.java;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Class for managing database connections and JDBC operations
 */
public class DatabaseConnection {
    // Database connection parameters
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/hn";
    private static final String USER = "root";
    private static final String PASSWORD = "Alexutu2004$";

    private Connection connection;

    /**
     * Constructor that initializes the connection
     */
    public DatabaseConnection() {
        try {
            // Load JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("Failed to load MySQL driver: " + e.getMessage());
        }
    }

    /**
     * Opens a connection to the database
     * @return connection if successful, null otherwise
     */
    public Connection connect() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
                System.out.println("Database connection established successfully!");
            }
            return connection;
        } catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
            return null;
        }
    }

    /**
     * Closes the database connection
     */
    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }

    /**
     * Executes a SELECT query
     * @param query SQL query string
     * @return ResultSet with query results
     */
    public ResultSet executeQuery(String query) {
        try {
            Connection conn = connect();
            if (conn != null) {
                Statement statement = conn.createStatement();
                return statement.executeQuery(query);
            }
        } catch (SQLException e) {
            System.err.println("Query execution error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Executes an UPDATE, INSERT or DELETE query
     * @param query SQL query string
     * @return number of affected rows
     */
    public int executeUpdate(String query) {
        try {
            Connection conn = connect();
            if (conn != null) {
                Statement statement = conn.createStatement();
                return statement.executeUpdate(query);
            }
        } catch (SQLException e) {
            System.err.println("Update execution error: " + e.getMessage());
        }
        return -1;
    }

    /**
     * Creates a PreparedStatement for parameterized queries
     * @param query SQL query with parameters
     * @return created PreparedStatement
     */
    public PreparedStatement prepareStatement(String query) {
        try {
            Connection conn = connect();
            if (conn != null) {
                return conn.prepareStatement(query);
            }
        } catch (SQLException e) {
            System.err.println("PreparedStatement creation error: " + e.getMessage());
        }
        return null;
    }

    // Test method for connection
    public static void main(String[] args) {
        DatabaseConnection dbConnection = new DatabaseConnection();
        Connection connection = dbConnection.connect();

        if (connection != null) {
            System.out.println("Test connection successful!");
            dbConnection.disconnect();
        } else {
            System.out.println("Test connection failed!");
        }
    }
}
