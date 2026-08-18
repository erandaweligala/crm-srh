package com.adl.et.telco.crm.securerequesthandler.application.filter.responsefilter;

import lombok.extern.slf4j.Slf4j;

/**
 * Thread-local storage for response filter context details.
 * This class maintains filter-specific information that needs to be accessible throughout the request lifecycle.
 * It provides thread-safe storage for action ID and filter enabled status.
 */
@Slf4j
public final class ResponseFilterContextHolder {
    private static final String LOG_PREFIX = "SRH|ResponseFilterContextHolder|";
    private static final String SET = "Set";
    private static final String GET = "Get";
    private static final String DESTROY = "Destroy";
    private static final String NULL_VALUE = "null";
    private static final String DEFAULT_FILTER_ENABLED = "false";

    private static final ThreadLocal<Integer> ACTION_ID = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> IS_FILTER_ENABLED = new ThreadLocal<>();

    private ResponseFilterContextHolder() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Sets the response filter context with action ID and enabled status.
     * @param actionId The action ID to set
     * @param isEnabled The filter enabled status to set
     */
    public static void setContext(int actionId, boolean isEnabled) {
        log.debug("{}|{}|ActionId: {}|IsEnabled: {}", LOG_PREFIX, SET, actionId, isEnabled);
        ACTION_ID.set(actionId);
        IS_FILTER_ENABLED.set(isEnabled);
    }

    /**
     * Retrieves the current action ID from thread-local storage.
     * @return The action ID, or null if not set
     */
    public static Integer getActionId() {
        Integer actionId = ACTION_ID.get();
        log.debug("{}|{}|ActionId: {}", LOG_PREFIX, GET, getValueForLogging(actionId));
        return actionId;
    }

    /**
     * Retrieves the current filter enabled status from thread-local storage.
     * @return The filter enabled status, or false if not set
     */
    public static boolean isFilterEnabled() {
        Boolean isEnabled = IS_FILTER_ENABLED.get();
        log.debug("{}|{}|IsEnabled: {}", LOG_PREFIX, GET, getValueForLogging(isEnabled));
        return isEnabled != null ? isEnabled : false;
    }

    /**
     * Removes all context values from thread-local storage.
     */
    public static void destroy() {
        Integer actionId = ACTION_ID.get();
        Boolean isEnabled = IS_FILTER_ENABLED.get();
        log.debug("{}|{}|ActionId: {}|IsEnabled: {}", LOG_PREFIX, DESTROY, 
            getValueForLogging(actionId), getValueForLogging(isEnabled));
        ACTION_ID.remove();
        IS_FILTER_ENABLED.remove();
    }

    /**
     * Gets a safe string representation of a value for logging.
     * @param value The value to log
     * @return A string representation of the value
     */
    private static String getValueForLogging(Object value) {
        if (value == null) {
            return NULL_VALUE;
        }
        if (value instanceof Boolean) {
            return value.toString();
        }
        return value.toString();
    }
}
