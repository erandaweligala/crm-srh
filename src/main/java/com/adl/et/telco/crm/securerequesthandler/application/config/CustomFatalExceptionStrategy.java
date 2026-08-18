package com.adl.et.telco.crm.securerequesthandler.application.config;

import org.springframework.amqp.rabbit.listener.ConditionalRejectingErrorHandler;

/**
 * Custom exception strategy for handling fatal errors in RabbitMQ listeners.
 * Extends the default exception strategy with enhanced logging.
 */
public class CustomFatalExceptionStrategy extends ConditionalRejectingErrorHandler.DefaultExceptionStrategy {
    /**
     * Determines if an exception is fatal and should be rejected.
     * @param t The throwable to check
     * @return false to indicate the exception is not fatal
     */
    @Override
    public boolean isFatal(Throwable t) {
        return false;
    }
}