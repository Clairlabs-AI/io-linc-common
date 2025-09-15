package com.medgenome.common.http.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Standardized error response format for REST APIs.
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    /**
     * The timestamp when the error occurred.
     */
    private LocalDateTime timestamp;
    
    /**
     * The HTTP status code.
     */
    private int status;
    
    /**
     * The HTTP status description.
     */
    private String error;
    
    /**
     * A human-readable error message.
     */
    private String message;
    
    /**
     * Field-specific validation errors (optional).
     */
    private Map<String, String> validationErrors;
    
    /**
     * The path of the request that caused the error.
     */
    private String path;
    
    /**
     * A unique identifier for this error instance (optional).
     */
    private String errorId;
    
    /**
     * Additional details about the error (optional).
     */
    private Object details;
}