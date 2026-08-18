package com.adl.et.telco.crm.securerequesthandler.domain.dto.common;

import com.adl.et.telco.crm.securerequesthandler.domain.dto.PageDetailDto;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Result {
    private String resultCode;
    private String resultDescription;
    private PageDetailDto pageDetail;
}
