package com.adl.et.telco.crm.securerequesthandler.domain.dto.account;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class BillingDetails {
    private String billFormat;
    private List<String> billPresentation;
    private String cycleStartDate;
    private String cycleEndDate;
    private String paymentDueDate;
    private String billingPeriod;
}
