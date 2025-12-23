package com.medgenome.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.env.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@ConfigurationProperties(prefix = "common.config")
public class PriorityConfigurationProperties {

    private static final Logger log = LoggerFactory.getLogger(PriorityConfigurationProperties.class);

    private transient Environment environment;

    private boolean enabled = true;
    private Map<String, Integer> propertyFiles = new HashMap<>();
    private Map<String, PriorityProperty> properties = new HashMap<>();
    private int defaultPriority = 100;

    @PostConstruct
    public void init() {
        if (!enabled) {
            log.info("Priority configuration handling is disabled");
            return;
        }

        // Logging only safe values
        log.info("Initializing priority configuration: {} property files loaded", propertyFiles.size());

        propertyFiles.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .forEach(entry -> {
                    log.debug("Loading property file: {} with priority: {}", entry.getKey(), entry.getValue());
                });
    }

    public String getProperty(String key, String defaultValue) {
        if (!enabled) {
            return environment != null ? environment.getProperty(key, defaultValue) : defaultValue;
        }

        if (properties.containsKey(key)) {
            return properties.get(key).value();
        }

        return environment != null ? environment.getProperty(key, defaultValue) : defaultValue;
    }

    public void setProperty(String key, String value, int priority) {
        if (!enabled) {
            log.warn("Priority configuration is disabled, property will not be set: {}", key);
            return;
        }

        PriorityProperty existing = properties.get(key);
        if (existing == null || existing.priority() <= priority) {
            properties.put(key, new PriorityProperty(value, priority));
            log.debug("Set property: {} with value: {} and priority: {}", key, value, priority);
        } else {
            log.debug("Ignored property: {} with value: {} due to lower priority: {} < {}",
                    key, value, priority, existing.priority());
        }
    }

    public void setProperty(String key, String value) {
        setProperty(key, value, defaultPriority);
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Map<String, Integer> getPropertyFiles() {
        return propertyFiles;
    }

    public void setPropertyFiles(Map<String, Integer> propertyFiles) {
        this.propertyFiles = propertyFiles;
    }

    public Map<String, PriorityProperty> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, PriorityProperty> properties) {
        this.properties = properties;
    }

    public int getDefaultPriority() {
        return defaultPriority;
    }

    public void setDefaultPriority(int defaultPriority) {
        this.defaultPriority = defaultPriority;
    }

    public static class PriorityProperty {
        private final String value;
        private final int priority;

        public PriorityProperty(String value, int priority) {
            this.value = value;
            this.priority = priority;
        }

        public String value() {
            return value;
        }

        public int priority() {
            return priority;
        }
    }
}
