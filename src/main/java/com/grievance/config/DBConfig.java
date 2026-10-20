package com.grievance.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Reads DB settings from environment variables (DB_URL, DB_USER, DB_PASSWORD)
 * or from a config.properties file in the project folder. Nothing is hardcoded.
 */
public final class DBConfig {
    private static final Properties PROPS = new Properties();

    static {
        try (InputStream in = new FileInputStream("config.properties")) {
            PROPS.load(in);
        } catch (IOException e) {
            // File is optional when environment variables are used
        }
    }

    private DBConfig() { }

    private static String get(String envKey, String propKey, String fallback) {
        String v = System.getenv(envKey);
        if (v != null && !v.isBlank()) return v;
        return PROPS.getProperty(propKey, fallback);
    }

    public static Connection getConnection() throws SQLException {
        String url  = get("DB_URL", "db.url", "jdbc:mysql://localhost:3306/grievance_db");
        String user = get("DB_USER", "db.user", "root");
        String pass = get("DB_PASSWORD", "db.password", "");
        return DriverManager.getConnection(url, user, pass);
    }
}
