package com.adl.et.telco.crm.securerequesthandler.domain.dto;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

/**
 * Thread-local storage for request context details.
 * This class maintains request-specific information that needs to be accessible throughout the request lifecycle.
 * It provides thread-safe storage for request ID and source system ID.
 */
@Slf4j
public final class RequestContextDetail {
    private static final String LOG_PREFIX = "SRH|RequestContextDetail|";
    private static final String SET = "Set";
    private static final String GET = "Get";
    private static final String REMOVE = "Remove";
    private static final String CLEAR = "Clear";
    private static final String VALIDATE = "Validate";
    private static final String NULL_VALUE = "null";
    private static final String EMPTY_VALUE = "empty";

    private static final ThreadLocal<String> REQUEST_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> SOURCE_SYSTEM_ID = new ThreadLocal<>();

    private RequestContextDetail() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Retrieves the current request ID from thread-local storage.
     * @return The request ID, or null if not set
     */
    public static String getRequestID() {
        String requestId = REQUEST_ID.get();
        log.debug("{}|{}|RequestID: {}", LOG_PREFIX, GET, getValueForLogging(requestId));
        return requestId;
    }

    /**
     * Retrieves the current source system ID from thread-local storage.
     * @return The source system ID, or null if not set
     */
    public static String getSourceSystemID() {
        String sourceSystemId = SOURCE_SYSTEM_ID.get();
        log.debug("{}|{}|SourceSystemID: {}", LOG_PREFIX, GET, getValueForLogging(sourceSystemId));
        return sourceSystemId;
    }

    /**
     * Sets the request ID in thread-local storage.
     * @param requestId The request ID to set
     * @throws IllegalArgumentException if requestId is null or empty
     */
    public static void setRequestID(String requestId) {
        validateValue(requestId, "Request ID");
        log.debug("{}|{}|RequestID: {}", LOG_PREFIX, SET, requestId);
        REQUEST_ID.set(requestId);
    }

    /**
     * Sets the source system ID in thread-local storage.
     * @param sourceSystemId The source system ID to set
     * @throws IllegalArgumentException if sourceSystemId is null or empty
     */
    public static void setSourceSystemID(String sourceSystemId) {
        validateValue(sourceSystemId, "Source System ID");
        log.debug("{}|{}|SourceSystemID: {}", LOG_PREFIX, SET, sourceSystemId);
        SOURCE_SYSTEM_ID.set(sourceSystemId);
    }

    /**
     * Removes the request ID from thread-local storage.
     */
    public static void removeRequestId() {
        String requestId = REQUEST_ID.get();
        log.debug("{}|{}|RequestID: {}", LOG_PREFIX, REMOVE, getValueForLogging(requestId));
        REQUEST_ID.remove();
    }

    /**
     * Removes the source system ID from thread-local storage.
     */
    public static void removeSourceSystemID() {
        String sourceSystemId = SOURCE_SYSTEM_ID.get();
        log.debug("{}|{}|SourceSystemID: {}", LOG_PREFIX, REMOVE, getValueForLogging(sourceSystemId));
        SOURCE_SYSTEM_ID.remove();
    }

    /**
     * Clears all thread-local storage values.
     */
    public static void clear() {
        log.debug("{}|{}|Clearing all context values", LOG_PREFIX, CLEAR);
        removeRequestId();
        removeSourceSystemID();
    }

    /**
     * Validates if a value is not null or empty.
     * @param value The value to validate
     * @param fieldName The name of the field being validated
     * @throws IllegalArgumentException if value is null or empty
     */
    private static void validateValue(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            String errorMessage = String.format("%s cannot be null or empty", fieldName);
            log.error("{}|{}|{}", LOG_PREFIX, VALIDATE, errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }
    }

    /**
     * Gets a safe string representation of a value for logging.
     * @param value The value to log
     * @return A string representation of the value
     */
    private static String getValueForLogging(String value) {
        if (value == null) {
            return NULL_VALUE;
        }
        return value.isEmpty() ? EMPTY_VALUE : value;
    }
}
