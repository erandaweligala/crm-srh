package com.adl.et.telco.crm.securerequesthandler.domain.dto.ums.customproperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Setter
@Getter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CustomPropertiesItem {
    private String propertyId;
    private String propertyName;
    private List<CustomPropertiesItemValues> values;
}