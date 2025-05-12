package com.medgenome.servicecommon.context;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Thread-local storage for request-scoped data.
 * Stores user information, correlation IDs, and other request context.
 */
public final class RequestContext {
    private static final ThreadLocal<RequestContext> CURRENT = ThreadLocal.withInitial(RequestContext::new);
    
    private String userId;
    private String username;
    private String messageId;
    private String correlationId;
    private String sessionId;
    private final Map<String, Object> attributes = new HashMap<>();

    private RequestContext() {
        // Initialize with default correlation ID
        this.correlationId = UUID.randomUUID().toString();
    }

    /**
     * Gets the current request context from ThreadLocal.
     *
     * @return the current request context instance
     */
    public static RequestContext current() {
        return CURRENT.get();
    }

    /**
     * Clears the current request context from ThreadLocal.
     * Should be called at the end of request processing.
     */
    public static void clear() {
        CURRENT.remove();
    }

    /**
     * Gets the user ID from the context.
     *
     * @return the user ID
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Sets the user ID in the context.
     *
     * @param userId the user ID to set
     * @return this context instance for method chaining
     */
    public RequestContext setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    /**
     * Gets the username from the context.
     *
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username in the context.
     *
     * @param username the username to set
     * @return this context instance for method chaining
     */
    public RequestContext setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getCorrelationId() {
        if (correlationId == null) {
            throw new IllegalStateException("Correlation ID is required but not set");
        }
        return correlationId;
    }

    /**
     * Sets the correlation ID in the context.
     *
     * @param correlationId the correlation ID to set
     * @return this context instance for method chaining
     * @throws IllegalArgumentException if correlationId is null or empty
     */
    public RequestContext setCorrelationId(String correlationId) {
        if (correlationId == null || correlationId.trim().isEmpty()) {
            throw new IllegalArgumentException("Correlation ID cannot be null or empty");
        }
        this.correlationId = correlationId;
        return this;
    }

    public String getSessionId() {
        if (sessionId == null) {
            throw new IllegalStateException("Session ID is required but not set");
        }
        return sessionId;
    }

    /**
     * Sets the session ID in the context.
     *
     * @param sessionId the session ID to set
     * @return this context instance for method chaining
     * @throws IllegalArgumentException if sessionId is null or empty
     */
    public RequestContext setSessionId(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            throw new IllegalArgumentException("Session ID cannot be null or empty");
        }
        this.sessionId = sessionId;
        return this;
    }

    /**
     * Stores a custom attribute in the context.
     *
     * @param key the attribute key
     * @param value the attribute value
     * @return this context instance for method chaining
     */
    public RequestContext setAttribute(String key, Object value) {
        attributes.put(key, value);
        return this;
    }


    /**
     * Gets the message ID from the context.
     *
     * @return the message ID
     * @throws IllegalStateException if message ID is not set
     */
    public String getMessageId() {
        if (messageId == null) {
            throw new IllegalStateException("Message ID is required but not set");
        }
        return messageId;
    }

    /**
     * Sets the message ID in the context.
     *
     * @param messageId the message ID to set
     * @return this context instance for method chaining
     * @throws IllegalArgumentException if messageId is null or empty
     */
    public RequestContext setMessageId(String messageId) {
        if (messageId == null || messageId.trim().isEmpty()) {
            throw new IllegalArgumentException("Message ID cannot be null or empty");
        }
        this.messageId = messageId;
        return this;
    }

    /**
     * Retrieves a custom attribute from the context.
     *
     * @param key the attribute key
     * @param <T> the expected type of the attribute
     * @return the attribute value wrapped in an Optional
     */
    @SuppressWarnings("unchecked")
    public <T> Optional<T> getAttribute(String key) {
        return Optional.ofNullable((T) attributes.get(key));
    }
}