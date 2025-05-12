package com.medgenome.servicecommon.web;

import com.medgenome.servicecommon.web.advice.GlobalExceptionHandler;
import com.medgenome.servicecommon.web.interceptor.WebMvcConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Auto-configuration for web components.
 */
@Configuration
public class WebAutoConfiguration {

    /**
     * Creates a GlobalExceptionHandler bean.
     *
     * @return the GlobalExceptionHandler
     */
    @Bean
    public GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }

    /**
     * Creates a WebMvcConfig bean.
     *
     * @return the WebMvcConfig
     */
    @Bean
    public WebMvcConfigurer webMvcConfigurer() {
        return new WebMvcConfig();
    }
}