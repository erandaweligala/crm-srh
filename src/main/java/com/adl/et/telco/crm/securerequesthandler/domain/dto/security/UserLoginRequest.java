package com.adl.et.telco.crm.securerequesthandler.domain.dto.security;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class UserLoginRequest {
    //private String tempToken;
    private String code;
    private String tenant;
    private String clientId;
    private String redirectUri;
}
