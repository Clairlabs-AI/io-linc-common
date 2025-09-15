package com.medgenome.common.logging;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for logging behavior.
 * Supports console and file logging with customizable patterns.
 */
@Data
@ConfigurationProperties(prefix = "common.logging")
public class LoggingProperties {

    /**
     * Enable/disable logging configuration.
     */
    private boolean enabled = true;

    /**
     * Default logging level.
     */
    private String level = "INFO";

    /**
     * Console logging configuration.
     */
    private boolean consoleEnabled = true;
    private String consolePattern = "%d{yyyy-MM-dd HH:mm:ss} [%X{messageId:-N/A}] [%X{correlationId:-N/A}] [%X{appName:-app}] [%X{user:-system}] %-5level %logger{36} - %msg%n>";

    /**
     * File logging configuration.
     */
    private boolean fileEnabled = false;
    private String filePath = "logs/application.log";
    private String filePattern = "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n";
    
    /**
     * Rolling policy configuration.
     */
    private boolean rollBySize = true;
    private String maxFileSize = "10MB";
    private int maxHistory = 7;
    private String totalSizeCap = "1GB";
}