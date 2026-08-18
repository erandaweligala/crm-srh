package com.adl.et.telco.crm.securerequesthandler.domain.service.serviceinterface.auditview;


import com.adl.et.telco.crm.securerequesthandler.domain.dto.auditview.*;
import com.adl.et.telco.crm.securerequesthandler.domain.dto.common.CommonNorthBoundResponse;
import com.adl.et.telco.crm.securerequesthandler.application.util.exception.BaseException;

import java.util.List;

public interface AuditService {

    CommonNorthBoundResponse<List<ActionLogResponseDTO>> getActionAudits(ActionLogPramDTO actionLogPramDTO) throws BaseException;
    CommonNorthBoundResponse<List<SearchTypeResponseDTO>> getSearchTypes() throws BaseException;
    CommonNorthBoundResponse<List<StatusCodeResponseDTO>> getStatusCodes() throws BaseException;
    CommonNorthBoundResponse<List<UserNameResponseDTO>> getUserNames() throws BaseException;
    CommonNorthBoundResponse<List<ActivityCodesResponseDTO>> getActivities() throws BaseException;


}
