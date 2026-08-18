package com.adl.et.telco.crm.securerequesthandler.domain.dto.security;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class TempTokenRequest {
    private String username;
    private String password;
}
