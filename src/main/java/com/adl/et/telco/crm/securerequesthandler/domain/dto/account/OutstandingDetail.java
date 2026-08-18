package com.adl.et.telco.crm.securerequesthandler.domain.dto.account;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class OutstandingDetail {
    private String billCycle;
    private String billCycleStartTime;
    private String billCycleEndTime;
    private String dueDate;
    private String outstandingAmount;
}
