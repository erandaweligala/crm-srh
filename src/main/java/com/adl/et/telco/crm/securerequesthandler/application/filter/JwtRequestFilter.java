package com.adl.et.telco.crm.securerequesthandler.application.filter;

import com.adl.et.telco.crm.securerequesthandler.domain.dto.common.CommonNorthBoundResponse;
import com.adl.et.telco.crm.securerequesthandler.domain.service.serviceimpl.access.JwtService;
import com.adl.et.telco.crm.securerequesthandler.application.util.ResponseHandler;
import com.adl.et.telco.crm.securerequesthandler.application.util.access.AuthLoggingConstants;
import com.adl.et.telco.crm.securerequesthandler.application.util.constants.ServiceConstants;
import com.adl.et.telco.crm.securerequesthandler.application.util.exception.BaseException;
import com.adl.et.telco.crm.securerequesthandler.application.util.resultenum.AuthCodeEnum;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;

/**
 * JWT Request Filter that handles JWT token validation and authentication.
 * This filter is responsible for:
 * 1. Extracting and validating JWT tokens from requests
 * 2. Setting up security context for authenticated users
 * 3. Handling request verification tokens
 * 4. Managing request metadata and logging
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

    private final JwtService jwtService;
    private final ResponseHandler responseHandler;
    private final ObjectMapper objectMapper;

    @Autowired
    public JwtRequestFilter(JwtService jwtService, ResponseHandler responseHandler, ObjectMapper objectMapper) {
        this.jwtService = jwtService;
        this.responseHandler = responseHandler;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException {
        if (request == null || response == null || chain == null) {
            log.error("{}doFilterInternal|ERROR|Request, response or chain is null", LOG_PREFIX);
            return;
        }

        log.info("{}doFilterInternal|START|Processing request: {}", LOG_PREFIX, request.getRequestURI());
        initialData(request);
        
        try {
            String jwtToken = extractJwtToken(request);
            
            if (jwtToken != null) {
                jwtTokenValidation(jwtToken, request);
                setupSecurityContext(jwtToken, request);
            }
            
            chain.doFilter(request, response);
            log.info("{}doFilterInternal|END|Request processed successfully", LOG_PREFIX);
        } catch (BaseException ex) {
            log.error("{}doFilterInternal|ERROR|Authentication failed: {}", LOG_PREFIX, ex.getMessage());
            handleException(response, ex);
        } catch (Exception ex) {
            log.error("{}doFilterInternal|ERROR|Unexpected error: {}", LOG_PREFIX, ex.getMessage());
            handleUnexpectedException(response);
        }
    }

    private String extractJwtToken(HttpServletRequest request) {
        if (request == null) {
            log.error("{}extractJwtToken|ERROR|Request is null", LOG_PREFIX);
            return null;
        }

        final String authorizationHeader = request.getHeader(ServiceConstants.AUTHORIZATION);
        if (authorizationHeader != null && authorizationHeader.startsWith(ServiceConstants.BEARER)) {
            return authorizationHeader.substring(7);
        }
        return null;
    }

    private void setupSecurityContext(String jwtToken, HttpServletRequest request) {
        if (jwtToken == null || request == null || jwtService == null) {
            log.error("{}setupSecurityContext|ERROR|Required parameters are null", LOG_PREFIX);
            return;
        }

        if (SecurityContextHolder.getContext().getAuthentication() == null && jwtService.isValidToken(jwtToken)) {
            String userName = jwtService.extractUsername(jwtToken);
            if (userName != null) {
                MDC.put(ServiceConstants.USERNAME, userName);
                log.info("{}setupSecurityContext|INFO|Setting up security context for user: {}", LOG_PREFIX, userName);
                
                UserDetails userDetails = new User(userName, "", jwtService.extractAuthorities(jwtToken));
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
    }

    private void jwtTokenValidation(String jwtToken, HttpServletRequest request) throws BaseException {
        if (jwtToken == null || request == null || jwtService == null) {
            log.error("{}jwtTokenValidation|ERROR|Required parameters are null", LOG_PREFIX);
            throw new BaseException(
                "Invalid token validation parameters",
                "Token validation failed due to null parameters",
                HttpStatus.UNAUTHORIZED,
                "INVALID_PARAMS",
                null
            );
        }

        String userId = jwtService.extractUsername(jwtToken);
        if (userId == null) {
            log.error("{}jwtTokenValidation|ERROR|User ID is null", LOG_PREFIX);
            throw new BaseException(
                "Invalid user ID",
                "Token validation failed due to null user ID",
                HttpStatus.UNAUTHORIZED,
                "INVALID_USER_ID",
                null
            );
        }

        log.info("{}jwtTokenValidation|INFO|Validating token for user: {}", LOG_PREFIX, userId);
        
        if (jwtService.isTokenExpired(jwtToken)) {
            log.error("{}jwtTokenValidation|ERROR|Token expired for user: {}", LOG_PREFIX, userId);
            throw new BaseException(
                AuthCodeEnum.ACCESS_TOKEN_EXPIRED.description(),
                AuthCodeEnum.ACCESS_TOKEN_EXPIRED.description(),
                HttpStatus.UNAUTHORIZED,
                AuthCodeEnum.ACCESS_TOKEN_EXPIRED.code(),
                null
            );
        }

        validateRequestVerificationToken(jwtToken, request);
    }

    private void validateRequestVerificationToken(String jwtToken, HttpServletRequest request) throws BaseException {
        if (jwtToken == null || request == null || jwtService == null) {
            log.error("{}validateRequestVerificationToken|ERROR|Required parameters are null", LOG_PREFIX);
            throw new BaseException(
                "Invalid token validation parameters",
                "Token validation failed due to null parameters",
                HttpStatus.UNAUTHORIZED,
                "INVALID_PARAMS",
                null
            );
        }

        Cookie[] cookies = request.getCookies();
        Cookie requestVerificationCookie = cookies != null ? 
            Arrays.stream(cookies)
                .filter(cookie -> cookie != null && cookie.getName() != null && cookie.getName().equals(ServiceConstants.RV_TOKEN))
                .findAny()
                .orElse(null) : null;

        if (requestVerificationCookie == null) {
            log.error("{}validateRequestVerificationToken|ERROR|RV token not found", LOG_PREFIX);
            throw new BaseException(
                AuthCodeEnum.RV_TOKEN_MUST_NOT_BE_NULL.description(),
                AuthCodeEnum.RV_TOKEN_MUST_NOT_BE_NULL.description(),
                HttpStatus.FORBIDDEN,
                AuthCodeEnum.RV_TOKEN_MUST_NOT_BE_NULL.code(),
                null
            );
        }

        String cookieValue = requestVerificationCookie.getValue();
        if (cookieValue == null) {
            log.error("{}validateRequestVerificationToken|ERROR|RV token value is null", LOG_PREFIX);
            throw new BaseException(
                "Invalid RV token value",
                "Request verification token value is null",
                HttpStatus.UNAUTHORIZED,
                "INVALID_RV_TOKEN_VALUE",
                null
            );
        }

        if (!jwtService.isValidRequestVerificationTokenUsingAccessToken(jwtToken, cookieValue)) {
            log.error("{}validateRequestVerificationToken|ERROR|Invalid RV token", LOG_PREFIX);
            throw new BaseException(
                AuthCodeEnum.INVALID_RV_TOKEN.description(),
                AuthCodeEnum.INVALID_RV_TOKEN.description(),
                HttpStatus.UNAUTHORIZED,
                AuthCodeEnum.INVALID_RV_TOKEN.code(),
                null
            );
        }
    }

    private void initialData(HttpServletRequest request) {
        if (request == null) {
            log.error("{}initialData|ERROR|Request is null", LOG_PREFIX);
            return;
        }

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

    private void handleException(HttpServletResponse response, BaseException ex) throws IOException {
        if (response == null || ex == null || responseHandler == null) {
            log.error("{}handleException|ERROR|Required parameters are null", LOG_PREFIX);
            return;
        }

        CommonNorthBoundResponse<String> responseBody = responseHandler.northBoundRespHandler(ex.getResultCode(), ex.getReason());
        response.setStatus(ex.getHttpStatus().value());
        response.setHeader(ServiceConstants.CONTENT_TYPE, ServiceConstants.APPLICATION_JSON);
        String json = objectMapper.writeValueAsString(responseBody);
        response.getWriter().write(json);
    }

    private void handleUnexpectedException(HttpServletResponse response) throws IOException {
        if (response == null || responseHandler == null) {
            log.error("{}handleUnexpectedException|ERROR|Required parameters are null", LOG_PREFIX);
            return;
        }

        CommonNorthBoundResponse<String> responseBody = responseHandler.northBoundRespHandler(
            AuthCodeEnum.INTERNAL_SERVER_ERROR.code(),
            AuthCodeEnum.INTERNAL_SERVER_ERROR.description()
        );
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setHeader(ServiceConstants.CONTENT_TYPE, ServiceConstants.APPLICATION_JSON);
        String json = objectMapper.writeValueAsString(responseBody);
        response.getWriter().write(json);
    }
}
