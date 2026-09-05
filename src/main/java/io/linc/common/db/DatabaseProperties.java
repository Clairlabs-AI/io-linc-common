package io.linc.common.db;

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

        public int getPriority() {
            return priority;
        }

        public void setPriority(int priority) {
            this.priority = priority;
        }

        public String getJdbcUrl() {
            return jdbcUrl;
        }

        public void setJdbcUrl(String jdbcUrl) {
            this.jdbcUrl = jdbcUrl;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getDriverClassName() {
            return driverClassName;
        }

        public void setDriverClassName(String driverClassName) {
            this.driverClassName = driverClassName;
        }

        public int getMaxPoolSize() {
            return maxPoolSize;
        }

        public void setMaxPoolSize(int maxPoolSize) {
            this.maxPoolSize = maxPoolSize;
        }

        public int getMinIdle() {
            return minIdle;
        }

        public void setMinIdle(int minIdle) {
            this.minIdle = minIdle;
        }

        public long getIdleTimeoutMs() {
            return idleTimeoutMs;
        }

        public void setIdleTimeoutMs(long idleTimeoutMs) {
            this.idleTimeoutMs = idleTimeoutMs;
        }

        public long getMaxLifetimeMs() {
            return maxLifetimeMs;
        }

        public void setMaxLifetimeMs(long maxLifetimeMs) {
            this.maxLifetimeMs = maxLifetimeMs;
        }

        public long getConnectionTimeoutMs() {
            return connectionTimeoutMs;
        }

        public void setConnectionTimeoutMs(long connectionTimeoutMs) {
            this.connectionTimeoutMs = connectionTimeoutMs;
        }

        public Map<String, String> getAdditionalProperties() {
            return additionalProperties;
        }

        public void setAdditionalProperties(Map<String, String> additionalProperties) {
            this.additionalProperties = additionalProperties;
        }
    }
}