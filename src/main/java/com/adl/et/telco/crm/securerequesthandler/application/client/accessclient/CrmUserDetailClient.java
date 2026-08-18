package com.adl.et.telco.crm.securerequesthandler.application.client.accessclient;

import com.adl.et.telco.crm.securerequesthandler.application.client.BaseClient;
import com.adl.et.telco.crm.securerequesthandler.application.util.constants.Constants;
import com.adl.et.telco.crm.securerequesthandler.domain.dto.authentication.AccessSystemDTO;
import com.adl.et.telco.crm.securerequesthandler.domain.dto.authentication.UserBasicInfo;
import com.adl.et.telco.crm.securerequesthandler.domain.dto.authentication.UserDetailsForToken;
import com.adl.et.telco.crm.securerequesthandler.domain.dto.common.CommonSouthBoundResponse;
import com.adl.et.telco.crm.securerequesthandler.domain.dto.security.UserLoginRequest;
import com.adl.et.telco.crm.securerequesthandler.application.util.exception.BaseException;
import com.adl.et.telco.crm.securerequesthandler.application.util.exception.ExceptionHandler;
import com.adl.et.telco.crm.securerequesthandler.application.util.resultenum.DisplayResultCodeEnum;
import com.adl.et.telco.crm.securerequesthandler.domain.dto.ums.groups.UserGroupDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;


@Component
public class CrmUserDetailClient extends BaseClient {
    @Value("${ums.get-user-details-for-email.url}")
    private String getUserDetailsByEmail;
    @Value("${ums.get-basic-info-for-token.url}")
    private String getGetUserDetailsForTokenUrl;
    @Value("${ums.get-user-name.url}")
    private String getUserNameUrl;
    @Value("${ums.get-user-access-system}")
    private String getUserAccessSystemUrl;

    @Value("${ums.get-user-details}")
    private String getUserDetailsUrl;

    @Value("${ums.get-user-groups}")
    private String getUserGroupsUrl;

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private ExceptionHandler exceptionHandler;


    public CommonSouthBoundResponse<UserDetailsForToken> getUserDetails(String userName, String productId) throws BaseException {
        try {
            ParameterizedTypeReference<CommonSouthBoundResponse<UserDetailsForToken>> typeRef = new ParameterizedTypeReference<CommonSouthBoundResponse<UserDetailsForToken>>() {
            };
            HttpHeaders headers = populateHeadersWithTenant();
            headers.set(Constants.APPLICATION_ID, productId);

            HttpEntity<String> requestEntity = new HttpEntity<>(null, headers);

            ResponseEntity<CommonSouthBoundResponse<UserDetailsForToken>> exchange = restTemplate.exchange(getGetUserDetailsForTokenUrl, HttpMethod.GET, requestEntity, typeRef, userName);
            return exchange.getBody();

        } catch (Exception ex) {
            throw exceptionHandler.clientExceptionHandler(ex, DisplayResultCodeEnum.GET_USER_BASIC_INFO_FAILED);
        }
    }

    public CommonSouthBoundResponse<String> getUserName(UserLoginRequest userLoginRequest) throws BaseException {
        try {
            ParameterizedTypeReference<CommonSouthBoundResponse<String>> typeRef = new ParameterizedTypeReference<CommonSouthBoundResponse<String>>() {
            };
            ResponseEntity<CommonSouthBoundResponse<String>> exchange = restTemplate.exchange(getUserNameUrl, HttpMethod.POST, populateRequestEntity(userLoginRequest), typeRef);
            return exchange.getBody();

        } catch (Exception ex) {
            throw exceptionHandler.clientExceptionHandler(ex, DisplayResultCodeEnum.GET_USER_NAME_FAILED);
        }
    }

    public CommonSouthBoundResponse<UserBasicInfo> getBasicUserDetails(String email) throws BaseException {
        try {

            ParameterizedTypeReference<CommonSouthBoundResponse<UserBasicInfo>> typeRef = new ParameterizedTypeReference<CommonSouthBoundResponse<UserBasicInfo>>() {
            };
            ResponseEntity<CommonSouthBoundResponse<UserBasicInfo>> exchange = restTemplate.exchange(getUserDetailsByEmail, HttpMethod.GET, populateRequestEntity(), typeRef, email);
            return exchange.getBody();
        } catch (Exception ex) {
            throw exceptionHandler.clientExceptionHandler(ex, DisplayResultCodeEnum.GET_USER_BASIC_INFO_FAILED);
        }
    }

    public CommonSouthBoundResponse<List<Integer>> getUserAccessSystem(String userId) throws BaseException {
        try {
            ParameterizedTypeReference<CommonSouthBoundResponse<?>> typeRef = new ParameterizedTypeReference<CommonSouthBoundResponse<?>>() {
            };
            String url = getUserAccessSystemUrl + "/" + userId;
            ResponseEntity<CommonSouthBoundResponse<?>> exchange = restTemplate.exchange(url, HttpMethod.GET, populateRequestEntity(), typeRef, userId);
            return (CommonSouthBoundResponse<List<Integer>>) exchange.getBody();
        } catch (Exception ex) {
            throw exceptionHandler.clientExceptionHandler(ex, DisplayResultCodeEnum.GET_USER_BASIC_INFO_FAILED);
        }
    }

    public CommonSouthBoundResponse<UserBasicInfo> getUserDetailByUserName(String username) throws BaseException {
        try {
            ParameterizedTypeReference<CommonSouthBoundResponse<UserBasicInfo>> typeRef = new ParameterizedTypeReference<CommonSouthBoundResponse<UserBasicInfo>>() {
            };
            String url = getUserDetailsUrl + "/" + username;
            ResponseEntity<CommonSouthBoundResponse<UserBasicInfo>> exchange = restTemplate.exchange(url, HttpMethod.GET, populateRequestEntity(), typeRef, username);
            return exchange.getBody();
        } catch (Exception ex) {
            throw exceptionHandler.clientExceptionHandler(ex, DisplayResultCodeEnum.GET_USER_BASIC_INFO_FAILED);
        }
    }

    public CommonSouthBoundResponse<UserGroupDto> getUserGroupDetails(String userName, String productId) throws BaseException {
        try {
            String url = getUserGroupsUrl + "/" + userName;
            ParameterizedTypeReference<CommonSouthBoundResponse<UserGroupDto>> typeRef = new ParameterizedTypeReference<CommonSouthBoundResponse<UserGroupDto>>() {};
            HttpHeaders headers = populateHeadersWithTenant();
            headers.set(Constants.APPLICATION_ID, productId);
            HttpEntity<String> requestEntity = new HttpEntity<>(null, headers);

            ResponseEntity<CommonSouthBoundResponse<UserGroupDto>> exchange = restTemplate.exchange(url, HttpMethod.GET, requestEntity, typeRef, userName);
            return exchange.getBody();
        } catch (Exception ex) {
            throw exceptionHandler.clientExceptionHandler(ex, DisplayResultCodeEnum.GET_USER_BASIC_INFO_FAILED);
        }
    }

}
