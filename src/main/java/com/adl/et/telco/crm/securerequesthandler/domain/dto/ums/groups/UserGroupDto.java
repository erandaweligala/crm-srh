package com.adl.et.telco.crm.securerequesthandler.domain.dto.ums.groups;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserGroupDto {
    private Long userId;
    private List<GroupDTO> groups;
    private List<Long> roleIds;
}
