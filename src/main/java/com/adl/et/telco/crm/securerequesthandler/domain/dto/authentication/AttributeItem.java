package com.adl.et.telco.crm.securerequesthandler.domain.dto.authentication;

import lombok.*;

@Setter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class AttributeItem {
    private Integer id;
    private String path;
}
