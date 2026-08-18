package com.adl.et.telco.crm.securerequesthandler.application.util.resultenum;

public enum DisplayResultCodeEnum {
    GET_TROUBLE_TICKETS_SUCCESS("100","Get trouble ticket details successful"),

    INTERNAL_SERVER_ERROR("101", "Internal Server Error."),
    INVALID_INPUT("102", "Invalid Input Parameters. Please Check the request again!"),
    ACCESS_DENIED("103", "Access Denied"),

    SENDING_OTP_FAILED("107","Sending OTP Failed. Error response from API"),
    SENDING_OTP_INTERNAL_SERVER_ERROR("108","Sending OTP Failed. Internal server error"),
    VALIDATE_OTP_FAILED("109","Validate OTP Failed. Error response from API"),
    VALIDATE_OTP_INTERNAL_SERVER_ERROR("110","Validate OTP Failed. Internal server error"),
    GET_USER_BASIC_INFO_FAILED("111","Get user basic information failed.Internal server error"),
    GET_CPQ_USER_BASIC_INFO_FAILED("111","Get CPQ user basic information failed. Internal server error!"),

    RESOURCE_ACCESS_EXCEPTION("112","I/O error.Internal server error"),
    GET_USER_NAME_FAILED("111","Get User Name failed.Internal server error"),

    GET_CUSTOMER_ACCOUNT_DETAILS_BY_MSISDN_SUCCESS("100","Get Customer Account Details By MSISDN Succcess."),
    GET_CUSTOMER_ACCOUNT_DETAILS_FAILED_INTERNAL_SERVER_ERROR("114","Get Customer Account Details By MSISDN Failed. Internal Server Error."),

    INTERNAL_SERVER_ERROR_GET_CUSTOMER_PROFILE("120", "GET CUSTOMER PROFILE FAILED. INTERNAL SERVER ERROR."),
    GET_PRODUCT_DETAILS_FAILED("114","Get Product Details Failed. Internal Server"),

    GET_TROUBLE_TICKETS_INTERNAL_SERVER_ERROR("116","Get Trouble Tickets Failed.Internal Server Error"),


    CONNECTION_OVERVIEW_INTERNAL_SERVER_ERROR("118","Get connection overview Failed. Internal server error"),
    GET_PRODUCT_DETAILS_SUCCESS("100","Product details successfully retrieved"),


    GET_TODOLIST_FAILED("121","Get Todolist Failed.  Internal Server Error."),
    CREATE_TODOLIST_TASK_FAILED("122","Create Todolist Task Failed.  Internal Server Error."),
    UPDATE_TODOLIST_TASK_FAILED("123","Update Todolist Task Failed.  Internal Server Error."),
    DELETE_TODOLIST_TASK_FAILED("124","Delete Todolist Failed.  Internal Server Error."),

    CREATE_NOTE_FAILED("125","Create  Note Failed.  Internal Server Error."),
    UPDATE_NOTE_FAILED("126","Update  Note Failed.  Internal Server Error."),
    DELETE_NOTE_FAILED("127","Delete  Note Failed.  Internal Server Error."),
    GET_NOTE_FAILED("128","Get  Note Failed.  Internal Server Error."),

    CREATE_TASK_FAILED("129","Create Task Failed.  Internal Server Error."),
    UPDATE_TASK_FAILED("130","Update Task Failed.  Internal Server Error."),
    DELETE_TASK_FAILED("131","Delete Task Failed.  Internal Server Error."),
    GET_TASK_FAILED("132","Get Task Failed.  Internal Server Error."),

    CREATE_CALL_FAILED("133","Create Call Failed.  Internal Server Error."),
    UPDATE_CALL_FAILED("134","Update Call Failed.  Internal Server Error."),
    DELETE_CALL_FAILED("135","Delete Call Failed.  Internal Server Error."),
    GET_CALL_FAILED("136","Get Call Failed.  Internal Server Error."),


    CREATE_MEETING_FAILED("137","Create Meeting Failed.  Internal Server Error."),
    UPDATE_MEETING_FAILED("138","Update Meeting Failed.  Internal Server Error."),
    DELETE_MEETING_FAILED("139","Delete Meeting Failed.  Internal Server Error."),
    GET_MEETING_FAILED("140","Get Meeting Failed.  Internal Server Error."),

    CREATE_EMAIL_FAILED("141","Create Email Failed.  Internal Server Error."),
    GET_EMAIL_FAILED("142","Get Email Failed.  Internal Server Error."),


    GET_CUSTOMER_OVERVIEW_FAILED("4001","GET_CUSTOMER_OVERVIEW_FAILED"),
    GET_PERMISSION_LIST_FAILED("4002", "GET_PERMISSION_LIST_FAILED"),
    GET_PERMISSION_DETAILS_FAILED("4003", "GET_PERMISSION_DETAILS_FAILED"),
    GET_ACTION_TO_COMPONENT_FAILED("4004", "GET_ACTION_TO_COMPONENT_FAILED"),
    GET_ALL_MENU_COMPONENT_FAILED("4005", "GET_ALL_MENU_COMPONENT_FAILED"),
    CREATE_PERMISSION_FAILED("4006", "CREATE_PERMISSION_FAILED"),
    EDIT_PERMISSION_FAILED("4007", "EDIT_PERMISSION_FAILED"),
    GET_PERMISSION_META_DATA_FAILED("4008", "GET_PERMISSION_META_DATA_FAILED"),
    GET_ROLE_LIST_FAILED("4009","GET_ROLE_LIST_FAILED" ),
    GET_ROLE_DETAILS_FAILED("4010", "GET_ROLE_DETAILS_FAILED"),
    CREATE_ROLE_FAILED("4011","CREATE_ROLE_FAILED" ),
    CREATE_TICKET_FAILED("4012", "CREATE_TICKET_FAILED"),
    UPDATE_ROLE_FAILED("4012","UPDATE_ROLE_FAILED" ),
    GET_ROLE_META_DATA_FAILED("4013", "GET_ROLE_META_DATA_FAILED"),
    GET_USER_DETAILS_FAILED("4014","GET_USER_DETAILS_FAILED" ),
    CREATE_USER_FAILED("4015","CREATE_USER_FAILED" ),
    EDIT_USER_FAILED("4016", "EDIT_USER_FAILED"),
    VALIDATE_EMAIL_FAILED("4017", "VALIDATE_EMAIL_FAILED"),
    GET_USER_STATUS_META_DATA_FAILED("4018", "GET_USER_STATUS_META_DATA_FAILED"),
    GET_ALL_USER_FAILED("4019", "GET_ALL_USER_FAILED"),
    GET_ALL_ATTRIBUTE_DETAILS_FOR_CACHE_FAILED("4020", "GET_ALL_ATTRIBUTE_DETAILS_FOR_CACHE_FAILED"),
    GET_ALL_ROUTE_PATH_DETAILS_FOR_CACHE_FAILED("4020", "GET_ALL_ROUTE_PATH_DETAILS_FOR_CACHE_FAILED"),
    GET_INTERACTION_TIMELINE_FAILED("4020", "GET_INTERACTION_TIMELINE_FAILED"),

    UPDATE_PROFILE_FAILED("4021", "UPDATE_PROFILE_FAILED"),
    GET_TICKET_SUMMARY( "4022", "GET_TICKET_SUMMARY"),
    EXCEPTION_IN_SERVICE_LAYER("4023", "Exception In Service Layer." ),
    GET_ORDER_STATUS_FLOW_FAILED("4024","Get Order Staus Flow Failed." ),
    GET_CUSTOMER_EXCEPTION("4025","Get Customer Failed." ),
    REJECT_CUSTOMER_EXCEPTION("4026","Reject Customer Failed." ),
    ACCEPT_CUSTOMER_EXCEPTION("4027","Accept Customer Failed." ),
    EDIT_TICKET_FAILED("4028","Edit Trouble Ticket Failed." ),
    GET_CUSTOMER_ORDER_FAILED("4029","Get Customer Order Failed." ),
    GET_AUDIT_DETAILS_FAILED("4030","Get Audit Details Failed." ),
    EXCEPTION_IN_CLIENT_LAYER("4031","Exception In Client Layer" ),
    GET_FORM_LIST_FAILED("4032", "Get Eform List Failed."),
    GET_FORM_ELEMENT_LIST_FAILED("4033", "Get Eform Element List Failed." ),
    SUCCESS("1000", "Success"),
    CREATE_E_ACCOUNT_FAILED("4034","Create Enterprise Account Failed. Internal Server Error." ),
    UPDATE_E_ACCOUNT_FAILED("4035","Update Enterprise Account Failed. Internal Server Error." ),
    GET_ACCOUNT_LIST_FAILED("4036", "Get Account List Failed. Internal Server Error."),
    GET_ACCOUNT_BY_ID_FAILED("4037","Get Account By Id Failed. Internal Server Error." ),

    CREATE_E_CONTACT_FAILED("4038","Create Enterprise Contact Failed. Internal Server Error." ),
    UPDATE_E_CONTACT_FAILED("4039","Update Enterprise Contact Failed. Internal Server Error." ),
    GET_CONTACT_LIST_FAILED("4040", "Get Contact List Failed. Internal Server Error."),
    GET_CONTACT_BY_ID_FAILED("4041","Get Contact By Id Failed. Internal Server Error." ),
    ADD_EFORM_ELEMENT_FAILED("4042","Add Eform Element Failed. Internal Server Error." ),
    UPDATE_EFORM_ELEMENT_FAILED("4043","Update Eform Element Failed. Internal Server Error." ),
    DELETE_EFORM_ELEMENT_FAILED("4044","Delete Eform Element Failed. Internal Server Error." ),
    GET_COMMON_META_DATA_LIST_FAILED("4045","Get Meta Data Failed. Internal Server Error." ),
    DELETE_ATTACHMENT_FAILED("4046", "Delete Attachment Failed. Internal Server Error."),
    UPDATE_ATTACHMENT_FAILED("4047", "Update Attachment Failed. Internal Server Error." ),
    UPLOAD_ATTACHMENT_FAILED("4048", "Upload Attachment Failed. Internal Server Error." ),
    LIST_ATTACHMENT_FAILED("4049", "List Attachment Failed. Internal Server Error." ),
    DOWNLOAD_ATTACHMENT_FAILED("4050", "Download Attachment Failed. Internal Server Error." ),
    CREATE_E_LEAD_FAILED("4051", "Create Lead Failed. Internal Server Error."),
    UPDATE_E_LEAD_FAILED("4052", "Update Lead Failed. Internal Server Error." ),
    GET_LEAD_LIST_FAILED("4053", "Get All Leads Failed. Internal Server Error." ),
    GET_LEAD_BY_ID_FAILED("4054", "Get Lead Failed. Internal Server Error." ),
    CONVERT_LEAD_FAILED("4055", "Convert Lead Failed. Internal Server Error." ),
    CREATE_E_DEAL_FAILED("4056", "Create Deal Failed. Internal Server Error."),

    UPDATE_E_DEAL_FAILED("4057", "Update Deal Failed. Internal Server Error." ),


    GET_LEAD_DEAL_FAILED("4058", "Get Deal Failed. Internal Server Error."  ),
    GET_DEAL_BY_ID_FAILED("4059", "Get Deal By ID Failed. Internal Server Error."  ),

    GET_DEAL_LIST_FAILED("4060", "Get Deal List Failed. Internal Server Error."  ),
    GET_CUSTOM_PROPERTIES_DATA_FAILED("4061", "GET_CUSTOM_PROPERTIES_DATA_FAILED"),
    GET_EXTENSION_DATA_FAILED("4061", "GET_EXTENSION_DATA_FAILED"),
    CREATE_EXTENSION_FAILED("4061", "CREATE_EXTENSION_FAILED"),
    CPQ_CREATE_QUOTE_FAILED("4015","CPQ_CREATE_QUOTE_FAILED" ),
    CPQ_GET_QUOTE_LIST_FAILED("4015","CPQ_GET_QUOTE_LIST_FAILED" ),
    CPQ_GET_PRICE_BOOK_LIST_FAILED("4015","CPQ_GET_PRICE_BOOK_LIST_FAILED" ),
    ;
    private String code;
    private String description;

    DisplayResultCodeEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String code(){
        return code;
    }
    public String description(){
        return description;
    }
}
