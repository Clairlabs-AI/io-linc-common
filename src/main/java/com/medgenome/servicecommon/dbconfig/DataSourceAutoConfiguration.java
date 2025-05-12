package com.medgenome.servicecommon.dbconfig;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
@ConfigurationProperties(prefix = "spring.datasource")
@Data
public class DataSourceAutoConfiguration {

    // Spring DataSource properties (defaults will be picked up here)
    private String url;
    private String username;
    private String password;
    private String driverClassName;

    // Custom connection pool properties
    private String hikariPoolName;
    private int hikariMaximumPoolSize;
    private int hikariMinimumIdle;


    @Bean
    public DataSource dataSource() {
        // Use DataSourceBuilder to create the DataSource
        DataSourceBuilder<?> dataSourceBuilder = DataSourceBuilder.create();

        // Set Spring DataSource properties
        dataSourceBuilder.url(this.url);
        dataSourceBuilder.username(this.username);
        dataSourceBuilder.password(this.password);
        dataSourceBuilder.driverClassName(this.driverClassName);

        // Optionally, configure HikariCP settings (if they exist)
        if (this.hikariPoolName != null) {
            dataSourceBuilder.type(com.zaxxer.hikari.HikariDataSource.class);
            com.zaxxer.hikari.HikariDataSource hikariDataSource = (com.zaxxer.hikari.HikariDataSource) dataSourceBuilder.build();
            hikariDataSource.setPoolName(this.hikariPoolName);
            hikariDataSource.setMaximumPoolSize(this.hikariMaximumPoolSize);
            hikariDataSource.setMinimumIdle(this.hikariMinimumIdle);
            return hikariDataSource;
        }

        // Return default DataSource if no HikariCP customizations
        return dataSourceBuilder.build();
    }
}


