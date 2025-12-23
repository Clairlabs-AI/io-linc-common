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



    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Map<String, String> getValidationErrors() {
        return validationErrors;
    }

    public void setValidationErrors(Map<String, String> validationErrors) {
        this.validationErrors = validationErrors;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getErrorId() {
        return errorId;
    }

    public void setErrorId(String errorId) {
        this.errorId = errorId;
    }

    public Object getDetails() {
        return details;
    }

    public void setDetails(Object details) {
        this.details = details;
    }

    public ErrorResponse(LocalDateTime timestamp, int status, String error, String message, Map<String, String> validationErrors, String path, String errorId, Object details) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.message = message;
        this.validationErrors = validationErrors;
        this.path = path;
        this.errorId = errorId;
        this.details = details;
    }

    public ErrorResponse() {
    }
}