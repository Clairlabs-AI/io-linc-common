package com.medgenome.common.config;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.PropertySource;

/**
 * Custom PropertySource implementing priority-based override logic.
 * Properties with higher priority override those with lower priority.
 */
@Slf4j
public class PriorityPropertySource extends PropertySource<PriorityConfigurationProperties> {

    private final PriorityConfigurationProperties properties;

    public PriorityPropertySource(String name, PriorityConfigurationProperties properties) {
        super(name, properties);
        this.properties = properties;
        log.info("Created priority property source: {}", name);
    }

    @Override
    public Object getProperty(@NonNull String name) {
        if (!properties.isEnabled()) {
            return null;
        }
        
        return properties.getProperty(name, null);
    }
}