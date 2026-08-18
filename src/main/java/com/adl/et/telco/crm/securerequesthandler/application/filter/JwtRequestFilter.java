package com.adl.et.telco.crm.securerequesthandler.application.filter;

import com.adl.et.telco.crm.securerequesthandler.application.util.access.AuthLoggingConstants;
import com.adl.et.telco.crm.securerequesthandler.application.util.constants.ServiceConstants;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

/**
 * Request filter that prepares the logging context for every incoming request.
 *
 * AUTHENTICATION BYPASS: this filter no longer validates JWT tokens, no longer
 * checks the request verification (RV) cookie and no longer populates the Spring
 * Security context. Requests are passed down the chain untouched whether or not
 * they carry an Authorization header.
 */
@Component
@Slf4j
@Order(1)
public class JwtRequestFilter extends OncePerRequestFilter {
    private static final String LOG_PREFIX = "SRH|JwtRequestFilter|";
    private static final String X_FORWARDED_FOR = "X-Forwarded-For";
    private static final String CACHE_ID = "cache-id";
    private static final String CLIENT_IP = "Client-IP";
    private static final String MESSAGE_ID = "Message-ID";
    private static final String CACHE_ID_MDC = "Cache-Id";
    private static final String ACTUATOR_HEALTH = "/actuator/health";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request == null || response == null || chain == null) {
            log.error("{}doFilterInternal|ERROR|Request, response or chain is null", LOG_PREFIX);
            return;
        }

        log.info("{}doFilterInternal|START|Processing request: {}", LOG_PREFIX, request.getRequestURI());
        initialData(request);

        // No token validation, no authentication, no authorization - forward as is.
        chain.doFilter(request, response);

        log.info("{}doFilterInternal|END|Request forwarded without authentication", LOG_PREFIX);
    }

    private void initialData(HttpServletRequest request) {
        log.info("{}initialData|START|Initializing request data", LOG_PREFIX);

        MDC.remove(ServiceConstants.SUBJECT_VALUE);
        String clientIp = request.getHeader(X_FORWARDED_FOR);
        if (clientIp != null) {
            MDC.put(CLIENT_IP, clientIp);
        }
        MDC.put(MESSAGE_ID, UUID.randomUUID().toString());

        String cacheId = request.getHeader(CACHE_ID);
        if (cacheId != null) {
            MDC.put(CACHE_ID_MDC, cacheId);
        }

        MDC.remove(ServiceConstants.USERNAME);

        if (!request.getRequestURI().equalsIgnoreCase(ACTUATOR_HEALTH)) {
            log.info("{}initialData|INFO|{}", LOG_PREFIX, AuthLoggingConstants.REQUEST_RECEIVED, request.getMethod(), request.getRequestURI());
        }

        log.info("{}initialData|END|Request data initialized", LOG_PREFIX);
    }
}
