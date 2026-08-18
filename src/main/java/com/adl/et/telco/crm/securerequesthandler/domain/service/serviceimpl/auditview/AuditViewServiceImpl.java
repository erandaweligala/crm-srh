package com.adl.et.telco.crm.securerequesthandler.domain.service.serviceimpl.auditview;

import com.adl.et.telco.crm.securerequesthandler.application.client.auditview.AuditViewClient;
import com.adl.et.telco.crm.securerequesthandler.domain.dto.auditview.*;
import com.adl.et.telco.crm.securerequesthandler.domain.dto.common.CommonNorthBoundResponse;
import com.adl.et.telco.crm.securerequesthandler.domain.dto.common.CommonSouthBoundResponse;
import com.adl.et.telco.crm.securerequesthandler.domain.service.serviceinterface.auditview.AuditService;
import com.adl.et.telco.crm.securerequesthandler.application.util.ResponseHandler;
import com.adl.et.telco.crm.securerequesthandler.application.util.constants.AuditSubjectType;
import com.adl.et.telco.crm.securerequesthandler.application.util.exception.BaseException;
import com.adl.et.telco.crm.securerequesthandler.application.util.exception.ExceptionHandler;
import com.adl.et.telco.crm.securerequesthandler.application.util.resultenum.DisplayResultCodeEnum;
import com.adl.et.telco.crm.securerequesthandler.application.util.resultenum.ResponseCodeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class AuditViewServiceImpl implements AuditService {

    private static final Logger logger = LoggerFactory.getLogger(AuditViewServiceImpl.class);

    @Autowired
    private AuditViewClient auditViewClient;

    @Autowired
    private ResponseHandler handler;
    @Autowired
    private ExceptionHandler exceptionHandler;

    @Override
    public CommonNorthBoundResponse<List<ActionLogResponseDTO>> getActionAudits(ActionLogPramDTO actionLogPramDTO) throws BaseException {
        logger.debug("Getting action audits for parameters: {}", actionLogPramDTO);
        try {
            CommonSouthBoundResponse<List<ActionLogResponseDTO>> overview = auditViewClient.getAuditDetails(actionLogPramDTO);
            logger.info("Successfully retrieved {} audit records", overview.getResponseData().size());
            return handler.responseBuilderWithPageInformation(overview.getResponseData(), overview.getResult().getResultDescription(),
                    overview.getResult().getResultCode(), overview.getResult().getPageDetail());
        } catch (Exception e) {
            logger.error("Failed to get action audits: {}", e.getMessage(), e);
            throw exceptionHandler.serviceExceptionHandler(e, DisplayResultCodeEnum.GET_AUDIT_DETAILS_FAILED);
        }
    }

    @Override
    public CommonNorthBoundResponse<List<SearchTypeResponseDTO>> getSearchTypes() throws BaseException {
        logger.debug("Getting search types");
        try {
            List<String> subjects = AuditSubjectType.getAllSubjects();
            List<SearchTypeResponseDTO> dtoLists = subjects.stream().map(s -> SearchTypeResponseDTO.builder().id(s).name(s).build()).collect(Collectors.toList());
            logger.info("Successfully retrieved {} search types", dtoLists.size());
            return handler.responseBuilder(dtoLists, ResponseCodeEnum.SUCCESSFUL.description(), ResponseCodeEnum.SUCCESSFUL.code());
        } catch (Exception e) {
            logger.error("Failed to get search types: {}", e.getMessage(), e);
            throw exceptionHandler.serviceExceptionHandler(e, DisplayResultCodeEnum.GET_AUDIT_DETAILS_FAILED);
        }
    }

    @Override
    public CommonNorthBoundResponse<List<StatusCodeResponseDTO>> getStatusCodes() throws BaseException {
        logger.debug("Getting status codes");
        try {
            StatusCodeResponseDTO s1 = new StatusCodeResponseDTO();
            s1.setId(1);
            s1.setName("SUCCESS");
            StatusCodeResponseDTO s2 = new StatusCodeResponseDTO();
            s2.setId(2);
            s2.setName("FAILED");

            StatusCodeResponseDTO s3 = new StatusCodeResponseDTO();
            s3.setId(3);
            s3.setName("PENDING");

            List<StatusCodeResponseDTO> dtoLists = new ArrayList<>();
            dtoLists.add(s1);
            dtoLists.add(s2);
            dtoLists.add(s3);
            logger.info("Successfully retrieved {} status codes", dtoLists.size());
            return handler.responseBuilder(dtoLists, ResponseCodeEnum.SUCCESSFUL.description(), ResponseCodeEnum.SUCCESSFUL.code());
        } catch (Exception e) {
            logger.error("Failed to get status codes: {}", e.getMessage(), e);
            throw exceptionHandler.serviceExceptionHandler(e, DisplayResultCodeEnum.GET_AUDIT_DETAILS_FAILED);
        }
    }

    @Override
    public CommonNorthBoundResponse<List<UserNameResponseDTO>> getUserNames() throws BaseException {
        logger.debug("Getting user names");
        try {
            List<Object[]> response = auditViewClient.getUserNames().getResponseData();
            List<UserNameResponseDTO> dtoLists = new ArrayList<>();
            if (response != null && !response.isEmpty()) {
                AtomicInteger counter = new AtomicInteger(0);
                dtoLists = response.stream().map(e -> UserNameResponseDTO.builder().id(counter.getAndIncrement()).name((String) e[0]).email((String) e[1]).build()).collect(Collectors.toList());
            }
            logger.info("Successfully retrieved {} user names", dtoLists.size());
            return handler.responseBuilder(dtoLists, ResponseCodeEnum.SUCCESSFUL.description(), ResponseCodeEnum.SUCCESSFUL.code());
        } catch (Exception e) {
            logger.error("Failed to get user names: {}", e.getMessage(), e);
            throw exceptionHandler.serviceExceptionHandler(e, DisplayResultCodeEnum.GET_AUDIT_DETAILS_FAILED);
        }
    }

    @Override
    public CommonNorthBoundResponse<List<ActivityCodesResponseDTO>> getActivities() throws BaseException {
        logger.debug("Getting activity codes");
        try {
            List<ActivityCodesResponseDTO> activities = auditViewClient.getActivityCodes().getResponseData();
            logger.info("Successfully retrieved {} activity codes", activities.size());
            return handler.responseBuilder(activities, ResponseCodeEnum.SUCCESSFUL.description(), ResponseCodeEnum.SUCCESSFUL.code());
        } catch (Exception e) {
            logger.error("Failed to get activity codes: {}", e.getMessage(), e);
            throw exceptionHandler.serviceExceptionHandler(e, DisplayResultCodeEnum.GET_AUDIT_DETAILS_FAILED);
        }
    }
}
