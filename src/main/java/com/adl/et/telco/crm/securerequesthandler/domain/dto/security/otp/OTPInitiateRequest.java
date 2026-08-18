package com.adl.et.telco.crm.securerequesthandler.domain.dto.security.otp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@ToString
@Builder
public class OTPInitiateRequest {
    private String msisdn;
    private Integer length;
    private boolean useLetter;
    private boolean useNumber;
    private boolean allCapital;
    private String transport;
    private Integer validityInSecond;
}
