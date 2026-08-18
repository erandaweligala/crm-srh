package com.adl.et.telco.crm.securerequesthandler.application.client.accessclient;

import com.adl.et.telco.crm.securerequesthandler.application.client.BaseClient;
import com.adl.et.telco.crm.securerequesthandler.domain.dto.authentication.UserDetailsForToken;
import com.adl.et.telco.crm.securerequesthandler.domain.dto.common.CommonSouthBoundResponse;
import com.adl.et.telco.crm.securerequesthandler.application.util.exception.BaseException;
import com.adl.et.telco.crm.securerequesthandler.application.util.exception.ExceptionHandler;
import com.adl.et.telco.crm.securerequesthandler.application.util.resultenum.DisplayResultCodeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;


@Component
public class CpqUserDetailClient extends BaseClient {

    @Value("${ums.cpq.get-basic-info-for-token.url}")
    private String getGetUserDetailsForTokenUrl;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ExceptionHandler exceptionHandler;

    public CommonSouthBoundResponse<UserDetailsForToken> getUserDetails(String userName) throws BaseException {
        try {
            ParameterizedTypeReference<CommonSouthBoundResponse<UserDetailsForToken>> typeRef = new ParameterizedTypeReference<CommonSouthBoundResponse<UserDetailsForToken>>() {};
            ResponseEntity<CommonSouthBoundResponse<UserDetailsForToken>> exchange = restTemplate.exchange(getGetUserDetailsForTokenUrl, HttpMethod.GET, populateRequestEntity(), typeRef, userName);
            return exchange.getBody();
        } catch (Exception ex) {
            throw exceptionHandler.clientExceptionHandler(ex, DisplayResultCodeEnum.GET_CPQ_USER_BASIC_INFO_FAILED);
        }
    }

}
