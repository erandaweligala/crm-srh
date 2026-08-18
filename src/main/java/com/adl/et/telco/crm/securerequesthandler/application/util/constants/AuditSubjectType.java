package com.adl.et.telco.crm.securerequesthandler.application.util.constants;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Enum-like class containing all possible audit subject types.
 * This class provides a centralized location for all audit subject types
 * and a method to retrieve all available subject types.
 */
public final class AuditSubjectType {
    
    // Prevent instantiation
    private AuditSubjectType() {
        throw new IllegalStateException("Utility class");
    }

    // User-related subjects
    public static final String USER_ID = "USER_ID";
    public static final String EMAIL = "EMAIL";
    public static final String MSISDN = "MSISDN";

    // Role and Permission subjects
    public static final String ROLE_ID = "ROLE_ID";
    public static final String ROLE_NAME = "ROLE_NAME";
    public static final String PERMISSION_ID = "PERMISSION_ID";
    public static final String PERMISSION_NAME = "PERMISSION_NAME";

    // Customer and Account subjects
    public static final String CUSTOMER_ID = "CUSTOMER_ID";
    public static final String ACCOUNT_ID = "ACCOUNT_ID";
    public static final String REFERENCE_ID = "REFERENCE_ID";

    // Business Process subjects
    public static final String ORDER_ID = "ORDER_ID";
    public static final String TASK_ID = "TASK_ID";
    public static final String FORM_ID = "FORM_ID";

    // CRM subjects
    public static final String NOTE_ID = "NOTE_ID";
    public static final String CONTACT_ID = "CONTACT_ID";
    public static final String LEAD_ID = "LEAD_ID";
    public static final String DEAL_ID = "DEAL_ID";

    // Cache for all subject types
    private static final List<String> ALL_SUBJECTS = Collections.unmodifiableList(
        Arrays.stream(AuditSubjectType.class.getDeclaredFields())
            .filter(field -> field.getType().equals(String.class))
            .map(Field::getName)
            .collect(Collectors.toList())
    );

    /**
     * Returns an unmodifiable list of all available audit subject types.
     * The list is cached for better performance.
     *
     * @return List of all audit subject types
     */
    public static List<String> getAllSubjects() {
        return ALL_SUBJECTS;
    }

    /**
     * Validates if the given subject type is a valid audit subject type.
     *
     * @param subjectType The subject type to validate
     * @return true if the subject type is valid, false otherwise
     */
    public static boolean isValidSubjectType(String subjectType) {
        return subjectType != null && ALL_SUBJECTS.contains(subjectType);
    }

    /**
     * Returns a comma-separated string of all available audit subject types.
     * Useful for logging or documentation purposes.
     *
     * @return Comma-separated string of all audit subject types
     */
    public static String getAllSubjectsAsString() {
        return String.join(", ", ALL_SUBJECTS);
    }
}
