package com.adl.et.telco.crm.securerequesthandler.domain.dto.security.otp;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder
public class OTPValidateRequest {
    private String msisdn;
    private String otp;
}
