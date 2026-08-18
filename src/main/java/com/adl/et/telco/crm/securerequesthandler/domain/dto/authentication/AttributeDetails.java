package com.adl.et.telco.crm.securerequesthandler.domain.dto.authentication;

import lombok.*;

import java.util.List;

@Setter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class AttributeDetails {
    private Integer actionId;
    private List<AttributeItem> attributeList;
}
