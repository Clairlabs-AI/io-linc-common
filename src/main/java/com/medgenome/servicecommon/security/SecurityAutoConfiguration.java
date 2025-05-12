package com.medgenome.servicecommon.security;

import com.medgenome.servicecommon.security.header.HeaderValidationProperties;
import com.medgenome.servicecommon.security.header.RequiredHeadersFilter;
import com.medgenome.servicecommon.security.jwt.JwtAuthenticationFilter;
import com.medgenome.servicecommon.security.jwt.JwtProperties;
import com.medgenome.servicecommon.security.jwt.JwtTokenProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Security configuration for the application.
 * Sets up JWT authentication and security filters.
 */
@Configuration
@EnableWebSecurity
@EnableConfigurationProperties({JwtProperties.class, HeaderValidationProperties.class})
public class SecurityAutoConfiguration {

    /**
     * Creates a JwtTokenProvider bean.
     *
     * @param jwtProperties JWT configuration properties
     * @return the JwtTokenProvider
     */
    @Bean
    public JwtTokenProvider jwtTokenProvider(JwtProperties jwtProperties) {
        return new JwtTokenProvider(jwtProperties);
    }

    /**
     * Creates a JwtAuthenticationFilter bean.
     *
     * @param jwtTokenProvider JWT token provider
     * @param jwtProperties JWT configuration properties
     * @return the JwtAuthenticationFilter
     */
    @Bean
    @ConditionalOnProperty(name = "app.security.jwt.enabled", havingValue = "true", matchIfMissing = true)
    public JwtAuthenticationFilter jwtAuthenticationFilter(
            JwtTokenProvider jwtTokenProvider,
            JwtProperties jwtProperties) {
        return new JwtAuthenticationFilter(jwtTokenProvider, jwtProperties);
    }

    /**
     * Creates a RequiredHeadersFilter bean.
     *
     * @param properties header validation properties
     * @return the RequiredHeadersFilter
     */
    @Bean
    @ConditionalOnProperty(name = "app.security.header-validation.enabled", havingValue = "true", matchIfMissing = true)
    public RequiredHeadersFilter requiredHeadersFilter(HeaderValidationProperties properties) {
        return new RequiredHeadersFilter(properties);
    }

    /**
     * Configures the SecurityFilterChain.
     *
     * @param http HttpSecurity to configure
     * @param jwtAuthenticationFilter JWT authentication filter
     * @param requiredHeadersFilter headers validation filter
     * @return the configured SecurityFilterChain
     * @throws Exception if an error occurs during configuration
     */
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            RequiredHeadersFilter requiredHeadersFilter) throws Exception {
        return http
            .csrf(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/**", "/api/auth/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .anyRequest().authenticated()
            )
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(requiredHeadersFilter, JwtAuthenticationFilter.class)
            .build();
    }
    
    /**
     * Creates a CorsConfigurationSource bean.
     *
     * @return the CorsConfigurationSource
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Authorization", "Content-Type"));
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}