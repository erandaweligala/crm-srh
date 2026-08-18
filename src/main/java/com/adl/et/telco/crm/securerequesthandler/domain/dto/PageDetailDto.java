package com.adl.et.telco.crm.securerequesthandler.domain.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PageDetailDto {
    private int pageNumber;
    private int pageElementCount;
    private long totalRecords;

}
