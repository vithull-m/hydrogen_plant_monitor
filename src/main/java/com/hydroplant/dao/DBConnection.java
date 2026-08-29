package com.hydroplant.dao;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Central point for obtaining JDBC connections to the MySQL database.
 * Reads connection settings from db.properties on the classpath so
 * credentials are not hard-coded into the source.
 */
public final class DBConnection {

    private static final String CONFIG_FILE = "/db.properties";
    private static String url;
    private static String username;
    private static String password;

    static {
        loadConfig();
    }

    private DBConnection() {
        // utility class - no instances
    }

    private static void loadConfig() {
        Properties props = new Properties();
        try (InputStream in = DBConnection.class.getResourceAsStream(CONFIG_FILE)) {
            if (in != null) {
                props.load(in);
            }
        } catch (IOException e) {
            System.err.println("Warning: could not load db.properties, falling back to defaults: " + e.getMessage());
        }
        url = props.getProperty("db.url", "jdbc:mysql://localhost:3306/hydrogen_plant?useSSL=false&serverTimezone=UTC");
        username = props.getProperty("db.username", "root");
        password = props.getProperty("db.password", "");
    }

    /**
     * Opens a new JDBC connection. Caller is responsible for closing it
     * (use try-with-resources).
     */
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC driver not found on classpath. " +
                    "Add mysql-connector-j to your project libraries.", e);
        }
        return DriverManager.getConnection(url, username, password);
    }

    /** Quick connectivity check used by the login/startup screen. */
    public static boolean testConnection() {
        try (Connection c = getConnection()) {
            return c != null && !c.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}
