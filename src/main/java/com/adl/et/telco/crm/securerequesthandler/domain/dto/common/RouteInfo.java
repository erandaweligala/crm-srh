package com.adl.et.telco.crm.securerequesthandler.domain.dto.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RouteInfo {
    private Long id;
    private String inPath;
    private String outURL;
    private Boolean isPathVariableAvailable;
}
