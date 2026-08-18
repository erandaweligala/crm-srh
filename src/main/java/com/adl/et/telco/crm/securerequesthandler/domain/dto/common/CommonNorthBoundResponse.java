package com.adl.et.telco.crm.securerequesthandler.domain.dto.common;

import com.adl.et.telco.crm.securerequesthandler.domain.dto.PageDetailDto;
import lombok.*;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommonNorthBoundResponse<T> {

    private String code;
    private String message;
    private String description;
    private String traceId;
    private PageDetailDto pageDetail;
    private T data;
}
