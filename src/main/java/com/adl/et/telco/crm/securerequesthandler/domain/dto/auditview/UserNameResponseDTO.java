package com.adl.et.telco.crm.securerequesthandler.domain.dto.auditview;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserNameResponseDTO {
    private long id;
    private String name;
    private String email;

}
