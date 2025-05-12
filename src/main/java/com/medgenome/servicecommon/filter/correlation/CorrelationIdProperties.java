package com.medgenome.servicecommon.filter.correlation;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for correlation ID.
 */
@Setter
@Getter
@ConfigurationProperties(prefix = "app.correlation")
public class CorrelationIdProperties {

    private boolean enabled = true;
    

    private String headerName = "X-Correlation-ID";
    

    private boolean generateIfMissing = true;
    
    private boolean includeInResponse = true;

}