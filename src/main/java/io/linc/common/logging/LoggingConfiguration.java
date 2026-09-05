package io.linc.common.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import ch.qos.logback.core.util.FileSize;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

/**
 * Configures logging behavior based on application properties.
 * Supports both console and file-based logging with configurable patterns.
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(LoggingProperties.class)
@ConditionalOnProperty(prefix = "common.logging", name = "enabled", havingValue = "true", matchIfMissing = true)
public class LoggingConfiguration {

    private final LoggingProperties loggingProperties;

    @Autowired
    public LoggingConfiguration(LoggingProperties loggingProperties) {
        this.loggingProperties = loggingProperties;
    }

    @PostConstruct
    public void configureLogging() {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger rootLogger = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        
        // Reset any existing appenders
        rootLogger.detachAndStopAllAppenders();
        
        // Set the root logger level
        rootLogger.setLevel(Level.valueOf(loggingProperties.getLevel().toUpperCase()));
        
        // Configure console logging if enabled
        if (loggingProperties.isConsoleEnabled()) {
            rootLogger.addAppender(createConsoleAppender(loggerContext));
        }
        
        // Configure file logging if enabled
        if (loggingProperties.isFileEnabled()) {
            rootLogger.addAppender(createFileAppender(loggerContext));
        }
        
        log.info("Logging configuration applied with level: {}", loggingProperties.getLevel());
    }
    
    private ConsoleAppender<ILoggingEvent> createConsoleAppender(LoggerContext context) {
        ConsoleAppender<ILoggingEvent> appender = new ConsoleAppender<>();
        appender.setContext(context);
        appender.setName("CONSOLE");
        
        PatternLayout layout = new PatternLayout();
        layout.setContext(context);
        layout.setPattern(loggingProperties.getConsolePattern());
        layout.start();
        
        appender.setLayout(layout);
        appender.start();
        
        return appender;
    }
    
    private RollingFileAppender<ILoggingEvent> createFileAppender(LoggerContext context) {
        RollingFileAppender<ILoggingEvent> appender = new RollingFileAppender<>();
        appender.setContext(context);
        appender.setName("FILE");
        appender.setFile(loggingProperties.getFilePath());
        
        PatternLayout layout = new PatternLayout();
        layout.setContext(context);
        layout.setPattern(loggingProperties.getFilePattern());
        layout.start();
        
        appender.setLayout(layout);
        
        // Configure rolling policy based on properties
        if (loggingProperties.isRollBySize()) {
            SizeAndTimeBasedRollingPolicy<ILoggingEvent> rollingPolicy = new SizeAndTimeBasedRollingPolicy<>();
            rollingPolicy.setContext(context);
            rollingPolicy.setParent(appender);
            rollingPolicy.setFileNamePattern(loggingProperties.getFilePath() + ".%d{yyyy-MM-dd}.%i");
            rollingPolicy.setMaxFileSize(FileSize.valueOf(loggingProperties.getMaxFileSize()));
            rollingPolicy.setMaxHistory(loggingProperties.getMaxHistory());
            rollingPolicy.setTotalSizeCap(FileSize.valueOf(loggingProperties.getTotalSizeCap()));
            rollingPolicy.start();
            
            appender.setRollingPolicy(rollingPolicy);
        } else {
            TimeBasedRollingPolicy<ILoggingEvent> rollingPolicy = new TimeBasedRollingPolicy<>();
            rollingPolicy.setContext(context);
            rollingPolicy.setParent(appender);
            rollingPolicy.setFileNamePattern(loggingProperties.getFilePath() + ".%d{yyyy-MM-dd}");
            rollingPolicy.setMaxHistory(loggingProperties.getMaxHistory());
            rollingPolicy.start();
            
            appender.setRollingPolicy(rollingPolicy);
        }
        
        appender.start();
        return appender;
    }
}