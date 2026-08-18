package com.adl.et.telco.crm.securerequesthandler.domain.dto.account;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomerBillItem {

    @JsonProperty("nextBillDate")
    private String nextBillDate;

    @JsonProperty("amountDue")
    private String amountDue;

    @JsonProperty("paymentDueDate")
    private String paymentDueDate;

    @JsonProperty("billDocument")
    private String billDocument;

    @JsonProperty("billingPeriod")
    private String billingPeriod;

    @JsonProperty("runType")
    private String runType;

    @JsonProperty("billDate")
    private String billDate;

    @JsonProperty("id")
    private String id;

    @JsonProperty("state")
    private String state;

    @JsonProperty("category")
    private String category;

    @JsonProperty("billNo")
    private String billNo;
}