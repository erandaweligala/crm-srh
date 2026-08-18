package com.adl.et.telco.crm.securerequesthandler.application.util.constants;

public class LoggingAdviceConstants {

    private LoggingAdviceConstants(){}

    public static final String API_NAME = "API-Name";
    public static final String CLASS_NAME = "Class-Name";
    public static final String METHOD_NAME = "Method-Name";
    public static final String REQUEST_ID = "Request-Id";
    public static final String SOURCE_ID = "Source-Id";
    public static final String PACKAGE_ROOT = "com.adl.et.telco.crm.securerequesthandler";
    public static final String REQUEST_INITIATED = "SRH_REQUEST_INITIATED | REQUEST_METHOD : {} | REQUEST_URI : {}";
    public static final String FULL_REQUEST = "SRH_FULL_REQUEST | CONTROLLER_REQUEST : {}";
    public static final String FULL_RESPONSE = "SRH_FULL_RESPONSE | CONTROLLER_RESPONSE : {}";
    public static final String CODE_MESSAGE_NOT_FOUND = "SRH_WARNING | CODE_MESSAGE_NOT_FOUND : {}";
    public static final String REQUEST_TERMINATED = "SRH_REQUEST_TERMINATED | RESPONSE_MESSAGE : {} | RESPONSE_CODE : {} | HTTP_STATUS : {} | RESPONSE_TIME : {} ms";
    public static final String EXCEPTION_REQUEST_TERMINATED = "SRH_EXCEPTION | REQUEST_TERMINATED | ERROR_MESSAGE : {} | ERROR_REASON : {} | ERROR_CODE : {} | HTTP_STATUS : {} | ERROR_STACKTRACE : {} | RESPONSE_TIME : {} ms";
    public static final String EXCEPTION_STACKTRACE = "SRH_EXCEPTION | STACKTRACE : {}";
    public static final String SERVICE_INITIATED = "SRH_SERVICE_INITIATED | REQUEST_ARGS : {}";
    public static final String SERVICE_TERMINATED = "SRH_SERVICE_TERMINATED | RESPONSE_BODY : {} | RESPONSE_TIME : {} ms";
    public static final String SERVICE_TERMINATED_INFO = "SRH_SERVICE_TERMINATED | RESPONSE_TIME : {} ms";
    public static final String EXCEPTION_SERVICE_TERMINATED = "SRH_EXCEPTION | SERVICE_TERMINATED | ERROR_MESSAGE : {} | ERROR_REASON : {} | ERROR_CODE : {} | HTTP_STATUS : {} | RESPONSE_TIME : {} ms";
    public static final String HTTP_CLIENT_INITIATED = "SRH_HTTP_CLIENT_INITIATED | REQUEST_ARGS : {}";
    public static final String HTTP_CLIENT_TERMINATED = "SRH_HTTP_CLIENT_TERMINATED | RESPONSE_BODY : {} | RESPONSE_TIME : {} ms";
    public static final String HTTP_CLIENT_TERMINATED_INFO = "SRH_HTTP_CLIENT_TERMINATED | RESPONSE_TIME : {} ms";
    public static final String EXCEPTION_HTTP_CLIENT_TERMINATED = "SRH_EXCEPTION | HTTP_CLIENT_TERMINATED | ERROR_MESSAGE : {} | ERROR_REASON : {} | ERROR_CODE : {} | HTTP_STATUS : {} | RESPONSE_TIME : {} ms";
}
