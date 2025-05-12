package com.medgenome.servicecommon;

import com.medgenome.servicecommon.filter.FilterAutoConfiguration;
import com.medgenome.servicecommon.logging.LoggingAutoConfiguration;
import com.medgenome.servicecommon.logging.config.JpaAuditingAutoConfiguration;
import com.medgenome.servicecommon.security.SecurityAutoConfiguration;
import com.medgenome.servicecommon.web.WebAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;

/**
 * Main entry point for Service Common library auto-configuration.
 * Imports all necessary configurations.
 */
@Configuration
@Import({
        FilterAutoConfiguration.class,
        LoggingAutoConfiguration.class,
        JpaAuditingAutoConfiguration.class,
        SecurityAutoConfiguration.class,
        WebAutoConfiguration.class
})
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class ServiceCommonAutoConfiguration {

}