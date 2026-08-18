package com.adl.et.telco.crm.securerequesthandler.domain.dto.action;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RouteToActionDto {
    private Long routeId;
    private List<Long> actionIds;

    // Optional: equals() and hashCode() if you're not using Lombok
    // You can also add any additional methods or validations if needed
}
