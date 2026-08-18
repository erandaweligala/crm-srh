package com.adl.et.telco.crm.securerequesthandler.domain.dto.security;

import lombok.Data;

@Data
public class TokenEnhancementRequest {
    private String tenant;
    private String clientId;
    private Integer loginSystemId;
}
