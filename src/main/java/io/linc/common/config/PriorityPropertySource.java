package io.linc.common.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.NonNull;
import org.springframework.core.env.PropertySource;

/**
 * Custom PropertySource implementing priority-based override logic.
 * Properties with higher priority override those with lower priority.
 */
public class PriorityPropertySource extends PropertySource<PriorityConfigurationProperties> {

    private static final Logger log = LoggerFactory.getLogger(PriorityPropertySource.class);

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