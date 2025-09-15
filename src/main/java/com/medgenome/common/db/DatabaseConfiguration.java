package com.medgenome.common.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Configuration for database connection pools.
 * Supports multiple databases with different priorities.
 */
@Slf4j
@Configuration
@ConditionalOnClass(HikariDataSource.class)
@EnableConfigurationProperties(DatabaseProperties.class)
@ConditionalOnProperty(prefix = "common.db", name = "enabled", havingValue = "true", matchIfMissing = true)
public class DatabaseConfiguration {

    private final DatabaseProperties databaseProperties;

    public DatabaseConfiguration(DatabaseProperties databaseProperties) {
        this.databaseProperties = databaseProperties;
        log.info("Database configuration initialized with {} data sources", 
                databaseProperties.getDatasources().size());
    }

    @Bean
    @Primary
    public DataSource primaryDataSource() {
        String primaryDataSourceName = databaseProperties.getPrimaryDatasource();
        
        Map<String, HikariDataSource> dataSources = createAllDataSources();
        
        if (!dataSources.containsKey(primaryDataSourceName)) {
            throw new IllegalStateException("Primary data source '" + primaryDataSourceName + "' not found in configuration");
        }
        
        log.info("Created primary data source: {}", primaryDataSourceName);
        return dataSources.get(primaryDataSourceName);
    }
    
    @Bean
    public Map<String, DataSource> allDataSources() {
        Map<String, DataSource> allDataSources = new ConcurrentHashMap<>();
        allDataSources.putAll(createAllDataSources());
        log.info("Created data source mapping bean with {} sources", allDataSources.size());
        return allDataSources;
    }
    
    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
    
    private Map<String, HikariDataSource> createAllDataSources() {
        Map<String, HikariDataSource> dataSources = new ConcurrentHashMap<>();
        
        databaseProperties.getDatasources().forEach((name, config) -> {
            HikariConfig hikariConfig = new HikariConfig();
            hikariConfig.setJdbcUrl(config.getJdbcUrl());
            hikariConfig.setUsername(config.getUsername());
            hikariConfig.setPassword(config.getPassword());
            hikariConfig.setDriverClassName(config.getDriverClassName());
            hikariConfig.setPoolName(name + "-pool");
            
            // Connection pool settings
            hikariConfig.setMaximumPoolSize(config.getMaxPoolSize());
            hikariConfig.setMinimumIdle(config.getMinIdle());
            hikariConfig.setIdleTimeout(config.getIdleTimeoutMs());
            hikariConfig.setMaxLifetime(config.getMaxLifetimeMs());
            hikariConfig.setConnectionTimeout(config.getConnectionTimeoutMs());
            
            // Additional properties
            if (config.getAdditionalProperties() != null) {
                config.getAdditionalProperties().forEach(hikariConfig::addDataSourceProperty);
            }
            
            try {
                HikariDataSource dataSource = new HikariDataSource(hikariConfig);
                dataSources.put(name, dataSource);
                log.info("Created data source: {} with url: {}", name, config.getJdbcUrl());
            } catch (Exception e) {
                log.error("Failed to create data source: {}", name, e);
                throw new RuntimeException("Failed to create data source: " + name, e);
            }
        });
        
        return dataSources;
    }
}