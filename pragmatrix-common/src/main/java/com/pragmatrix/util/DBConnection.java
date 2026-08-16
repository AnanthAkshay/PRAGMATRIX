package com.pragmatrix.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Manages a HikariCP connection pool for the PRAGMATRIX 2026 application.
 * Shared across both Public and Admin web application contexts.
 *
 * <p>Configuration Resolution Order:
 * <ol>
 *   <li><b>Environment Variables</b>: DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD (with SSL required for Aiven/cloud DB).</li>
 *   <li><b>Local Fallback Properties</b>: {@code db.local.properties} (classpath or root directory).</li>
 *   <li><b>Default Properties</b>: {@code db.properties} on classpath.</li>
 * </ol>
 * </p>
 */
public class DBConnection {

    private static volatile HikariDataSource dataSource;
    private static final Object LOCK = new Object();

    // Default Conservative Pool Configuration (tuned for cloud/Aiven connection limits)
    private static final int DEFAULT_MAX_POOL_SIZE = 10;
    private static final int DEFAULT_MIN_IDLE = 2;
    private static final long DEFAULT_CONNECTION_TIMEOUT = 30000L;
    private static final long DEFAULT_IDLE_TIMEOUT = 600000L;
    private static final long DEFAULT_MAX_LIFETIME = 1800000L;

    private DBConnection() {} // utility class

    /**
     * Initialise the HikariCP pool. Called once on startup or lazily on first connection.
     */
    public static void init() {
        if (dataSource != null && !dataSource.isClosed()) return;

        synchronized (LOCK) {
            if (dataSource != null && !dataSource.isClosed()) return;

            HikariConfig config = buildHikariConfig();
            dataSource = new HikariDataSource(config);
            System.out.println("[PRAGMATRIX-DB] HikariCP Connection Pool initialised successfully. (Max Pool: "
                    + config.getMaximumPoolSize() + ", URL: " + config.getJdbcUrl() + ")");
        }
    }

    /**
     * Builds HikariConfig from environment variables or fallback properties.
     */
    private static HikariConfig buildHikariConfig() {
        Properties fileProps = loadFallbackProperties();

        String envHost = getEnvOrProp("DB_HOST", "db.host", fileProps);
        String envPort = getEnvOrProp("DB_PORT", "db.port", fileProps, "3306");
        String envName = getEnvOrProp("DB_NAME", "db.name", fileProps);
        String envUser = getEnvOrProp("DB_USER", "db.user", fileProps);
        if (envUser == null) {
            envUser = getEnvOrProp("DB_USERNAME", "db.username", fileProps);
        }
        String envPass = getEnvOrProp("DB_PASSWORD", "db.password", fileProps);

        HikariConfig config = new HikariConfig();
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");

        if (envHost != null && !envHost.trim().isEmpty() && envName != null && !envName.trim().isEmpty()) {
            // Cloud / Aiven Configuration with SSL Required
            String jdbcUrl = String.format(
                    "jdbc:mysql://%s:%s/%s?useSSL=true&requireSSL=true&sslMode=REQUIRED&allowPublicKeyRetrieval=true&characterEncoding=UTF-8",
                    envHost.trim(),
                    envPort.trim(),
                    envName.trim()
            );
            config.setJdbcUrl(jdbcUrl);
            config.setUsername(envUser != null ? envUser.trim() : "");
            config.setPassword(envPass != null ? envPass.trim() : "");
            System.out.println("[PRAGMATRIX-DB] Using database configuration for host: " + envHost.trim() + " (SSL REQUIRED)");
        } else {
            // Fallback to legacy/local db.url from properties file
            String dbUrl = fileProps.getProperty("db.url");
            String dbUser = fileProps.getProperty("db.username");
            String dbPass = fileProps.getProperty("db.password");

            if (dbUrl != null && !dbUrl.trim().isEmpty()) {
                config.setJdbcUrl(dbUrl.trim());
                config.setUsername(dbUser != null ? dbUser.trim() : "");
                config.setPassword(dbPass != null ? dbPass.trim() : "");
                System.out.println("[PRAGMATRIX-DB] Using fallback database URL from properties file.");
            } else {
                throw new IllegalStateException("[PRAGMATRIX-DB] Database connection details not configured! Please set DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD environment variables or configure db.local.properties.");
            }
        }

        // Pool Sizing and Timeouts
        int maxPool = parsePositiveInt(getEnvOrProp("DB_POOL_MAX_SIZE", "db.pool.maxSize", fileProps), DEFAULT_MAX_POOL_SIZE);
        int minIdle = parsePositiveInt(getEnvOrProp("DB_POOL_MIN_IDLE", "db.pool.minIdle", fileProps), DEFAULT_MIN_IDLE);
        long connTimeout = parsePositiveLong(getEnvOrProp("DB_POOL_TIMEOUT", "db.pool.connectionTimeout", fileProps), DEFAULT_CONNECTION_TIMEOUT);
        long idleTimeout = parsePositiveLong(getEnvOrProp("DB_POOL_IDLE_TIMEOUT", "db.pool.idleTimeout", fileProps), DEFAULT_IDLE_TIMEOUT);
        long maxLifetime = parsePositiveLong(getEnvOrProp("DB_POOL_MAX_LIFETIME", "db.pool.maxLifetime", fileProps), DEFAULT_MAX_LIFETIME);

        config.setMaximumPoolSize(maxPool);
        config.setMinimumIdle(Math.min(minIdle, maxPool));
        config.setConnectionTimeout(connTimeout);
        config.setIdleTimeout(idleTimeout);
        config.setMaxLifetime(maxLifetime);

        // Performance & Reliability properties for MySQL Connector/J
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("useLocalSessionState", "true");
        config.addDataSourceProperty("rewriteBatchedStatements", "true");
        config.addDataSourceProperty("cacheResultSetMetadata", "true");
        config.addDataSourceProperty("cacheServerConfiguration", "true");
        config.addDataSourceProperty("elideSetAutoCommits", "true");
        config.addDataSourceProperty("maintainTimeStats", "false");

        return config;
    }

    /**
     * Loads fallback properties from db.local.properties or db.properties.
     */
    private static Properties loadFallbackProperties() {
        Properties props = new Properties();

        // 1. Try db.local.properties from ClassLoader
        try (InputStream is = DBConnection.class.getClassLoader().getResourceAsStream("db.local.properties")) {
            if (is != null) {
                props.load(is);
                System.out.println("[PRAGMATRIX-DB] Loaded fallback configuration from classpath: db.local.properties");
                return props;
            }
        } catch (IOException ignored) {}

        // 2. Try db.local.properties from working directory
        File localFile = new File("db.local.properties");
        if (localFile.exists() && localFile.isFile()) {
            try (InputStream is = new FileInputStream(localFile)) {
                props.load(is);
                System.out.println("[PRAGMATRIX-DB] Loaded fallback configuration from local file: db.local.properties");
                return props;
            } catch (IOException ignored) {}
        }

        // 3. Try db.properties from ClassLoader
        try (InputStream is = DBConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (is != null) {
                props.load(is);
                System.out.println("[PRAGMATRIX-DB] Loaded fallback configuration from classpath: db.properties");
            }
        } catch (IOException ignored) {}

        return props;
    }

    private static String getEnvOrProp(String envKey, String propKey, Properties props) {
        return getEnvOrProp(envKey, propKey, props, null);
    }

    private static String getEnvOrProp(String envKey, String propKey, Properties props, String defaultValue) {
        String envVal = System.getenv(envKey);
        if (envVal != null && !envVal.trim().isEmpty()) {
            return envVal.trim();
        }
        if (props != null) {
            String propVal = props.getProperty(envKey);
            if (propVal == null || propVal.trim().isEmpty()) {
                propVal = props.getProperty(propKey);
            }
            if (propVal != null && !propVal.trim().isEmpty()) {
                return propVal.trim();
            }
        }
        return defaultValue;
    }

    private static int parsePositiveInt(String val, int defaultVal) {
        if (val == null || val.trim().isEmpty()) return defaultVal;
        try {
            int parsed = Integer.parseInt(val.trim());
            return parsed > 0 ? parsed : defaultVal;
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }

    private static long parsePositiveLong(String val, long defaultVal) {
        if (val == null || val.trim().isEmpty()) return defaultVal;
        try {
            long parsed = Long.parseLong(val.trim());
            return parsed > 0 ? parsed : defaultVal;
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }

    /**
     * Get a pooled Connection. Automatically initialises the pool if not already running.
     */
    public static Connection getConnection() throws SQLException {
        if (dataSource == null || dataSource.isClosed()) {
            init();
        }
        return dataSource.getConnection();
    }

    /**
     * Close the connection pool on application shutdown.
     */
    public static void close() {
        synchronized (LOCK) {
            if (dataSource != null && !dataSource.isClosed()) {
                dataSource.close();
                dataSource = null;
                System.out.println("[PRAGMATRIX-DB] HikariCP Connection Pool closed.");
            }
        }
    }
}
