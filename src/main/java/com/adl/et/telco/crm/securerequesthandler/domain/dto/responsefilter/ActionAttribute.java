package com.adl.et.telco.crm.securerequesthandler.domain.dto.responsefilter;

import lombok.*;

import java.util.Map;

@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActionAttribute {
    Integer actionId;
    Map<Integer,String> attributeList;
}
