package io.linc.common.http.interceptor;

import io.linc.auth.security.JwtTokenProvider;
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

