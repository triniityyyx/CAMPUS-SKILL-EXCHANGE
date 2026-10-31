package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

/**
 * Utility to automatically initialize the MySQL database and load sample data from sql/schema.sql.
 * Useful for 1-step project setup and evaluation.
 */
public class DatabaseInitializer {

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("     CAMPUS SKILL EXCHANGE - DB INITIALIZER       ");
        System.out.println("==================================================");

        // Connect to MySQL server root (without selecting database initially)
        String serverUrl = "jdbc:mysql://localhost:3306/?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&allowMultiQueries=true";
        String user = DBConnection.getUser();
        String password = DBConnection.getPassword();

        System.out.println("Connecting to MySQL at localhost:3306 as '" + user + "'...");

        File schemaFile = new File("sql/schema.sql");
        if (!schemaFile.exists()) {
            schemaFile = new File("campus-skill-exchange/sql/schema.sql");
        }

        if (!schemaFile.exists()) {
            System.err.println("Error: sql/schema.sql not found!");
            return;
        }

        try (Connection conn = DriverManager.getConnection(serverUrl, user, password);
             Statement stmt = conn.createStatement()) {

            System.out.println("Reading " + schemaFile.getAbsolutePath() + "...");
            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new FileReader(schemaFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String trimmed = line.trim();
                    if (trimmed.startsWith("--") || trimmed.isEmpty()) {
                        continue;
                    }
                    sb.append(line).append("\n");
                }
            }

            // Split into individual SQL statements by semicolon
            String[] statements = sb.toString().split(";");
            int executedCount = 0;

            for (String sql : statements) {
                String cleanSql = sql.trim();
                if (!cleanSql.isEmpty()) {
                    try {
                        stmt.execute(cleanSql);
                        executedCount++;
                    } catch (Exception ex) {
                        System.err.println("Notice on statement execution: " + ex.getMessage());
                    }
                }
            }

            System.out.println("==================================================");
            System.out.println("SUCCESS! Database initialized successfully.");
            System.out.println("Total statements executed: " + executedCount);
            System.out.println("Database 'campus_skill_exchange' is ready with sample data.");
            System.out.println("==================================================");

        } catch (Exception e) {
            System.err.println("Database initialization failed: " + e.getMessage());
            System.err.println("Please check your MySQL username and password in DBConnection.java or db.properties.");
        }
    }
}
