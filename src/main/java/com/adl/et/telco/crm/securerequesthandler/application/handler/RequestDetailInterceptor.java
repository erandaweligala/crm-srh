package com.adl.et.telco.crm.securerequesthandler.application.handler;

import com.adl.et.telco.crm.securerequesthandler.domain.dto.RequestContextDetail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Interceptor for capturing and setting request context details.
 * This interceptor sets the request ID and source system ID from the request headers.
 */
@Component
@Slf4j
public class RequestDetailInterceptor implements HandlerInterceptor {
    private static final String LOG_PREFIX = "SRH|RequestDetailInterceptor|";
    private static final String REQUEST_ID = "X-Request-Id";
    private static final String SOURCE_SYSTEM_ID = "X-Source-System-Id";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (request == null || response == null) {
            log.error("{}preHandle|ERROR|Request or response is null", LOG_PREFIX);
            return false;
        }

        log.info("{}preHandle|START|Processing request: {}", LOG_PREFIX, request.getRequestURI());
        
        String requestId = request.getHeader(REQUEST_ID);
        String sourceSystemId = request.getHeader(SOURCE_SYSTEM_ID);
        
        if (requestId != null) {
            RequestContextDetail.setRequestID(requestId);
        }
        
        if (sourceSystemId != null) {
            RequestContextDetail.setSourceSystemID(sourceSystemId);
        }
        
        log.info("{}preHandle|INFO|Request ID: {}, Source System ID: {}", LOG_PREFIX, requestId, sourceSystemId);
        log.info("{}preHandle|END|Request context details set", LOG_PREFIX);
        
        return true;
    }
}
