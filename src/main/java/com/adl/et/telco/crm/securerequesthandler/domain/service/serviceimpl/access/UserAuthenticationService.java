package com.adl.et.telco.crm.securerequesthandler.domain.service.serviceimpl.access;


import com.adl.et.telco.crm.securerequesthandler.domain.dto.common.CommonNorthBoundResponse;
import com.adl.et.telco.crm.securerequesthandler.domain.dto.security.AccessTokenResponse;
import com.adl.et.telco.crm.securerequesthandler.domain.dto.security.TokenEnhancementRequest;
import com.adl.et.telco.crm.securerequesthandler.domain.dto.security.UserLoginRequest;
import com.adl.et.telco.crm.securerequesthandler.application.util.exception.BaseException;
import jakarta.servlet.http.HttpServletRequest;


public interface UserAuthenticationService {
    CommonNorthBoundResponse<AccessTokenResponse> login(String requestVerificationToken, UserLoginRequest tempToken, String productType) throws BaseException;

    CommonNorthBoundResponse<AccessTokenResponse> newAccessToken(String requestVerificationToken, HttpServletRequest request, String productType) throws BaseException;

    CommonNorthBoundResponse<String> userLogOut(HttpServletRequest httpServletRequest);

    CommonNorthBoundResponse<AccessTokenResponse> enhanceAccessToken(TokenEnhancementRequest request, String rvToken, HttpServletRequest httpServletRequest, String productType);
}
