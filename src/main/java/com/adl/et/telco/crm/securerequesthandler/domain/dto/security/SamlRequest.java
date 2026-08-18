package com.adl.et.telco.crm.securerequesthandler.domain.dto.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SamlRequest {
    private String url;
}
