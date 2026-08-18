package com.adl.et.telco.crm.securerequesthandler.domain.dto.account;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class BalanceInstance {
    private String amount;
    private String effectiveTime;
    private String expireTime;
    private String instanceId;
    private String initialAmount;
    private String offeringName;
}
