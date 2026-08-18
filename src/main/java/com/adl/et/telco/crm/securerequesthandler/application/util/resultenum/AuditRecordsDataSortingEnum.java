package com.adl.et.telco.crm.securerequesthandler.application.util.resultenum;

import lombok.Getter;

@Getter
public enum AuditRecordsDataSortingEnum {
    TRANSACTION_ID("activity_id",""),
    CREATED_DATE_TIME("date_time",""),
    USER_NAME("user_id",""),
    MSISDN("msisdn",""),
    ACTIVITY("activity",""),
    SEARCH_TYPE("subject_type",""),
    SEARCH_VALUE("subject_value",""),
    STATUS("status",""),
    STATUS_DESCRIPTION("status_description",""),

    CASE_ID("case_id","cau."),
    CASE_TYPE("case_type","cau."),
    CASE_CATEGORY("case_category","cau."),
    CASE_SUB_CATEGORY("case_sub_category","cau."),

    ORDER_ID("order_id","oau."),
    ORDER_CATEGORY("order_category","oau."),
    ORDER_SUB_CATEGORY("order_sub_category","oau."),
    TEMP_ORDER_ID("temp_order_id","oau.");

    private String columnName;
    private String tableName;

    AuditRecordsDataSortingEnum(String columnName, String tableName) {
        this.columnName = columnName;
        this.tableName = tableName;
    }
}
