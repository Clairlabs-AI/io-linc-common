package com.medgenome.servicecommon.web.advice;

import com.medgenome.servicecommon.exception.MissingRequiredHeaderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Global exception handler for REST controllers.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);


    /*@ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolation(ConstraintViolationException ex) {
        Set<ConstraintViolation<?>> constraintViolations = ex.getConstraintViolations();
        List<Error> errorList = new ArrayList<>();
        constraintViolationErrorBuild(errorList, constraintViolations);
        model400ErrorResponseResult.setErrors(errorList);
        model400ErrorResponse.setResult(model400ErrorResponseResult);
        return new ResponseEntity<>(model400ErrorResponse, HttpStatus.BAD_REQUEST);
    }

    private void constraintViolationErrorBuild(List<Error> errorList, Set<ConstraintViolation<?>> constraintViolations) {
        if(constraintViolations != null && !constraintViolations.isEmpty()) {
            constraintViolations.stream().filter(distinctByKey(ConstraintViolation::getPropertyPath))
                    .forEach(violation -> {
                        Error error = new Error();
                        error.setCode(APIConstants.INVALID_HEADER_PARAM_CODE);
                        error.setMessage(String.format(APIConstants.INVALID_PARAM_MESSAGE, violation.getPropertyPath()));
                        error.setDetails(String.format(APIConstants.INVALID_HEADER_PARAM_DETAILS, violation.getPropertyPath()));
                        errorList.add(error);
                    });
        }
    }
*/
    private <T> Predicate<T> distinctByKey(Function<? super T, Object> keyExtractor) {
        Map<Object, Boolean> map = new ConcurrentHashMap<>();
        return t -> map.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }


    /**
     * Handles MissingRequiredHeaderException.
     *
     * @param ex the exception
     * @param request the web request
     * @return the error response
     */
    @ExceptionHandler(MissingRequiredHeaderException.class)
    public ResponseEntity<ErrorResponse> handleMissingRequiredHeaderException(
            MissingRequiredHeaderException ex, WebRequest request) {
        logger.warn("Missing required header: {}", ex.getMessage());
        return buildErrorResponse(ex, HttpStatus.BAD_REQUEST, request);
    }

    /**
     * Handles AuthenticationException.
     *
     * @param ex the exception
     * @param request the web request
     * @return the error response
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(
            AuthenticationException ex, WebRequest request) {
        logger.warn("Authentication error: {}", ex.getMessage());
        return buildErrorResponse(ex, HttpStatus.UNAUTHORIZED, request);
    }

    /**
     * Handles AccessDeniedException.
     *
     * @param ex the exception
     * @param request the web request
     * @return the error response
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            AccessDeniedException ex, WebRequest request) {
        logger.warn("Access denied: {}", ex.getMessage());
        return buildErrorResponse(ex, HttpStatus.FORBIDDEN, request);
    }

    /**
     * Handles MethodArgumentNotValidException.
     *
     * @param ex the exception
     * @param request the web request
     * @return the error response
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, WebRequest request) {
        logger.warn("Validation error: {}", ex.getMessage());
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        ErrorResponse errorResponse = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.BAD_REQUEST.value(),
            "Validation error",
            "Validation failed for request parameters",
            request.getDescription(false),
            errors
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles all other exceptions.
     *
     * @param ex the exception
     * @param request the web request
     * @return the error response
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(
            Exception ex, WebRequest request) {
        logger.error("Unhandled exception", ex);
        return buildErrorResponse(ex, HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(
            Exception ex, HttpStatus status, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
            LocalDateTime.now(),
            status.value(),
            status.getReasonPhrase(),
            ex.getMessage(),
            request.getDescription(false),
            null
        );
        
        return new ResponseEntity<>(errorResponse, status);
    }

    /**
         * Error response class.
         */
    public record ErrorResponse(LocalDateTime timestamp, int status, String error, String message, String path,
                                    Map<String, String> validationErrors) {

    }

}