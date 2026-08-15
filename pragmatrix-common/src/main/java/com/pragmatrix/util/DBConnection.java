package com.pragmatrix.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Manages a HikariCP connection pool for the application.
 * Initialised once on app startup via AppContextListener and
 * provides connections to all DAO classes.
 */
public class DBConnection {

    private static HikariDataSource dataSource;

    private DBConnection() {} // utility class

    /**
     * Initialise the HikariCP pool. Called once from AppContextListener.
     */
    public static synchronized void init() {
        if (dataSource != null) return;

        Properties props = new Properties();
        try (InputStream is = DBConnection.class.getClassLoader()
                .getResourceAsStream("db.properties")) {
            if (is == null) {
                throw new RuntimeException("db.properties not found on classpath!");
            }
            props.load(is);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load db.properties", e);
        }

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(props.getProperty("db.url"));
        config.setUsername(props.getProperty("db.username"));
        config.setPassword(props.getProperty("db.password"));
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");

        config.setMaximumPoolSize(
                Integer.parseInt(props.getProperty("db.pool.maxSize", "15")));
        config.setMinimumIdle(
                Integer.parseInt(props.getProperty("db.pool.minIdle", "5")));
        config.setConnectionTimeout(
                Long.parseLong(props.getProperty("db.pool.connectionTimeout", "30000")));
        config.setIdleTimeout(
                Long.parseLong(props.getProperty("db.pool.idleTimeout", "600000")));
        config.setMaxLifetime(
                Long.parseLong(props.getProperty("db.pool.maxLifetime", "1800000")));

        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        dataSource = new HikariDataSource(config);
    }

    /**
     * Get a connection from the pool.
     */
    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("Connection pool not initialised. Call DBConnection.init() first.");
        }
        return dataSource.getConnection();
    }

    /**
     * Shut down the connection pool. Called from AppContextListener on app destroy.
     */
    public static synchronized void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            dataSource = null;
        }
    }
}
