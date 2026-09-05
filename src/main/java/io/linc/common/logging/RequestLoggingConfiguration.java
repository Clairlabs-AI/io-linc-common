package io.linc.common.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;
import org.springframework.web.servlet.DispatcherServlet;

/**
 * Configuration for HTTP request and response logging.
 * Only activated for web applications with DispatcherServlet present.
 */
@Configuration
@ConditionalOnWebApplication
@ConditionalOnClass(DispatcherServlet.class)
@EnableConfigurationProperties(RequestLoggingProperties.class)
@ConditionalOnProperty(prefix = "common.logging.request", name = "enabled", havingValue = "true", matchIfMissing = true)
public class RequestLoggingConfiguration {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingConfiguration.class);

    private final RequestLoggingProperties properties;

    public RequestLoggingConfiguration(RequestLoggingProperties properties) {
        this.properties = properties;
        log.info("Request logging configuration initialized");
    }

    @Bean
    public CommonsRequestLoggingFilter requestLoggingFilter() {
        CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter();
        filter.setIncludeQueryString(properties.isIncludeQueryString());
        filter.setIncludePayload(properties.isIncludePayload());
        filter.setMaxPayloadLength(properties.getMaxPayloadLength());
        filter.setIncludeHeaders(properties.isIncludeHeaders());
        filter.setIncludeClientInfo(properties.isIncludeClientInfo());
        filter.setBeforeMessagePrefix(properties.getBeforeMessagePrefix());
        filter.setBeforeMessageSuffix(properties.getBeforeMessageSuffix());
        filter.setAfterMessagePrefix(properties.getAfterMessagePrefix());
        filter.setAfterMessageSuffix(properties.getAfterMessageSuffix());
        log.info("Configured request logging filter with maxPayloadLength: {}", properties.getMaxPayloadLength());
        return filter;
    }
}