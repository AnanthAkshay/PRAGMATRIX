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
 * Database Connection & Connection Pool Manager using HikariCP.
 *
 * Configuration precedence:
 * <ol>
 *   <li><b>OS Environment Variables (System.getenv)</b>: DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD</li>
 *   <li><b>JVM System Properties (System.getProperty)</b>: -DDB_HOST=... or -Ddb.host=...</li>
 *   <li><b>Local Properties file</b>: {@code db.local.properties} (classpath or working directory)</li>
 *   <li><b>Default Properties file</b>: {@code db.properties} on classpath</li>
 * </ol>
 */
public class DBConnection {

    private static final int DEFAULT_MAX_POOL_SIZE = 5;
    private static final int DEFAULT_MIN_IDLE = 1;
    private static final long DEFAULT_CONNECTION_TIMEOUT = 30000; // 30s
    private static final long DEFAULT_IDLE_TIMEOUT = 600000;      // 10 min
    private static final long DEFAULT_MAX_LIFETIME = 1800000;     // 30 min

    private static volatile HikariDataSource dataSource;
    private static final Object LOCK = new Object();

    private DBConnection() {}

    /**
     * Initialises the HikariCP DataSource.
     */
    public static void init() {
        if (dataSource == null) {
            synchronized (LOCK) {
                if (dataSource == null) {
                    try {
                        HikariConfig config = createHikariConfig();
                        dataSource = new HikariDataSource(config);
                        System.out.println("[PRAGMATRIX-DB] HikariCP Connection Pool initialised successfully. (Max Pool: "
                                + config.getMaximumPoolSize() + ", Min Idle: " + config.getMinimumIdle() + ")");
                    } catch (Exception e) {
                        System.err.println("[PRAGMATRIX-DB] FATAL: Failed to initialise database connection pool: " + e.getMessage());
                        e.printStackTrace();
                        throw new RuntimeException("Database initialisation failed", e);
                    }
                }
            }
        }
    }

    /**
     * Builds HikariConfig by checking System.getenv() first, then falling back to property files.
     */
    private static HikariConfig createHikariConfig() {
        // Step 1: Probe Environment Variables (System.getenv) & JVM System Properties directly first
        String host = getFromEnvOrSystem("DB_HOST", "db.host", "MYSQL_HOST", "MYSQLHOST");
        String port = getFromEnvOrSystem("DB_PORT", "db.port", "MYSQL_PORT", "MYSQLPORT");
        String name = getFromEnvOrSystem("DB_NAME", "db.name", "MYSQL_DATABASE", "MYSQLDATABASE", "DB_DATABASE");
        String user = getFromEnvOrSystem("DB_USER", "db.user", "DB_USERNAME", "db.username", "MYSQL_USER", "MYSQLUSER");
        String pass = getFromEnvOrSystem("DB_PASSWORD", "db.password", "MYSQL_PASSWORD", "MYSQLPASSWORD", "DB_PASS");

        Properties fileProps = null;
        boolean usingEnvVars = (host != null && !host.trim().isEmpty() && name != null && !name.trim().isEmpty());

        System.out.println("================================================================================");
        System.out.println("[PRAGMATRIX-DB] Initialising Database Configuration...");

        if (usingEnvVars) {
            System.out.println("[PRAGMATRIX-DB] Configuration Source : ENVIRONMENT VARIABLES (System.getenv)");
            System.out.println("[PRAGMATRIX-DB] DB_HOST              : " + host.trim());
            System.out.println("[PRAGMATRIX-DB] DB_PORT              : " + (port != null && !port.trim().isEmpty() ? port.trim() : "3306 (default)"));
            System.out.println("[PRAGMATRIX-DB] DB_NAME              : " + name.trim());
            System.out.println("[PRAGMATRIX-DB] DB_USER              : " + (user != null ? user.trim() : "[NOT SET]"));
            System.out.println("[PRAGMATRIX-DB] DB_PASSWORD          : " + (pass != null && !pass.trim().isEmpty() ? "[SET (length " + pass.trim().length() + ")]" : "[NOT SET]"));
            System.out.println("[PRAGMATRIX-DB] SSL Mode             : REQUIRED (useSSL=true&requireSSL=true&sslMode=REQUIRED)");
        } else {
            System.out.println("[PRAGMATRIX-DB] No DB_HOST / DB_NAME environment variables detected.");
            System.out.println("[PRAGMATRIX-DB] Attempting fallback to properties files (db.local.properties / db.properties)...");
            fileProps = loadFallbackProperties();

            // Check if fallback files defined host/name or a direct db.url
            if (host == null || host.trim().isEmpty()) {
                host = getPropValue(fileProps, "DB_HOST", "db.host");
            }
            if (port == null || port.trim().isEmpty()) {
                port = getPropValue(fileProps, "DB_PORT", "db.port");
            }
            if (name == null || name.trim().isEmpty()) {
                name = getPropValue(fileProps, "DB_NAME", "db.name");
            }
            if (user == null || user.trim().isEmpty()) {
                user = getPropValue(fileProps, "DB_USER", "db.user", "DB_USERNAME", "db.username");
            }
            if (pass == null || pass.trim().isEmpty()) {
                pass = getPropValue(fileProps, "DB_PASSWORD", "db.password");
            }

            if (host != null && !host.trim().isEmpty() && name != null && !name.trim().isEmpty()) {
                System.out.println("[PRAGMATRIX-DB] Configuration Source : PROPERTIES FILE (host/name format)");
                System.out.println("[PRAGMATRIX-DB] DB_HOST              : " + host.trim());
                System.out.println("[PRAGMATRIX-DB] DB_PORT              : " + (port != null && !port.trim().isEmpty() ? port.trim() : "3306 (default)"));
                System.out.println("[PRAGMATRIX-DB] DB_NAME              : " + name.trim());
                System.out.println("[PRAGMATRIX-DB] DB_USER              : " + (user != null ? user.trim() : "[NOT SET]"));
                System.out.println("[PRAGMATRIX-DB] DB_PASSWORD          : " + (pass != null && !pass.trim().isEmpty() ? "[SET (length " + pass.trim().length() + ")]" : "[NOT SET]"));
            } else {
                String dbUrl = fileProps != null ? fileProps.getProperty("db.url") : null;
                if (dbUrl != null && !dbUrl.trim().isEmpty()) {
                    System.out.println("[PRAGMATRIX-DB] Configuration Source : PROPERTIES FILE (db.url format)");
                    System.out.println("[PRAGMATRIX-DB] JDBC URL             : " + dbUrl.trim());
                    System.out.println("[PRAGMATRIX-DB] DB_USER              : " + (fileProps.getProperty("db.username") != null ? fileProps.getProperty("db.username").trim() : "[NOT SET]"));
                } else {
                    System.err.println("[PRAGMATRIX-DB] FATAL: No database configuration found!");
                    throw new IllegalStateException("[PRAGMATRIX-DB] Database connection details not configured! Please set DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD environment variables in Render.");
                }
            }
        }
        System.out.println("================================================================================");

        HikariConfig config = new HikariConfig();
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");

        if (host != null && !host.trim().isEmpty() && name != null && !name.trim().isEmpty()) {
            String resolvedPort = (port != null && !port.trim().isEmpty()) ? port.trim() : "3306";
            String jdbcUrl = String.format(
                    "jdbc:mysql://%s:%s/%s?useSSL=true&requireSSL=true&sslMode=REQUIRED&allowPublicKeyRetrieval=true&characterEncoding=UTF-8",
                    host.trim(),
                    resolvedPort,
                    name.trim()
            );
            config.setJdbcUrl(jdbcUrl);
            config.setUsername(user != null ? user.trim() : "");
            config.setPassword(pass != null ? pass.trim() : "");
        } else if (fileProps != null) {
            String dbUrl = fileProps.getProperty("db.url");
            String dbUser = fileProps.getProperty("db.username", user != null ? user : "");
            String dbPass = fileProps.getProperty("db.password", pass != null ? pass : "");
            config.setJdbcUrl(dbUrl != null ? dbUrl.trim() : "");
            config.setUsername(dbUser != null ? dbUser.trim() : "");
            config.setPassword(dbPass != null ? dbPass.trim() : "");
        }

        // Pool Sizing and Timeouts
        int maxPool = parsePositiveInt(getFromEnvOrSystemOrProps("DB_POOL_MAX_SIZE", "db.pool.maxSize", fileProps), DEFAULT_MAX_POOL_SIZE);
        int minIdle = parsePositiveInt(getFromEnvOrSystemOrProps("DB_POOL_MIN_IDLE", "db.pool.minIdle", fileProps), DEFAULT_MIN_IDLE);
        long connTimeout = parsePositiveLong(getFromEnvOrSystemOrProps("DB_POOL_TIMEOUT", "db.pool.connectionTimeout", fileProps), DEFAULT_CONNECTION_TIMEOUT);
        long idleTimeout = parsePositiveLong(getFromEnvOrSystemOrProps("DB_POOL_IDLE_TIMEOUT", "db.pool.idleTimeout", fileProps), DEFAULT_IDLE_TIMEOUT);
        long maxLifetime = parsePositiveLong(getFromEnvOrSystemOrProps("DB_POOL_MAX_LIFETIME", "db.pool.maxLifetime", fileProps), DEFAULT_MAX_LIFETIME);

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

    private static String getFromEnvOrSystem(String... keys) {
        if (keys == null) return null;
        // 1. Try OS Environment Variables first (System.getenv)
        for (String key : keys) {
            String val = System.getenv(key);
            if (val != null && !val.trim().isEmpty()) {
                return val.trim();
            }
        }
        // 2. Try JVM System Properties (-Dkey=...)
        for (String key : keys) {
            String val = System.getProperty(key);
            if (val != null && !val.trim().isEmpty()) {
                return val.trim();
            }
        }
        return null;
    }

    private static String getPropValue(Properties props, String... keys) {
        if (props == null || keys == null) return null;
        for (String key : keys) {
            String val = props.getProperty(key);
            if (val != null && !val.trim().isEmpty()) {
                return val.trim();
            }
        }
        return null;
    }

    private static String getFromEnvOrSystemOrProps(String envKey, String propKey, Properties props) {
        String val = getFromEnvOrSystem(envKey, propKey);
        if (val != null && !val.trim().isEmpty()) {
            return val;
        }
        if (props != null) {
            val = getPropValue(props, envKey, propKey);
            if (val != null && !val.trim().isEmpty()) {
                return val;
            }
        }
        return null;
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
        if (dataSource != null && !dataSource.isClosed()) {
            synchronized (LOCK) {
                if (dataSource != null && !dataSource.isClosed()) {
                    dataSource.close();
                    System.out.println("[PRAGMATRIX-DB] HikariCP Connection Pool closed.");
                }
            }
        }
    }

    /**
     * Diagnostic helper to check connection pool status.
     */
    public static boolean isPoolActive() {
        return dataSource != null && !dataSource.isClosed() && dataSource.isRunning();
    }
}
