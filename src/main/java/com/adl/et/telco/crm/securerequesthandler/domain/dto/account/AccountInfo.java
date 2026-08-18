package com.adl.et.telco.crm.securerequesthandler.domain.dto.account;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountInfo {
    private String accountId;
    private String accountType;
    private String accountStatus;
    private String paymentStatus;
    private String lastModifiedDate;
    private String defaultPaymentMethod;
    private String ratingType;
    private String billingAddress;
    private String mainBalance;
}
