package com.adl.et.telco.crm.securerequesthandler.application.util.constants;

/**
 * Utility class containing application-wide constants.
 * Constants are organized by their functional area.
 */
public final class Constants {


    private Constants() {
        throw new IllegalStateException("Utility class");
    }

    // Order Related Constants
    public static final String ORDER_ID = "orderId";
    public static final String ORDERID = "orderId";
    public static final String ORDERITEMID = "itemId";

    // Pagination Constants
    public static final String LIMIT = "limit";
    public static final String OFFSET = "offset";

    // User Related Constants
    public static final String USER_NAME = "userName";
    public static final String USER_NAME_NOT_FOUND = "User name not found";
    public static final String USER_NOT_FOUN_PATH = "no-user";
    public static final String INACTIVE_USER_PATH = "user-inactive";
    public static final String FULL_NAME = "fullName";
    public static final String EMAIL = "email";
    public static final String EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$";

    // Role and Permission Constants
    public static final String ROLE_ID = "roleId";
    public static final String ROLE_NAME = "roleName";
    public static final String PERMISSION_NAME = "permissionName";
    public static final String PERMISSIONS = "permissions";

    // UI Component Constants
    public static final String MENU_ID = "menuId";
    public static final String SECTION_ID = "sectionId";
    public static final String COMPONENT_ID = "componentId";
    public static final String STATUS_ID = "statusId";

    // Error and Status Constants
    public static final String INTERNAL_ERROR_PATH = "internal-error";
    public static final String RESPONSE_FILTER_MASK = "***";

    // System Constants
    public static final String CPQ = "CPQ";
    public static final String CRM = "CRM";

    // Customer Related Constants
    public static final String TENANT_ID = "tenantId";
    public static final String APPLICATION_ID = "x-application-id";
    public static final String DEFAULT_APPLICATION_ID = "1";
    public static final String CUSTOMER_ID = "customerId";
    public static final String MSISDN = "msisdn";
    public static final String REFERENCE_ID = "referenceId";
    public static final String MOBILE_NO = "mobileNo";
    public static final String MOBILE_NUMBER = "MOBILENUMBER";
    public static final String XXXXX = "xxxxxx";
    public static final String X_USER = "X-User";
    public static final String X_ROLES = "X-Roles";
    public static final String X_GROUP_LEVELS = "X-GroupLevels";

    // Token and Authentication Constants
    public static final String T_ID = "tid";
    public static final String ACCESS = "access";
    public static final String T_TYPE = "ttype";
    public static final String TEMP_TOKEN = "TEMPTOKEN";
    public static final String ACESS_TOKEN = "ACCESSTOKEN:";
    public static final String AUTHORIZATION = "Authorization";
    public static final String BEARER = "Bearer ";

    // Session Related Constants
    public static final String IDLE_TIME_RANGE = "idleTimeRange";
    public static final String REFRESH_TIME_RANGE = "refreshTimeRange";

    // SIM Pack Related Constants
    public static final String SIMPACKID = "simPackId";
    public static final String SIMPACKSTATUS = "status";

    // Redis Related Constants
    public static final String REDIS_KEY_ROUTING = "CRM_TELCO_DEV_ROUTING";
    public static final String REDIS_ROUTING_CRM_EXTENSION = "CRM_ROUTING_CRM_EXTENSION";
}
