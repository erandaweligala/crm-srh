package com.adl.et.telco.crm.securerequesthandler.application.util.advice;

import com.adl.et.telco.crm.securerequesthandler.application.util.resultenum.ResponseCodeEnum;
import com.adl.et.telco.crm.securerequesthandler.application.util.constants.LoggingAdviceConstants;
import com.adl.et.telco.crm.securerequesthandler.application.util.exception.BaseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import jakarta.servlet.http.HttpServletRequest;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@Slf4j
@Aspect
public class ApplicationLogAdviser {

    @SneakyThrows
    @Around("@annotation(org.springframework.web.bind.annotation.GetMapping) && args(.., request)")
    public Object logGetMethod(ProceedingJoinPoint jointPoint, HttpServletRequest request) {
        return logRequest(jointPoint, request);
    }

    @SneakyThrows
    @Around("@annotation(org.springframework.web.bind.annotation.PostMapping) && args(@org.springframework.web.bind.annotation.RequestBody body,.., request)")
    public Object logPostMethod(ProceedingJoinPoint jointPoint, HttpServletRequest request, Object body) {
        return logRequest(jointPoint, request);
    }

    @SneakyThrows
    @Around("@annotation(org.springframework.web.bind.annotation.PatchMapping) && args(@org.springframework.web.bind.annotation.RequestBody body,.., request)")
    public Object logPatchMethod(ProceedingJoinPoint jointPoint, HttpServletRequest request, Object body) {
        return logRequest(jointPoint, request);
    }

    @SneakyThrows
    private Object logRequest(ProceedingJoinPoint joinPoint, HttpServletRequest request) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        String className = methodSignature.getDeclaringTypeName();
        String methodName = methodSignature.getName();
        long start = System.currentTimeMillis();

        MDC.put(LoggingAdviceConstants.API_NAME, methodName);
        MDC.put(LoggingAdviceConstants.CLASS_NAME, className);
        MDC.put(LoggingAdviceConstants.METHOD_NAME, methodName);

        log.info(LoggingAdviceConstants.REQUEST_INITIATED, request.getMethod(), request.getRequestURI());
        log.debug(LoggingAdviceConstants.FULL_REQUEST, Arrays.toString(joinPoint.getArgs()));

        return logResponseAndHeader(joinPoint, start, className, methodName);
    }

    @SneakyThrows
    private Object logResponseAndHeader(ProceedingJoinPoint joinPoint, long start, String className, String methodName) {
        ObjectMapper mapper = new ObjectMapper();
        Object result = null;

        try {
            result = joinPoint.proceed();
            MDC.put(LoggingAdviceConstants.CLASS_NAME, className);
            MDC.put(LoggingAdviceConstants.METHOD_NAME, methodName);

            String response = mapper.writeValueAsString(result);
            long elapsedTime = System.currentTimeMillis() - start;

            String resultCode = "";
            String resultDescription = "";
            Integer statusCodeValue = null;

            resultCode = JsonPath.read(response, "$.body.code");
            resultDescription = JsonPath.read(response, "$.body.message");
            statusCodeValue = JsonPath.read(response, "$.statusCodeValue");

            log.info(LoggingAdviceConstants.REQUEST_TERMINATED, 
                    resultDescription, resultCode, statusCodeValue, elapsedTime);
            log.debug(LoggingAdviceConstants.FULL_RESPONSE, response);

            return result;
        } catch (BaseException ex) {
            handleBaseException(ex, className, methodName, start);
            throw ex;
        } catch (Exception ex) {
            handleGeneralException(ex, className, methodName, start);
            throw new BaseException(
                ex.getMessage(),
                ResponseCodeEnum.EXCEPTION_ADVISER_CONTROLLER.description(),
                HttpStatus.INTERNAL_SERVER_ERROR,
                ResponseCodeEnum.EXCEPTION_ADVISER_CONTROLLER.code(),
                ex.getStackTrace()
            );
        }
    }

    @SneakyThrows
    @Around("execution(* com.adl.et.telco.crm.securerequesthandler.service..*(..)))")
    public Object logServiceInfo(ProceedingJoinPoint proceedingJoinPoint) {
        String className = null;
        String methodName = null;
        long start = System.currentTimeMillis();

        try {
            MethodSignature methodSignature = (MethodSignature) proceedingJoinPoint.getSignature();
            className = methodSignature.getDeclaringTypeName();
            methodName = methodSignature.getName();

            MDC.put(LoggingAdviceConstants.CLASS_NAME, className);
            MDC.put(LoggingAdviceConstants.METHOD_NAME, methodName);

            log.debug(LoggingAdviceConstants.SERVICE_INITIATED, 
                    Arrays.toString(proceedingJoinPoint.getArgs()));

            Object object = proceedingJoinPoint.proceed();
            long elapsedTime = System.currentTimeMillis() - start;

            MDC.put(LoggingAdviceConstants.CLASS_NAME, className);
            MDC.put(LoggingAdviceConstants.METHOD_NAME, methodName);

            log.info(LoggingAdviceConstants.SERVICE_TERMINATED_INFO, elapsedTime);
            String response = new ObjectMapper().writeValueAsString(object);
            log.debug(LoggingAdviceConstants.SERVICE_TERMINATED, response, elapsedTime);

            return object;
        } catch (BaseException ex) {
            handleServiceBaseException(ex, className, methodName, start);
            throw ex;
        } catch (Exception ex) {
            handleServiceGeneralException(ex, className, methodName, start);
            throw new BaseException(
                ex.getMessage(),
                ResponseCodeEnum.EXCEPTION_ADVISER_SERVICE_LAYER.description(),
                HttpStatus.INTERNAL_SERVER_ERROR,
                ResponseCodeEnum.EXCEPTION_ADVISER_SERVICE_LAYER.code(),
                ex.getStackTrace()
            );
        }
    }

    @SneakyThrows
    @Around("execution(* com.adl.et.telco.crm.securerequesthandler.client..*(..)))")
    public Object logClientInfo(ProceedingJoinPoint proceedingJoinPoint) {
        String className = null;
        String methodName = null;
        long start = System.currentTimeMillis();

        try {
            MethodSignature methodSignature = (MethodSignature) proceedingJoinPoint.getSignature();
            className = methodSignature.getDeclaringTypeName();
            methodName = methodSignature.getName();

            MDC.put(LoggingAdviceConstants.CLASS_NAME, className);
            MDC.put(LoggingAdviceConstants.METHOD_NAME, methodName);

            log.debug(LoggingAdviceConstants.HTTP_CLIENT_INITIATED, 
                    Arrays.toString(proceedingJoinPoint.getArgs()));

            Object object = proceedingJoinPoint.proceed();
            long elapsedTime = System.currentTimeMillis() - start;

            MDC.put(LoggingAdviceConstants.CLASS_NAME, className);
            MDC.put(LoggingAdviceConstants.METHOD_NAME, methodName);

            log.info(LoggingAdviceConstants.HTTP_CLIENT_TERMINATED_INFO, elapsedTime);
            String response = new ObjectMapper().writeValueAsString(object);
            log.debug(LoggingAdviceConstants.HTTP_CLIENT_TERMINATED, response, elapsedTime);

            return object;
        } catch (BaseException ex) {
            handleClientBaseException(ex, className, methodName, start);
            throw ex;
        } catch (Exception ex) {
            handleClientGeneralException(ex, className, methodName, start);
            throw new BaseException(
                ex.getMessage(),
                ResponseCodeEnum.EXCEPTION_ADVISER_ADAPTER_INTEGRATION_LAYER.description(),
                HttpStatus.INTERNAL_SERVER_ERROR,
                ResponseCodeEnum.EXCEPTION_ADVISER_ADAPTER_INTEGRATION_LAYER.code(),
                ex.getStackTrace()
            );
        }
    }

    private void handleBaseException(BaseException ex, String className, String methodName, long start) {
        MDC.put(LoggingAdviceConstants.CLASS_NAME, className);
        MDC.put(LoggingAdviceConstants.METHOD_NAME, methodName);
        long elapsedTime = System.currentTimeMillis() - start;
        log.error(LoggingAdviceConstants.EXCEPTION_REQUEST_TERMINATED, 
                ex.getMessage(), ex.getReason(), ex.getResultCode(), 
                ex.getHttpStatus(), displayStackStraceArray(ex.getStackTraceElements()), 
                elapsedTime);
        log.debug(LoggingAdviceConstants.EXCEPTION_STACKTRACE, 
                Arrays.toString(ex.getStackTraceElements()));
    }

    private void handleGeneralException(Exception ex, String className, String methodName, long start) {
        MDC.put(LoggingAdviceConstants.CLASS_NAME, className);
        MDC.put(LoggingAdviceConstants.METHOD_NAME, methodName);
        long elapsedTime = System.currentTimeMillis() - start;
        log.error(LoggingAdviceConstants.EXCEPTION_REQUEST_TERMINATED, 
                ex.getMessage(), ResponseCodeEnum.EXCEPTION_ADVISER_CONTROLLER.description(), 
                ResponseCodeEnum.EXCEPTION_ADVISER_CONTROLLER.code(), 
                HttpStatus.INTERNAL_SERVER_ERROR, 
                displayStackStraceArray(ex.getStackTrace()), 
                elapsedTime);
        log.debug(LoggingAdviceConstants.EXCEPTION_STACKTRACE, 
                Arrays.toString(ex.getStackTrace()));
    }

    private void handleServiceBaseException(BaseException ex, String className, String methodName, long start) {
        MDC.put(LoggingAdviceConstants.CLASS_NAME, className);
        MDC.put(LoggingAdviceConstants.METHOD_NAME, methodName);
        long elapsedTime = System.currentTimeMillis() - start;
        log.error(LoggingAdviceConstants.EXCEPTION_SERVICE_TERMINATED, 
                ex.getMessage(), ex.getReason(), ex.getResultCode(), 
                ex.getHttpStatus(), elapsedTime);
    }

    private void handleServiceGeneralException(Exception ex, String className, String methodName, long start) {
        MDC.put(LoggingAdviceConstants.CLASS_NAME, className);
        MDC.put(LoggingAdviceConstants.METHOD_NAME, methodName);
        long elapsedTime = System.currentTimeMillis() - start;
        log.error(LoggingAdviceConstants.EXCEPTION_SERVICE_TERMINATED, 
                ex.getMessage(), ResponseCodeEnum.EXCEPTION_ADVISER_SERVICE_LAYER.description(), 
                ResponseCodeEnum.EXCEPTION_ADVISER_SERVICE_LAYER.code(), 
                HttpStatus.INTERNAL_SERVER_ERROR, elapsedTime);
    }

    private void handleClientBaseException(BaseException ex, String className, String methodName, long start) {
        MDC.put(LoggingAdviceConstants.CLASS_NAME, className);
        MDC.put(LoggingAdviceConstants.METHOD_NAME, methodName);
        long elapsedTime = System.currentTimeMillis() - start;
        log.error(LoggingAdviceConstants.EXCEPTION_HTTP_CLIENT_TERMINATED, 
                ex.getMessage(), ex.getReason(), ex.getResultCode(), 
                ex.getHttpStatus(), elapsedTime);
    }

    private void handleClientGeneralException(Exception ex, String className, String methodName, long start) {
        MDC.put(LoggingAdviceConstants.CLASS_NAME, className);
        MDC.put(LoggingAdviceConstants.METHOD_NAME, methodName);
        long elapsedTime = System.currentTimeMillis() - start;
        log.error(LoggingAdviceConstants.EXCEPTION_HTTP_CLIENT_TERMINATED, 
                ex.getMessage(), ResponseCodeEnum.EXCEPTION_ADVISER_ADAPTER_INTEGRATION_LAYER.description(), 
                ResponseCodeEnum.EXCEPTION_ADVISER_ADAPTER_INTEGRATION_LAYER.code(), 
                HttpStatus.INTERNAL_SERVER_ERROR, elapsedTime);
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
}
