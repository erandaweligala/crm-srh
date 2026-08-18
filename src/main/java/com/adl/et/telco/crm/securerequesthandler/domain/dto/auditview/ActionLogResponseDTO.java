package com.adl.et.telco.crm.securerequesthandler.domain.dto.auditview;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ActionLogResponseDTO {
    private Long id;
    private String transactionId;
    private String activity;
    private String dateAndTime;
    private String status;
    private String statusDescription;
    private String subjectType;
    private String subjectValue;
    private String userName;
    private String clientIp;
}
