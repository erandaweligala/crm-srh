package com.adl.et.telco.crm.securerequesthandler.application.util.access;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enum representing different user statuses in the system.
 * Each status has a corresponding code that represents its value in the system.
 */
@RequiredArgsConstructor
@Getter
public enum UserStatusEnum {
    /**
     * Represents an active user in the system.
     * Code value: "1"
     */
    ACTIVE("1");

    /**
     * The code value associated with the user status.
     * This value is used in the system to represent the status.
     */
    private final String code;

    public String code() {
        return code;
    }
}
