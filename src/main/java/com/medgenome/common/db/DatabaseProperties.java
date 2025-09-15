package com.medgenome.common.db;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration properties for database connections.
 * Supports multiple datasources with different configurations.
 */
@Data
@ConfigurationProperties(prefix = "common.db")
public class DatabaseProperties {

    /**
     * Enable/disable database configuration.
     */
    private boolean enabled = true;

    /**
     * The name of the primary datasource to use as default.
     */
    private String primaryDatasource = "primary";

    /**
     * Map of named datasource configurations.
     */
    private Map<String, DatasourceConfig> datasources = new HashMap<>();

    /**
     * Configuration for a specific datasource.
     */
    @Data
    public static class DatasourceConfig {
        /**
         * Priority of this datasource (higher = more important).
         */
        private int priority = 0;
        
        /**
         * JDBC URL for the database.
         */
        private String jdbcUrl;
        
        /**
         * Database username.
         */
        private String username;
        
        /**
         * Database password.
         */
        private String password;
        
        /**
         * JDBC driver class name.
         */
        private String driverClassName;
        
        /**
         * Maximum number of connections in the pool.
         */
        private int maxPoolSize = 10;
        
        /**
         * Minimum number of idle connections in the pool.
         */
        private int minIdle = 5;
        
        /**
         * Maximum time a connection can be idle before being removed (in milliseconds).
         */
        private long idleTimeoutMs = 600000; // 10 minutes
        
        /**
         * Maximum lifetime of a connection in the pool (in milliseconds).
         */
        private long maxLifetimeMs = 1800000; // 30 minutes
        
        /**
         * Maximum time to wait for a connection from the pool (in milliseconds).
         */
        private long connectionTimeoutMs = 30000; // 30 seconds
        
        /**
         * Additional JDBC properties to set on connections.
         */
        private Map<String, String> additionalProperties;
    }
}