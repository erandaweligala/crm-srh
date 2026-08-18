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
public class AccountBalance {
    private String id;
    private String type;
    private String name;
    private String totalAmount;
    private String reservedAmount;
    private List<BalanceInstance> instances;
}
