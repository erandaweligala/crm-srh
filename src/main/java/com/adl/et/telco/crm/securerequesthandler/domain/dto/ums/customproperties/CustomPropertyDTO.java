package com.adl.et.telco.crm.securerequesthandler.domain.dto.ums.customproperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CustomPropertyDTO {
    Long propertyId;
    String propertyName;
    Long valueId;
    String valueName;
}
