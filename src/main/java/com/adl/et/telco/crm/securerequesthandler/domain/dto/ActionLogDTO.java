package com.adl.et.telco.crm.securerequesthandler.domain.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ActionLogDTO {
    private String auditAction;
    private String id;
    private String activityId;
    private int actionId;
    private String createdDate;
    private String completedDate;
    private String status;
    private String statusCode;
    private String statusDescription;
    private String subjectType;
    private String subjectValue;
    private String userName;
    private String clientIp;
    private int tenantId;
}
