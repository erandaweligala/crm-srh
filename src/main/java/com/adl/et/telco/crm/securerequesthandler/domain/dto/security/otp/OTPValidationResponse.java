package com.adl.et.telco.crm.securerequesthandler.domain.dto.security.otp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class OTPValidationResponse {
    private String msisdn;
    private String reason;
    private String esbuuid;
}
