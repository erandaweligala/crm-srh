package com.adl.et.telco.crm.securerequesthandler.domain.dto.ums.groups;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupLevelDTO {
    private String levelId;
    private String levelName;
    private String levelValue;
}
