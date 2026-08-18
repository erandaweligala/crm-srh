package com.adl.et.telco.crm.securerequesthandler.domain.dto.account;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentPlan {
    private String id;
    private String numberOfPayment;
    private String paymentFrequency;
    private String priority;
    private String paymentMethod;
    private String totalAmount;
    private String startDate;
    private String endDate;
    private String status;
}
