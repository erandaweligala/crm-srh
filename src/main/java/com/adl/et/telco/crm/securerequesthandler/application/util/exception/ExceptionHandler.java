package com.adl.et.telco.crm.securerequesthandler.application.util.exception;

import com.adl.et.telco.crm.securerequesthandler.application.util.resultenum.DisplayResultCodeEnum;
import com.adl.et.telco.crm.securerequesthandler.application.util.resultenum.ResponseCodeEnum;
import com.adl.et.telco.crm.securerequesthandler.domain.dto.common.Result;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;

/**
 * Handles exceptions and converts them into appropriate BaseException instances.
 * Provides methods for handling different types of exceptions at various layers.
 */
@Slf4j
@Component
public class ExceptionHandler {
    private static final String LOG_PREFIX = "SRH|ExceptionHandler|";

    /**
     * Handles HttpStatusCodeException and extracts error information from the response.
     *
     * @param e The HttpStatusCodeException to handle
     * @return Result containing the error code and description
     * @throws BaseException if parsing the response fails
     */
    public Result exceptionHandler(HttpStatusCodeException e) throws BaseException {
        log.debug("{}exceptionHandler|Start|Status: {}", LOG_PREFIX, e.getStatusCode());
        
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode resultJsonNode = objectMapper.readTree(e.getResponseBodyAsString()).get("result");

            if(resultJsonNode != null && resultJsonNode.get("resultCode") != null && resultJsonNode.get("resultDescription") != null){
                String resultCode = resultJsonNode.get("resultCode").asText();
                String resultDescription = resultJsonNode.get("resultDescription").asText();

                Result resultObj = new Result();
                resultObj.setResultDescription(resultDescription);
                resultObj.setResultCode(resultCode);
                log.debug("{}exceptionHandler|End|Code: {}|Description: {}", LOG_PREFIX, resultCode, resultDescription);
                return resultObj;
            }
        } catch (Exception ex) {
            log.error("{}exceptionHandler|Error|Failed to parse response: {}", LOG_PREFIX, ex.getMessage(), ex);
            throw new BaseException(
                ex.getMessage(),
                ResponseCodeEnum.INTERNAL_SERVER_ERROR.description(),
                HttpStatus.INTERNAL_SERVER_ERROR,
                ResponseCodeEnum.INTERNAL_SERVER_ERROR.code(),
                ex.getStackTrace()
            );
        }
        throw e;
    }

    /**
     * Handles exceptions at the client layer and converts them to BaseException.
     *
     * @param ex The exception to handle
     * @param displayResultCodeEnum The result code enum for the error
     * @return BaseException with appropriate error details
     */
    public BaseException clientExceptionHandler(Exception ex, DisplayResultCodeEnum displayResultCodeEnum) {
        log.debug("{}clientExceptionHandler|Start|Type: {}", LOG_PREFIX, ex.getClass().getSimpleName());
        
        try {
            if (ex instanceof HttpStatusCodeException) {
                HttpStatusCodeException hex = (HttpStatusCodeException) ex;
                if (hex.getStatusCode().is4xxClientError()) {
                    Result result = exceptionHandler(hex);
                    log.warn("{}clientExceptionHandler|ClientError|Code: {}|Description: {}", 
                        LOG_PREFIX, result.getResultCode(), result.getResultDescription());
                    return new BaseException(
                        result.getResultDescription(),
                        result.getResultDescription(),
                        HttpStatus.valueOf(hex.getStatusCode().value()),
                        result.getResultCode(),
                        ex.getStackTrace()
                    );
                }
            } else if (ex instanceof BaseException) {
                return (BaseException) ex;
            }
            
            log.error("{}clientExceptionHandler|InternalError|Code: {}", 
                LOG_PREFIX, displayResultCodeEnum.code());
            return new BaseException(
                displayResultCodeEnum.description(),
                displayResultCodeEnum.description(),
                HttpStatus.INTERNAL_SERVER_ERROR,
                displayResultCodeEnum.code(),
                ex.getStackTrace()
            );
        } catch (Exception e) {
            log.error("{}clientExceptionHandler|Error|Failed to handle exception: {}", 
                LOG_PREFIX, e.getMessage(), e);
            return new BaseException(
                e.getMessage(),
                DisplayResultCodeEnum.EXCEPTION_IN_CLIENT_LAYER.description(),
                HttpStatus.INTERNAL_SERVER_ERROR,
                DisplayResultCodeEnum.EXCEPTION_IN_CLIENT_LAYER.code(),
                e.getStackTrace()
            );
        }
    }

    /**
     * Handles exceptions at the service layer and converts them to BaseException.
     *
     * @param ex The exception to handle
     * @param displayResultCodeEnum The result code enum for the error
     * @return BaseException with appropriate error details
     * @throws BaseException if the exception cannot be handled
     */
    public BaseException serviceExceptionHandler(Exception ex, DisplayResultCodeEnum displayResultCodeEnum) throws BaseException {
        log.debug("{}serviceExceptionHandler|Start|Type: {}", LOG_PREFIX, ex.getClass().getSimpleName());
        
        try {
            if (ex instanceof BaseException) {
                return (BaseException) ex;
            }
            
            log.error("{}serviceExceptionHandler|InternalError|Code: {}", 
                LOG_PREFIX, displayResultCodeEnum.code());
            return new BaseException(
                ex.getMessage(),
                displayResultCodeEnum.description(),
                HttpStatus.INTERNAL_SERVER_ERROR,
                displayResultCodeEnum.code(),
                ex.getStackTrace()
            );
        } catch (Exception e) {
            log.error("{}serviceExceptionHandler|Error|Failed to handle exception: {}", 
                LOG_PREFIX, e.getMessage(), e);
            return new BaseException(
                e.getMessage(),
                DisplayResultCodeEnum.EXCEPTION_IN_SERVICE_LAYER.description(),
                HttpStatus.INTERNAL_SERVER_ERROR,
                DisplayResultCodeEnum.EXCEPTION_IN_SERVICE_LAYER.code(),
                e.getStackTrace()
            );
        }
    }
}
