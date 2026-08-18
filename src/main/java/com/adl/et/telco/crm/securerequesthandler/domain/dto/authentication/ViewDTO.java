package com.adl.et.telco.crm.securerequesthandler.domain.dto.authentication;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class ViewDTO {
    private Long id;
    private List<Long> actions;
    private List<Long> attributes;
    private List<Long> controllableAttributes;
    private ProductDetail productDetail;
}
