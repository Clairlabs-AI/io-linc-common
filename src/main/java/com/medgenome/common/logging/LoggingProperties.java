package com.medgenome.common.logging;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for logging behavior.
 * Supports console and file logging with customizable patterns.
 */
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
    private String consolePattern = "%d{yyyy-MM-dd HH:mm:ss} [%X{messageId:-N/A}] [%X{correlationId:-N/A}] [%X{appName:-app}] [%X{user:-system}] %-5level %logger{36} - %msg%n";

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

    // Explicit getters/setters to avoid Lombok dependency
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public boolean isConsoleEnabled() {
        return consoleEnabled;
    }

    public void setConsoleEnabled(boolean consoleEnabled) {
        this.consoleEnabled = consoleEnabled;
    }

    public String getConsolePattern() {
        return consolePattern;
    }

    public void setConsolePattern(String consolePattern) {
        this.consolePattern = consolePattern;
    }

    public boolean isFileEnabled() {
        return fileEnabled;
    }

    public void setFileEnabled(boolean fileEnabled) {
        this.fileEnabled = fileEnabled;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFilePattern() {
        return filePattern;
    }

    public void setFilePattern(String filePattern) {
        this.filePattern = filePattern;
    }

    public boolean isRollBySize() {
        return rollBySize;
    }

    public void setRollBySize(boolean rollBySize) {
        this.rollBySize = rollBySize;
    }

    public String getMaxFileSize() {
        return maxFileSize;
    }

    public void setMaxFileSize(String maxFileSize) {
        this.maxFileSize = maxFileSize;
    }

    public int getMaxHistory() {
        return maxHistory;
    }

    public void setMaxHistory(int maxHistory) {
        this.maxHistory = maxHistory;
    }

    public String getTotalSizeCap() {
        return totalSizeCap;
    }

    public void setTotalSizeCap(String totalSizeCap) {
        this.totalSizeCap = totalSizeCap;
    }
}