package com.adl.et.telco.crm.securerequesthandler.domain.service.serviceinterface.access;

import com.adl.et.telco.crm.securerequesthandler.domain.dto.common.CommonNorthBoundResponse;
import com.adl.et.telco.crm.securerequesthandler.domain.dto.security.AccessTokenRequest;
import com.adl.et.telco.crm.securerequesthandler.domain.dto.security.AccessTokenResponse;
import com.adl.et.telco.crm.securerequesthandler.domain.dto.security.TempTokenRequest;
import com.adl.et.telco.crm.securerequesthandler.domain.dto.security.TempTokenResponse;
import com.adl.et.telco.crm.securerequesthandler.application.util.exception.BaseException;
import jakarta.servlet.http.HttpServletRequest;


public interface ExternalUserAuthenticationServiceInterface {
    CommonNorthBoundResponse<TempTokenResponse> createTempToken(TempTokenRequest tempTokenRequest)throws BaseException;
    CommonNorthBoundResponse<AccessTokenResponse> createAccessToken(AccessTokenRequest accessTokenRequest, HttpServletRequest httpServletRequest)throws BaseException;
}
