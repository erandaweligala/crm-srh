package com.adl.et.telco.crm.securerequesthandler.application.util;

import com.adl.et.telco.crm.securerequesthandler.domain.dto.PageDetailDto;
import com.adl.et.telco.crm.securerequesthandler.domain.dto.common.CommonNorthBoundResponse;
import com.adl.et.telco.crm.securerequesthandler.application.util.resultenum.DisplayResultCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Utility class for handling and building standardized responses.
 * Provides methods for creating consistent response objects with proper status codes and messages.
 */
@Component
@Slf4j
public class ResponseHandler {
    private static final String LOG_PREFIX = "SRH|ResponseHandler|";
    private static final String START = "START";
    private static final String END = "END";
    private static final String ERROR = "ERROR";
    private static final String INVALID_PARAMS = "INVALID_PARAMS";
    private static final String INVALID_PAGINATION = "INVALID_PAGINATION";
    private static final String INVALID_RESPONSE_PARAMS = "Invalid response parameters";
    private static final String INVALID_PAGINATION_DETAILS = "Invalid pagination details";

    /**
     * Creates a response with the specified code and description.
     * @param code The response code
     * @param description The response description
     * @return A CommonNorthBoundResponse with the specified code and description
     */
    public <T> CommonNorthBoundResponse<T> respHandler(String code, String description) {
        log.debug("{}|{}|Creating response with code: {}, description: {}", LOG_PREFIX, START, code, description);
        
        if (!StringUtils.hasText(code) || !StringUtils.hasText(description)) {
            log.error("{}|{}|Code or description is null or empty", LOG_PREFIX, ERROR);
            return createErrorResponse(INVALID_PARAMS, INVALID_RESPONSE_PARAMS);
        }

        CommonNorthBoundResponse<T> resp = new CommonNorthBoundResponse<>();
        commonAdaptorRespCreator(resp, code, description);
        log.debug("{}|{}|Response created successfully", LOG_PREFIX, END);
        return resp;
    }

    /**
     * Creates a success response with default success code and description.
     * @return A CommonNorthBoundResponse with success code and description
     */
    public <T> CommonNorthBoundResponse<T> success() {
        log.debug("{}|{}|Creating success response", LOG_PREFIX, START);
        CommonNorthBoundResponse<T> resp = new CommonNorthBoundResponse<>();
        commonAdaptorRespCreator(resp, DisplayResultCodeEnum.SUCCESS.code(), DisplayResultCodeEnum.SUCCESS.description());
        log.debug("{}|{}|Success response created", LOG_PREFIX, END);
        return resp;
    }

    /**
     * Creates a success response with data and pagination details.
     * @param body The response data
     * @param pageDetailDto The pagination details
     * @return A CommonNorthBoundResponse with data and pagination
     */
    public <T> CommonNorthBoundResponse<T> success(T body, PageDetailDto pageDetailDto) {
        log.debug("{}|{}|Creating success response with data and pagination", LOG_PREFIX, START);
        
        if (pageDetailDto == null) {
            log.error("{}|{}|PageDetailDto is null", LOG_PREFIX, ERROR);
            return createErrorResponse(INVALID_PAGINATION, INVALID_PAGINATION_DETAILS);
        }

        CommonNorthBoundResponse<T> resp = new CommonNorthBoundResponse<>();
        resp.setData(body);
        commonAdaptorRespCreator(resp, DisplayResultCodeEnum.SUCCESS.code(), DisplayResultCodeEnum.SUCCESS.description(), pageDetailDto);
        log.debug("{}|{}|Success response with data created", LOG_PREFIX, END);
        return resp;
    }

    /**
     * Creates a northbound response with the specified code and description.
     * @param code The response code
     * @param description The response description
     * @return A CommonNorthBoundResponse with the specified code and description
     */
    public <T> CommonNorthBoundResponse<T> northBoundRespHandler(String code, String description) {
        log.debug("{}|{}|Creating northbound response with code: {}, description: {}", LOG_PREFIX, START, code, description);
        
        if (!StringUtils.hasText(code) || !StringUtils.hasText(description)) {
            log.error("{}|{}|Code or description is null or empty", LOG_PREFIX, ERROR);
            return createErrorResponse(INVALID_PARAMS, INVALID_RESPONSE_PARAMS);
        }

        CommonNorthBoundResponse<T> resp = new CommonNorthBoundResponse<>();
        resp.setCode(code);
        resp.setMessage(description);
        resp.setDescription(description);
        resp.setData(null);
        log.debug("{}|{}|Northbound response created", LOG_PREFIX, END);
        return resp;
    }

    /**
     * Builds a response with the specified object, description, and code.
     * @param object The response data
     * @param description The response description
     * @param code The response code
     * @return A CommonNorthBoundResponse with the specified parameters
     */
    public <T> CommonNorthBoundResponse<T> responseBuilder(T object, String description, String code) {
        log.debug("{}|{}|Building response with code: {}, description: {}", LOG_PREFIX, START, code, description);
        
        if (!StringUtils.hasText(description) || !StringUtils.hasText(code)) {
            log.error("{}|{}|Description or code is null or empty", LOG_PREFIX, ERROR);
            return createErrorResponse(INVALID_PARAMS, INVALID_RESPONSE_PARAMS);
        }

        CommonNorthBoundResponse<T> resp = new CommonNorthBoundResponse<>();
        resp.setCode(code);
        resp.setMessage(description);
        resp.setDescription(description);
        resp.setData(object);
        log.debug("{}|{}|Response built successfully", LOG_PREFIX, END);
        return resp;
    }

    /**
     * Builds a response with pagination information.
     * @param object The response data
     * @param description The response description
     * @param code The response code
     * @param pageDetailDto The pagination details
     * @return A CommonNorthBoundResponse with data and pagination
     */
    public <T> CommonNorthBoundResponse<T> responseBuilderWithPageInformation(
            T object, String description, String code, PageDetailDto pageDetailDto) {
        log.debug("{}|{}|Building response with pagination", LOG_PREFIX, START);
        
        if (!StringUtils.hasText(description) || !StringUtils.hasText(code) || pageDetailDto == null) {
            log.error("{}|{}|responseBuilderWithPageInformation|Required parameters are null or empty", LOG_PREFIX, ERROR);
            return createErrorResponse(INVALID_PARAMS, INVALID_RESPONSE_PARAMS);
        }

        CommonNorthBoundResponse<T> resp = new CommonNorthBoundResponse<>();
        resp.setCode(code);
        resp.setMessage(description);
        resp.setDescription(description);
        resp.setData(object);
        resp.setPageDetail(pageDetailDto);
        log.debug("{}|{}|Response with pagination built", LOG_PREFIX, END);
        return resp;
    }

    /**
     * Helper method to set common response fields.
     * @param response The response object to update
     * @param code The response code
     * @param description The response description
     */
    private void commonAdaptorRespCreator(CommonNorthBoundResponse response, String code, String description) {
        if (response == null || !StringUtils.hasText(code) || !StringUtils.hasText(description)) {
            log.error("{}|{}|Required parameters are null or empty", LOG_PREFIX, ERROR);
            return;
        }

        log.debug("{}|Setting response fields - code: {}, description: {}", LOG_PREFIX, code, description);
        response.setCode(code);
        response.setMessage(description);
        response.setDescription(description);
    }

    /**
     * Helper method to set common response fields with pagination.
     * @param response The response object to update
     * @param code The response code
     * @param description The response description
     * @param pageDetailDto The pagination details
     */
    private void commonAdaptorRespCreator(
            CommonNorthBoundResponse response, String code, String description, PageDetailDto pageDetailDto) {
        if (response == null || !StringUtils.hasText(code) || !StringUtils.hasText(description) || pageDetailDto == null) {
            log.error("{}|{}|Required parameters are null or empty", LOG_PREFIX, ERROR);
            return;
        }

        log.debug("{}|Setting response fields with pagination", LOG_PREFIX);
        response.setCode(code);
        response.setMessage(description);
        response.setDescription(description);
        response.setPageDetail(pageDetailDto);
    }

    /**
     * Creates an error response with the specified code and message.
     * @param code The error code
     * @param message The error message
     * @return A CommonNorthBoundResponse with error details
     */
    private <T> CommonNorthBoundResponse<T> createErrorResponse(String code, String message) {
        log.debug("{}|Creating error response with code: {}, message: {}", LOG_PREFIX, code, message);
        CommonNorthBoundResponse<T> errorResponse = new CommonNorthBoundResponse<>();
        errorResponse.setCode(code);
        errorResponse.setMessage(message);
        errorResponse.setDescription(message);
        return errorResponse;
    }
}
