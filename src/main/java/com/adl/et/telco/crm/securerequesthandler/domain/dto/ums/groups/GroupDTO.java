package com.adl.et.telco.crm.securerequesthandler.domain.dto.ums.groups;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupDTO {
    private String groupId;
    private String groupName;
    private List<GroupLevelDTO> levels;
}
