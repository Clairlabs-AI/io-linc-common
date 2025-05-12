package com.medgenome.servicecommon.security.header;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Configuration properties for header validation.
 */
@ConfigurationProperties(prefix = "app.security.header-validation")
public class HeaderValidationProperties {
    /**
     * Enable or disable header validation.
     */
    private boolean enabled = true;
    
    /**
     * List of required headers.
     */
    private List<String> requiredHeaders = new ArrayList<>();
    
    /**
     * Headers with specific validation rules.
     */
    private Map<String, ValidationRule> validationRules = new HashMap<>();
    
    /**
     * Paths to exclude from header validation.
     */
    private List<String> excludedPaths = List.of(
        "/actuator/**", 
        "/swagger-ui/**", 
        "/v3/api-docs/**", 
        "/api/auth/**"
    );

    /**
     * Gets whether header validation is enabled.
     *
     * @return true if header validation is enabled, false otherwise
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets whether header validation is enabled.
     *
     * @param enabled true to enable header validation, false to disable
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Gets the list of required headers.
     *
     * @return the list of required headers
     */
    public List<String> getRequiredHeaders() {
        return requiredHeaders;
    }

    /**
     * Sets the list of required headers.
     *
     * @param requiredHeaders the list of required headers to set
     */
    public void setRequiredHeaders(List<String> requiredHeaders) {
        this.requiredHeaders = requiredHeaders;
    }

    /**
     * Gets the header validation rules.
     *
     * @return the header validation rules
     */
    public Map<String, ValidationRule> getValidationRules() {
        return validationRules;
    }

    /**
     * Sets the header validation rules.
     *
     * @param validationRules the header validation rules to set
     */
    public void setValidationRules(Map<String, ValidationRule> validationRules) {
        this.validationRules = validationRules;
    }

    /**
     * Gets the paths excluded from header validation.
     *
     * @return the excluded paths
     */
    public List<String> getExcludedPaths() {
        return excludedPaths;
    }

    /**
     * Sets the paths excluded from header validation.
     *
     * @param excludedPaths the excluded paths to set
     */
    public void setExcludedPaths(List<String> excludedPaths) {
        this.excludedPaths = excludedPaths;
    }

    /**
     * Class representing a validation rule for a header.
     */
    public static class ValidationRule {
        private String pattern;
        private boolean required = false;
        private String errorMessage;

        /**
         * Gets the validation pattern.
         *
         * @return the validation pattern
         */
        public String getPattern() {
            return pattern;
        }

        /**
         * Sets the validation pattern.
         *
         * @param pattern the validation pattern to set
         */
        public void setPattern(String pattern) {
            this.pattern = pattern;
        }

        /**
         * Gets whether the header is required.
         *
         * @return true if the header is required, false otherwise
         */
        public boolean isRequired() {
            return required;
        }

        /**
         * Sets whether the header is required.
         *
         * @param required true if the header is required, false otherwise
         */
        public void setRequired(boolean required) {
            this.required = required;
        }

        /**
         * Gets the error message for validation failure.
         *
         * @return the error message
         */
        public String getErrorMessage() {
            return errorMessage;
        }

        /**
         * Sets the error message for validation failure.
         *
         * @param errorMessage the error message to set
         */
        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }
    }
}