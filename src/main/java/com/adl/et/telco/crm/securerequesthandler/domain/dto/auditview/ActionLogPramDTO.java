package com.adl.et.telco.crm.securerequesthandler.domain.dto.auditview;

import com.adl.et.telco.crm.securerequesthandler.application.util.resultenum.AuditRecordsDataSortingEnum;
import com.adl.et.telco.crm.securerequesthandler.application.util.resultenum.SortingDirectionEnum;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ActionLogPramDTO {
    private int offset;
    private int itemsPerPage;
    private String fromDate;
    private String toDate;
    private String subjectTypeValue;
    private String subjectValue;
    private String status;
    private String statusId;
    private String userId;
    private String userName;
    private String transactionId;
    private String activityId;
    private String activityValue;
    private AuditRecordsDataSortingEnum sortBy;
    private SortingDirectionEnum sortOrder;
}
