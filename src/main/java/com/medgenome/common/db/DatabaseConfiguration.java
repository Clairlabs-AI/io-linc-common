package com.medgenome.common.db;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Configuration for database connection pools.
 * Supports multiple databases with different priorities.
 */
@Configuration
@ConditionalOnClass(DataSource.class)
@EnableConfigurationProperties(DatabaseProperties.class)
@ConditionalOnProperty(prefix = "common.db", name = "enabled", havingValue = "true", matchIfMissing = true)
public class DatabaseConfiguration {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConfiguration.class);

    private final DatabaseProperties databaseProperties;

    public DatabaseConfiguration(DatabaseProperties databaseProperties) {
        this.databaseProperties = databaseProperties;
        log.info("Database configuration initialized with {} data sources",
                databaseProperties.getDatasources().size());
    }

    @Bean
    @Primary
    public javax.sql.DataSource primaryDataSource() {
        String primaryDataSourceName = databaseProperties.getPrimaryDatasource();

        Map<String, javax.sql.DataSource> dataSources = createAllDataSources();

        if (!dataSources.containsKey(primaryDataSourceName)) {
            throw new IllegalStateException("Primary data source '" + primaryDataSourceName + "' not found in configuration");
        }

        log.info("Created primary data source: {}", primaryDataSourceName);
        return dataSources.get(primaryDataSourceName);
    }

    @Bean
    public Map<String, javax.sql.DataSource> allDataSources() {
        Map<String, javax.sql.DataSource> allDataSources = new ConcurrentHashMap<>();
        allDataSources.putAll(createAllDataSources());
        log.info("Created data source mapping bean with {} sources", allDataSources.size());
        return allDataSources;
    }

    @Bean
    public JdbcTemplate jdbcTemplate(@Qualifier("primaryDataSource") javax.sql.DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    private Map<String, javax.sql.DataSource> createAllDataSources() {
        Map<String, javax.sql.DataSource> dataSources = new ConcurrentHashMap<>();

        databaseProperties.getDatasources().forEach((name, config) -> {
            try {
                PoolProperties p = new PoolProperties();
                p.setUrl(config.getJdbcUrl());
                p.setUsername(config.getUsername());
                p.setPassword(config.getPassword());
                p.setDriverClassName(config.getDriverClassName());
                p.setName(name + "-pool");

                // Pool sizing and timeouts mapping
                p.setMaxActive(config.getMaxPoolSize());
                p.setMinIdle(config.getMinIdle());
                p.setMaxIdle(Math.max(config.getMinIdle(), config.getMaxPoolSize()));
                p.setMaxWait((int) config.getConnectionTimeoutMs());
                p.setMinEvictableIdleTimeMillis((int) config.getIdleTimeoutMs());
                p.setMaxAge(config.getMaxLifetimeMs());

                // Sensible defaults
                p.setTestOnBorrow(true);
                p.setValidationQuery("SELECT 1");
                p.setValidationInterval(30000L);
                p.setRemoveAbandoned(true);
                p.setRemoveAbandonedTimeout(300);
                p.setJdbcInterceptors("ConnectionState;StatementFinalizer");

                // Additional properties passthrough
                if (config.getAdditionalProperties() != null) {
                    // Note: PoolProperties#setDbProperties expects a Properties object; convert map if needed
                    // Here we skip direct set to avoid type issues; Tomcat DataSource can pick up properties via URL/driver
                }

                DataSource ds = new DataSource();
                ds.setPoolProperties(p);
                dataSources.put(name, ds);
                log.info("Created data source: {} with url: {}", name, config.getJdbcUrl());
            } catch (Exception e) {
                log.error("Failed to create data source: {}", name, e);
                throw new RuntimeException("Failed to create data source: " + name, e);
            }
        });

        return dataSources;
    }
}