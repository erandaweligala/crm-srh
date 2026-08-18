package com.adl.et.telco.crm.securerequesthandler.application.client.auditview;

import com.adl.et.telco.crm.securerequesthandler.application.client.BaseClient;
import com.adl.et.telco.crm.securerequesthandler.domain.dto.ActionLogDTO;
import com.adl.et.telco.crm.securerequesthandler.domain.dto.auditview.ActionLogPramDTO;
import com.adl.et.telco.crm.securerequesthandler.domain.dto.auditview.ActionLogResponseDTO;
import com.adl.et.telco.crm.securerequesthandler.domain.dto.auditview.ActivityCodesResponseDTO;
import com.adl.et.telco.crm.securerequesthandler.domain.dto.common.CommonSouthBoundResponse;
import com.adl.et.telco.crm.securerequesthandler.application.util.exception.BaseException;
import com.adl.et.telco.crm.securerequesthandler.application.util.exception.ExceptionHandler;
import com.adl.et.telco.crm.securerequesthandler.application.util.resultenum.DisplayResultCodeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
public class AuditViewClient extends BaseClient {

    private static final Logger logger = LoggerFactory.getLogger(AuditViewClient.class);

    @Autowired
    private RestTemplate restTemplate;
    @Value("${audit.get-action-audits}")
    private String auditActionViewUrl;
    @Value("${audit.get-action-audits}")
    private String getSearchTypeURL;

    @Value("${ums.get-activity-list}")
    private String getActivityListURL;

    @Value("${ums.get-user-names}")
    private String getUserNamesURL;

    @Value("${audit.get-action-audits}")
    private String getResponseCodeURL;

    @Value("${audit.send-audits}")
    private String sendActionLogsURL;


    @Autowired
    private ExceptionHandler exceptionHandler;


    public CommonSouthBoundResponse<List<ActionLogResponseDTO>> getAuditDetails(ActionLogPramDTO actionLogPramDTO) throws BaseException {
        try {
            ParameterizedTypeReference<CommonSouthBoundResponse<List<ActionLogResponseDTO>>> typeRef = new ParameterizedTypeReference<CommonSouthBoundResponse<List<ActionLogResponseDTO>>>() {
            };
            logger.info("invoking action audit url  {}", auditActionViewUrl);
            ResponseEntity<CommonSouthBoundResponse<List<ActionLogResponseDTO>>> exchange = restTemplate.exchange(auditActionViewUrl, HttpMethod.POST, populateRequestEntity(actionLogPramDTO), typeRef);
            return exchange.getBody();
        } catch (Exception ex) {
            throw exceptionHandler.clientExceptionHandler(ex, DisplayResultCodeEnum.GET_AUDIT_DETAILS_FAILED);
        }
    }


    public CommonSouthBoundResponse<List<ActivityCodesResponseDTO>> getActivityCodes() throws BaseException {
        try {
            ParameterizedTypeReference<CommonSouthBoundResponse<List<ActivityCodesResponseDTO>>> typeRef = new ParameterizedTypeReference<CommonSouthBoundResponse<List<ActivityCodesResponseDTO>>>() {
            };
            ResponseEntity<CommonSouthBoundResponse<List<ActivityCodesResponseDTO>>> exchange = restTemplate.exchange(getActivityListURL, HttpMethod.GET, populateRequestEntity(), typeRef);
            return exchange.getBody();
        } catch (Exception ex) {
            throw exceptionHandler.clientExceptionHandler(ex, DisplayResultCodeEnum.GET_AUDIT_DETAILS_FAILED);
        }
    }


    public CommonSouthBoundResponse<List<Object[]>> getUserNames() throws BaseException {
        try {

            ParameterizedTypeReference<CommonSouthBoundResponse<List<Object[]>>> typeRef = new ParameterizedTypeReference<CommonSouthBoundResponse<List<Object[]>>>() {
            };
            ResponseEntity<CommonSouthBoundResponse<List<Object[]>>> exchange = restTemplate.exchange(getUserNamesURL, HttpMethod.GET, populateRequestEntity(), typeRef);
            return exchange.getBody();
        } catch (Exception ex) {
            throw exceptionHandler.clientExceptionHandler(ex, DisplayResultCodeEnum.GET_AUDIT_DETAILS_FAILED);
        }
    }

    public CommonSouthBoundResponse<String> sendActionLogs(ActionLogDTO actionLogDTO) throws BaseException {
        try {

            ParameterizedTypeReference<CommonSouthBoundResponse<String>> typeRef = new ParameterizedTypeReference<CommonSouthBoundResponse<String>>() {
            };
            ResponseEntity<CommonSouthBoundResponse<String>> exchange = restTemplate.exchange(sendActionLogsURL, HttpMethod.POST, populateRequestEntity(actionLogDTO), typeRef);
            return exchange.getBody();
        } catch (Exception ex) {
            throw exceptionHandler.clientExceptionHandler(ex, DisplayResultCodeEnum.GET_AUDIT_DETAILS_FAILED);
        }
    }
}
