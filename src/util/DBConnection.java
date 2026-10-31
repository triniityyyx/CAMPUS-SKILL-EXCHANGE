package util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Utility class to manage MySQL Database Connections using JDBC.
 * Uses PreparedStatement in all DAOs and manages connections cleanly.
 */
public class DBConnection {

    // =========================================================================
    // DATABASE CONFIGURATION - CHANGE YOUR CREDENTIALS HERE IF NEEDED
    // =========================================================================
    private static String URL = "jdbc:mysql://localhost:3306/campus_skill_exchange?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static String USER = "root";
    private static String PASSWORD = "password123"; // Set your MySQL root password here (e.g. root, root123, admin)
    // =========================================================================

    private static boolean driverLoaded = false;

    static {
        // Attempt to load external db.properties if present in the current working directory
        try {
            File propFile = new File("db.properties");
            if (propFile.exists()) {
                Properties props = new Properties();
                try (InputStream is = new FileInputStream(propFile)) {
                    props.load(is);
                    if (props.getProperty("db.url") != null) URL = props.getProperty("db.url");
                    if (props.getProperty("db.user") != null) USER = props.getProperty("db.user");
                    if (props.getProperty("db.password") != null) PASSWORD = props.getProperty("db.password");
                    System.out.println("[DBConnection] Loaded database credentials from db.properties");
                }
            }
        } catch (Exception e) {
            System.err.println("[DBConnection] Could not read db.properties, using default credentials: " + e.getMessage());
        }

        // Register MySQL JDBC Driver
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            driverLoaded = true;
            System.out.println("[DBConnection] MySQL Connector/J driver registered successfully.");
        } catch (ClassNotFoundException e) {
            System.err.println("[DBConnection] MySQL Driver not found. Ensure mysql-connector-j.jar is in classpath: " + e.getMessage());
        }
    }

    /**
     * Get a new connection to the database.
     * Callers should close this connection via try-with-resources.
     *
     * @return active Connection instance
     * @throws SQLException if a database access error occurs
     */
    public static Connection getConnection() throws SQLException {
        if (!driverLoaded) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                driverLoaded = true;
            } catch (ClassNotFoundException e) {
                throw new SQLException("MySQL JDBC Driver (com.mysql.cj.jdbc.Driver) not found in classpath. " +
                        "Please ensure mysql-connector-j.jar is included.", e);
            }
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /**
     * Test if database connection is alive.
     *
     * @return true if connection succeeds, false otherwise
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("[DBConnection Test Failed]: " + e.getMessage());
            return false;
        }
    }

    public static String getUrl() {
        return URL;
    }

    public static void setUrl(String url) {
        URL = url;
    }

    public static String getUser() {
        return USER;
    }

    public static void setUser(String user) {
        USER = user;
    }

    public static String getPassword() {
        return PASSWORD;
    }

    public static void setPassword(String password) {
        PASSWORD = password;
    }
}
