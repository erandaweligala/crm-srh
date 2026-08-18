package com.adl.et.telco.crm.securerequesthandler.domain.dto.security;

import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class TokenResponse {
    private String tempToken;
    private String requestVerificationToken;
}
