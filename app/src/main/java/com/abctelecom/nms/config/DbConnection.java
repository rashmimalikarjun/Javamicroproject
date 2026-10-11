package com.abctelecom.nms.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * Minimal JDBC connection provider (no pooling dependency, appropriate for a
 * Swing desktop app with a handful of background workers).
 */
public final class DbConnection {
    private static final Logger LOG = Logger.getLogger(DbConnection.class.getName());
    private static boolean driverChecked = false;

    private DbConnection() {}

    private static void ensureDriver() {
        if (!driverChecked) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("MySQL JDBC driver not on classpath", e);
            }
            driverChecked = true;
        }
    }

    public static Connection get() throws SQLException {
        ensureDriver();
        return DriverManager.getConnection(
                AppConfig.getDbUrl(), AppConfig.getDbUser(), AppConfig.getDbPassword());
    }

    /** Quick connectivity probe used by splash/settings screens. */
    public static boolean testConnection() {
        try (Connection c = get()) {
            return c.isValid(3);
        } catch (SQLException e) {
            LOG.warning("DB connection test failed: " + e.getMessage());
            return false;
        }
    }
}
