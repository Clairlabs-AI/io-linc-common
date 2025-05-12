package com.medgenome.servicecommon.security.header;

import com.medgenome.servicecommon.exception.MissingRequiredHeaderException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Filter that validates required headers in the request.
 */
public class RequiredHeadersFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(RequiredHeadersFilter.class);
    private final HeaderValidationProperties properties;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Creates a new RequiredHeadersFilter.
     *
     * @param properties header validation properties
     */
    public RequiredHeadersFilter(HeaderValidationProperties properties) {
        this.properties = properties;
    }

    @Override
    protected void doFilterInternal(@NonNull  HttpServletRequest request, @NonNull HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        try {
            validateHeaders(request);
            filterChain.doFilter(request, response);
        } catch (MissingRequiredHeaderException e) {
            logger.warn("Header validation failed: {}", e.getMessage());
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            
            Map<String, Object> errorResponse = Map.of(
                "status", HttpStatus.BAD_REQUEST.value(),
                "error", "Bad Request",
                "message", e.getMessage()
            );
            
            objectMapper.writeValue(response.getWriter(), errorResponse);
        }
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        if (!properties.isEnabled()) {
            return true;
        }
        
        String path = request.getServletPath();
        return properties.getExcludedPaths().stream()
            .anyMatch(p -> pathMatcher.match(p, path));
    }

    private void validateHeaders(@NonNull HttpServletRequest request) {
        // Check required headers
        for (String headerName : properties.getRequiredHeaders()) {
            String headerValue = request.getHeader(headerName);
            if (headerValue == null || headerValue.isEmpty()) {
                throw new MissingRequiredHeaderException("Required header '" + headerName + "' is missing");
            }
        }
        
        // Check validation rules
        for (Map.Entry<String, HeaderValidationProperties.ValidationRule> entry : properties.getValidationRules().entrySet()) {
            String headerName = entry.getKey();
            HeaderValidationProperties.ValidationRule rule = entry.getValue();
            
            String headerValue = request.getHeader(headerName);
            
            // Check if required
            if (rule.isRequired() && (headerValue == null || headerValue.isEmpty())) {
                throw new MissingRequiredHeaderException(
                    rule.getErrorMessage() != null 
                        ? rule.getErrorMessage() 
                        : "Required header '" + headerName + "' is missing"
                );
            }
            
            // Check pattern if header is present and pattern is defined
            if (headerValue != null && !headerValue.isEmpty() && rule.getPattern() != null) {
                if (!Pattern.matches(rule.getPattern(), headerValue)) {
                    throw new MissingRequiredHeaderException(
                        rule.getErrorMessage() != null 
                            ? rule.getErrorMessage() 
                            : "Header '" + headerName + "' has invalid format"
                    );
                }
            }
        }
    }
}