package com.medgenome.common.http.interceptor;

import com.medgenome.auth.security.JwtTokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InterceptorConfig {
    @Bean
    public SecurityTokenInterceptor securityTokenInterceptor(
            InterceptorProperties properties,
            JwtTokenProvider tokenProvider) {
        return new SecurityTokenInterceptor(properties, tokenProvider);
    }
}

