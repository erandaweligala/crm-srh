package com.adl.et.telco.crm.securerequesthandler.domain.service.serviceimpl.access;


import com.adl.et.telco.crm.securerequesthandler.domain.dto.common.CommonNorthBoundResponse;
import com.adl.et.telco.crm.securerequesthandler.domain.dto.security.SamlRequest;
import com.adl.et.telco.crm.securerequesthandler.application.util.exception.BaseException;

import java.io.IOException;

public interface UserAuthenticationManageService {
    CommonNorthBoundResponse<SamlRequest> createSamlRequest(String uuid) throws BaseException, IOException;

}
