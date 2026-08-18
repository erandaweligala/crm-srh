package com.adl.et.telco.crm.securerequesthandler.application.filter.responsefilter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Interceptor for handling response filtering based on method annotations.
 * Checks for @ResponseFiltering annotations and sets up the filter context accordingly.
 */
@Slf4j
@Component
public class ResponseFilterInterceptor implements HandlerInterceptor {
    private static final String LOG_PREFIX = "SRH|ResponseFilterInterceptor|";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        log.debug("{}preHandle|Start|URI: {}", LOG_PREFIX, request.getRequestURI());

        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            ResponseFiltering filter = handlerMethod.getMethodAnnotation(ResponseFiltering.class);
            
            if (filter != null) {
                log.debug("{}preHandle|Setting filter context|ActionId: {}|Enabled: {}", 
                    LOG_PREFIX, filter.actionId(), filter.enable());
                ResponseFilterContextHolder.setContext(filter.actionId(), filter.enable());
            }
        }

        log.debug("{}preHandle|End", LOG_PREFIX);
        return true;
    }
}