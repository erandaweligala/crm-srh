package com.adl.et.telco.crm.securerequesthandler.application.util.advice;

import com.adl.et.telco.crm.securerequesthandler.application.client.QueuePublisher;
import com.adl.et.telco.crm.securerequesthandler.application.client.auditview.AuditViewClient;
import com.adl.et.telco.crm.securerequesthandler.domain.dto.ActionLogDTO;
import com.adl.et.telco.crm.securerequesthandler.domain.dto.RequestContextDetail;
import com.adl.et.telco.crm.securerequesthandler.domain.dto.common.CommonNorthBoundResponse;
import com.adl.et.telco.crm.securerequesthandler.application.util.annotations.ActionLog;
import com.adl.et.telco.crm.securerequesthandler.application.util.constants.LoggingAdviceConstants;
import com.adl.et.telco.crm.securerequesthandler.application.util.constants.ServiceConstants;
import com.adl.et.telco.crm.securerequesthandler.application.util.exception.BaseException;
import com.adl.et.telco.crm.securerequesthandler.application.util.resultenum.ResponseCodeEnum;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import jakarta.servlet.http.HttpServletRequest;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;

@Component
@Slf4j
@Aspect
public class AuditLogAdviser {

    private static final String STATUS_SUCCESS = "SUCCESS";
    private static final String STATUS_FAILED = "FAILED";
    private static final String STATUS_IN_PROGRESS = "IN_PROGRESS";
    private static final String AUDIT_ACTION_INSERT = "INSERT";
    private static final String AUDIT_ACTION_UPDATE = "UPDATE";
    private static final String ERROR_MESSAGE_QUEUE_UNAVAILABLE = "MESSAGE QUEUE UNAVAILABLE";
    private static final String ERROR_DIRECT_LOGGING_FAILED = "MESSAGE QUEUE UNAVAILABLE AND DIRECT LOGGING FAILED";

    @Autowired
    private QueuePublisher queuePublisher;

    @Autowired
    private AuditViewClient auditViewClient;

    @Value("${tenant-id}")
    private String tenantId;

    private final ObjectMapper mapper = new ObjectMapper();

    @SneakyThrows
    @Around("@annotation(actionLogAnnotation)")
    public Object logGetMethod(ProceedingJoinPoint jointPoint, ActionLog actionLogAnnotation) {
        return logRequestResponse(jointPoint, actionLogAnnotation);
    }

    @SneakyThrows
    private Object logRequestResponse(ProceedingJoinPoint joinPoint, ActionLog actionLogAnnotation) {
        long startTime = System.currentTimeMillis();
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        
        // Extract request details
        Object requestBody = joinPoint.getArgs()[actionLogAnnotation.requestBodyParamIndex()];
        HttpServletRequest httpServletRequest = (HttpServletRequest) joinPoint.getArgs()[actionLogAnnotation.httpRequestParamIndex()];
        String className = methodSignature.getDeclaringTypeName();
        String methodName = methodSignature.getName();
        
        // Extract subject value
        String subjectValue = extractSubjectValue(actionLogAnnotation, requestBody);
        
        // Create initial action log
        ActionLogDTO actionLogDTO = createInitialActionLog(
            actionLogAnnotation.actionID(),
            actionLogAnnotation.subjectType(),
            subjectValue
        );
        
        // Persist initial log
        persistActionLog(actionLogDTO);
        
        // Set MDC context
        setMDCContext(methodName, className);
        
        // Log request initiation
        logRequestInitiation(httpServletRequest, joinPoint.getArgs());

        try {
            // Process the request
            Object result = joinPoint.proceed();
            
            // Update and persist success log
            actionLogDTO = updateActionLogWithSuccessResult(actionLogDTO, result);
            persistActionLog(actionLogDTO);
            
            // Log response
            logResponse(result, startTime);
            
            return result;
        } catch (BaseException ex) {
            handleBaseException(ex, actionLogDTO, startTime);
            throw ex;
        } catch (RuntimeException ex) {
            handleRuntimeException(ex, actionLogDTO, startTime);
            throw new BaseException(
                ex.getMessage(),
                ResponseCodeEnum.EXCEPTION_ADVISER_CONTROLLER.description(),
                HttpStatus.INTERNAL_SERVER_ERROR,
                ResponseCodeEnum.EXCEPTION_ADVISER_CONTROLLER.code(),
                ex.getStackTrace()
            );
        }
    }

    private String extractSubjectValue(ActionLog actionLogAnnotation, Object requestBody) {
        if (actionLogAnnotation.subjectType().isEmpty()) {
            return "";
        }
        
        try {
            if (!actionLogAnnotation.subjectIsAParam()) {
                return JsonPath.parse(mapper.writeValueAsString(requestBody))
                    .read(actionLogAnnotation.pathToSubjectValue());
            } else {
                return (String) requestBody;
            }
        } catch (Exception e) {
            log.warn("Failed to extract subject value: {}", e.getMessage());
            return "";
        }
    }

    private void setMDCContext(String methodName, String className) {
        MDC.put(LoggingAdviceConstants.API_NAME, methodName);
        MDC.put(LoggingAdviceConstants.CLASS_NAME, className);
        MDC.put(LoggingAdviceConstants.METHOD_NAME, methodName);
        MDC.put(LoggingAdviceConstants.REQUEST_ID, RequestContextDetail.getRequestID());
        MDC.put(LoggingAdviceConstants.SOURCE_ID, RequestContextDetail.getSourceSystemID());
    }

    private void logRequestInitiation(HttpServletRequest request, Object[] args) {
        log.info(LoggingAdviceConstants.REQUEST_INITIATED, 
            request.getMethod(), request.getRequestURI());
        log.debug(LoggingAdviceConstants.FULL_REQUEST, Arrays.toString(args));
    }

    private void logResponse(Object result, long startTime) throws Exception {
        String response = mapper.writeValueAsString(result);
        JSONParser parser = new JSONParser();
        JSONObject responseBody = (JSONObject) ((JSONObject) parser.parse(response)).get("body");
        
        String resultCode = (String) responseBody.get("code");
        String resultDescription = (String) responseBody.get("description");
        long elapsedTime = System.currentTimeMillis() - startTime;
        
        log.info(LoggingAdviceConstants.REQUEST_TERMINATED, 
            resultDescription, resultCode, elapsedTime);
        log.debug(LoggingAdviceConstants.FULL_RESPONSE, response);
    }

    private void handleBaseException(BaseException ex, ActionLogDTO actionLogDTO, long startTime) {
        long elapsedTime = System.currentTimeMillis() - startTime;
        log.error(LoggingAdviceConstants.EXCEPTION_REQUEST_TERMINATED, 
            ex.getMessage(), ex.getReason(), ex.getResultCode(),
            ex.getHttpStatus(), displayStackStraceArray(ex.getStackTraceElements()), 
            elapsedTime);
        
        actionLogDTO = updateActionLogWithErrorResult(actionLogDTO, ex);
        persistActionLog(actionLogDTO);
    }

    private void handleRuntimeException(RuntimeException ex, ActionLogDTO actionLogDTO, long startTime) {
        long elapsedTime = System.currentTimeMillis() - startTime;
        log.error(LoggingAdviceConstants.EXCEPTION_REQUEST_TERMINATED, 
            ex.getMessage(), ResponseCodeEnum.EXCEPTION_ADVISER_CONTROLLER.description(),
            ResponseCodeEnum.EXCEPTION_ADVISER_CONTROLLER.code(),
            HttpStatus.INTERNAL_SERVER_ERROR,
            displayStackStraceArray(ex.getStackTrace()), 
            elapsedTime);
        
        actionLogDTO = updateActionLogWithErrorResult(actionLogDTO, ex);
        persistActionLog(actionLogDTO);
    }

    private void persistActionLog(ActionLogDTO actionLogDTO) {
        try {
            log.debug("Attempting to send action log to queue: {}", actionLogDTO);
            queuePublisher.send(actionLogDTO);
        } catch (Exception e) {
            log.warn(ERROR_MESSAGE_QUEUE_UNAVAILABLE);
            try {
                log.debug("Attempting direct audit view logging: {}", actionLogDTO);
                auditViewClient.sendActionLogs(actionLogDTO);
            } catch (Exception ex) {
                log.error(ERROR_DIRECT_LOGGING_FAILED, ex.getMessage());
                throw ex;
            }
        }
    }

    private String displayStackStraceArray(StackTraceElement[] stackTraceElements) {
        StringBuilder stringBuilder = new StringBuilder();
        if (stackTraceElements != null) {
            for (StackTraceElement elem : stackTraceElements) {
                if (elem.getClassName().startsWith(LoggingAdviceConstants.PACKAGE_ROOT) 
                        && elem.getLineNumber() > 0) {
                    stringBuilder.append(elem.toString());
                    break;
                }
            }
        }
        return stringBuilder.toString();
    }

    private ActionLogDTO updateActionLogWithSuccessResult(ActionLogDTO actionLogDTO, Object result) {
        if (actionLogDTO == null || result == null) {
            return actionLogDTO;
        }

        Object body = ((ResponseEntity<?>) result).getBody();
        if (!(body instanceof CommonNorthBoundResponse)) {
            return actionLogDTO;
        }

        CommonNorthBoundResponse<?> response = (CommonNorthBoundResponse<?>) body;

        actionLogDTO.setStatus(STATUS_SUCCESS);
        actionLogDTO.setStatusDescription(
                response.getDescription() != null ? response.getDescription() : "No description"
        );
        actionLogDTO.setStatusCode(
                response.getCode() != null ? response.getCode() : "UNKNOWN"
        );
        actionLogDTO.setCompletedDate(LocalDateTime.now().toString());
        actionLogDTO.setAuditAction(AUDIT_ACTION_UPDATE);

        return actionLogDTO;
    }


    private ActionLogDTO updateActionLogWithErrorResult(ActionLogDTO actionLogDTO, RuntimeException ex) {
        actionLogDTO.setStatus(STATUS_FAILED);
        actionLogDTO.setStatusDescription(ex.getMessage());
        actionLogDTO.setStatusCode("500");
        actionLogDTO.setCompletedDate(LocalDateTime.now().toString());
        actionLogDTO.setAuditAction(AUDIT_ACTION_UPDATE);
        
        return actionLogDTO;
    }

    private ActionLogDTO createInitialActionLog(int actionId, String subjectType, String subjectValue) {
        ActionLogDTO actionLogDTO = new ActionLogDTO();
        actionLogDTO.setCreatedDate(LocalDateTime.now().toString());
        actionLogDTO.setUserName(MDC.get(ServiceConstants.USERNAME));
        actionLogDTO.setStatus(STATUS_IN_PROGRESS);
        actionLogDTO.setTenantId(Integer.parseInt(tenantId));
        actionLogDTO.setAuditAction(AUDIT_ACTION_INSERT);
        actionLogDTO.setActivityId(MDC.get("Message-ID"));
        actionLogDTO.setActionId(actionId);
        actionLogDTO.setSubjectType(subjectType);
        actionLogDTO.setSubjectValue(subjectValue);
        
        return actionLogDTO;
    }
}
