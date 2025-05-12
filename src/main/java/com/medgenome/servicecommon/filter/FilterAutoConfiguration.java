package com.medgenome.servicecommon.filter;

import com.medgenome.servicecommon.filter.correlation.CorrelationIdFilter;
import com.medgenome.servicecommon.filter.correlation.CorrelationIdProperties;
import com.medgenome.servicecommon.filter.request.RequestContextFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * Auto-configuration for filters.
 */
@Configuration
@EnableConfigurationProperties(CorrelationIdProperties.class)
public class FilterAutoConfiguration {

    /**
     * Creates a RequestContextFilter bean registration.
     *
     * @return the FilterRegistrationBean for RequestContextFilter
     */
    @Bean
    public FilterRegistrationBean<RequestContextFilter> requestContextFilter() {
        FilterRegistrationBean<RequestContextFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new RequestContextFilter());
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registrationBean;
    }

    /**
     * Creates a CorrelationIdFilter bean registration.
     *
     * @param properties correlation ID properties
     * @return the FilterRegistrationBean for CorrelationIdFilter
     */
    @Bean
    @ConditionalOnProperty(name = "app.correlation.enabled", havingValue = "true", matchIfMissing = true)
    public FilterRegistrationBean<CorrelationIdFilter> correlationIdFilter(CorrelationIdProperties properties) {
        FilterRegistrationBean<CorrelationIdFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new CorrelationIdFilter(properties));
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
        return registrationBean;
    }
}