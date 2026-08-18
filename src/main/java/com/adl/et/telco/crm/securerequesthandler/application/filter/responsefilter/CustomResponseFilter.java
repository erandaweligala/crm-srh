package com.adl.et.telco.crm.securerequesthandler.application.filter.responsefilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Response filter placeholder.
 *
 * AUTHENTICATION BYPASS: this filter used to mask response attributes the caller
 * was not permitted to see, which required decoding the JWT and reading its
 * permission claims. Attribute level authorization is switched off, so responses
 * are now returned to the caller exactly as the downstream microservice produced
 * them.
 */
@Component
@WebFilter(urlPatterns = "/*")
@Order(2)
@Slf4j
public class CustomResponseFilter extends OncePerRequestFilter {
    private static final String LOG_PREFIX = "SRH|CustomResponseFilter|";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        log.debug("{}doFilterInternal|Response filtering disabled, passing response through|URI: {}",
                LOG_PREFIX, request.getRequestURI());
        try {
            filterChain.doFilter(request, response);
        } finally {
            ResponseFilterContextHolder.destroy();
        }
    }
}
