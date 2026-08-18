package com.adl.et.telco.crm.securerequesthandler.application.controller;

import com.adl.et.telco.crm.securerequesthandler.application.client.ums.ActionInfoClient;
import com.adl.et.telco.crm.securerequesthandler.application.filter.responsefilter.ResponseFilterContextHolder;
import com.adl.et.telco.crm.securerequesthandler.domain.dto.action.RouteToActionDto;
import com.adl.et.telco.crm.securerequesthandler.domain.dto.common.CommonNorthBoundResponse;
import com.adl.et.telco.crm.securerequesthandler.domain.dto.common.CommonSouthBoundResponse;
import com.adl.et.telco.crm.securerequesthandler.domain.service.serviceimpl.access.JwtService;
import com.adl.et.telco.crm.securerequesthandler.domain.service.serviceimpl.common.MicroServiceURLFetchService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.adl.et.telco.crm.securerequesthandler.application.util.constants.ServiceConstants.SUB;

/**
 * Base controller class providing common functionality for all controllers.
 * Handles URL resolution, response formatting, and common request processing.
 */
@Slf4j
@RequiredArgsConstructor
public abstract class BaseController {

    private final JwtService jwtService;
    private final MicroServiceURLFetchService microServiceURLFetchService;

    @Autowired
    private ActionInfoClient actionInfoClient;

    /**
     * Creates a standardized response entity with the provided response object.
     *
     * @param commonNorthBoundResponse The response object to be returned
     * @param <T> The type of data in the response
     * @return ResponseEntity containing the response object
     */
    protected <T> ResponseEntity<CommonNorthBoundResponse<T>> setResponseEntity(CommonNorthBoundResponse<T> commonNorthBoundResponse) {
        log.debug("SRH|Setting response entity with code: {} and message: {}", 
            commonNorthBoundResponse.getCode(), 
            commonNorthBoundResponse.getMessage());
        return ResponseEntity.status(HttpStatus.OK).body(commonNorthBoundResponse);
    }

    /**
     * Performs common action for request processing, including URL resolution and timing.
     *
     * @param request The HTTP request
     * @param requestParams Request parameters
     * @return Resolved microservice URL
     * @throws RuntimeException if URL resolution fails
     */
    protected String performCommonAction(HttpServletRequest request, Map<String, String> requestParams) {
        long startTime = System.currentTimeMillis();
        String uri = request.getRequestURI();
        log.info("SRH|Processing request for resource URI: {}", uri);
        log.debug("SRH|Request parameters: {}", requestParams);
        
        try {
            String result = microServiceURLFetchService.checkAndReturnURL(uri, requestParams);
            long duration = System.currentTimeMillis() - startTime;
            log.info("SRH|Successfully processed request for URI: {} in {} ms", uri, duration);
            return result;
        } catch (Exception e) {
            log.error("SRH|Error processing request for URI: {} - Error: {}", uri, e.getMessage(), e);
            throw new RuntimeException("Failed to process request: " + e.getMessage(), e);
        }
    }

    /**
     * Populates common action for request processing, including URL resolution and timing.
     *
     * @param request The HTTP request
     * @param requestParams Request parameters
     * @return Resolved microservice URL
     * @throws RuntimeException if URL resolution fails
     */
    protected String populateCommonAction(HttpServletRequest request, Map<String, String> requestParams) {
        long startTime = System.currentTimeMillis();
        String uri = request.getRequestURI();
        log.info("SRH|Populating action for resource URI: {}", uri);
        log.debug("SRH|Request parameters: {}", requestParams);
        
        try {
            String result = microServiceURLFetchService.findAndReturnURL(uri, requestParams);
            if (Objects.nonNull(result)) {
                setActionContext(uri);
            }
            long duration = System.currentTimeMillis() - startTime;
            log.info("SRH|Successfully populated action for URI: {} in {} ms", uri, duration);
            return result;
        } catch (Exception e) {
            log.error("SRH|Error populating action for URI: {} - Error: {}", uri, e.getMessage(), e);
            throw new RuntimeException("Failed to populate action: " + e.getMessage(), e);
        }
    }

    protected void setActionContext(String uri){
        try {
            CommonSouthBoundResponse<List<RouteToActionDto>> actionResponse = actionInfoClient.getActionsByRouteUri(uri);
            if (Objects.nonNull(actionResponse) && !actionResponse.getResponseData().isEmpty()) {
                RouteToActionDto routeToActionDto = actionResponse.getResponseData().get(0);

                if (!routeToActionDto.getActionIds().isEmpty()) {
                    Long actionId = routeToActionDto.getActionIds().get(0);
                    ResponseFilterContextHolder.setContext(Integer.parseInt(String.valueOf(actionId)), true);
                    log.info("SRH|Action context set successfully for URI: {} with action ID: {}", uri, actionId);
                } else {
                    log.warn("SRH|No action IDs found for URI: {}", uri);
                }
            }
        } catch (Exception e) {
            log.error("SRH|Error setting action context for URI: {} - Error: {}", uri, e.getMessage(), e);
        }
    }

    protected String getUserName(HttpServletRequest request) {
        Claims allClaims = jwtService.extractAllClaims(jwtService.tokenExtractor(request));
        Object userNameObj = allClaims.get(SUB);

        if (userNameObj == null) {
            log.error("SRH|Username not found in token");
            return null;
        }

        String userName = null;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            userName = objectMapper.writeValueAsString(userNameObj);
        } catch (JsonProcessingException e) {
            log.error("SRH|Error converting username to string: {}", e.getMessage(), e);
        }
        log.debug("SRH|Extracted username from token: {}", userName);
        return userName;
    }


}
