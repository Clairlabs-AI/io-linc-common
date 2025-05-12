package com.medgenome.servicecommon.exception;

/**
 * Exception thrown when a required header is missing or invalid.
 */
public class MissingRequiredHeaderException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new MissingRequiredHeaderException with the specified message.
     *
     * @param message the exception message
     */
    public MissingRequiredHeaderException(String message) {
        super(message);
    }
}