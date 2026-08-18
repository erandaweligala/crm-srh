package com.adl.et.telco.crm.securerequesthandler.domain.dto.ums.customproperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CustomPropertiesItemValues {
    private String valueId;
    private String valueName;
}
