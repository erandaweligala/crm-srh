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
public class AccountCredit {
    private String id;
    private String creditLimitType;
    private String creditLimitName;
    private String totalCreditAmount;
    private String totalUsageAmount;
    private String totalRemainingAmount;
    private List<CreditInstance> instances;
}
