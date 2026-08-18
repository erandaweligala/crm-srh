package com.adl.et.telco.crm.securerequesthandler.application.constant;

/**
 * Application-wide constants used throughout the system.
 * Constants are organized by their functional area.
 */
public final class AppConstant {
    private AppConstant() {
        throw new IllegalStateException("Utility class");
    }

    // Response Status Constants
    public static final String SUCCESS_CODE = "200";
    public static final String SUCCESS_MESSAGE = "success";
    public static final String SUCCESS = "success";
    public static final String NUMBER = "200";
    public static final String CODE_400 = "400";
    public static final String CODE_500 = "500";

    // Common Field Names
    public static final String NAME = "name";
    public static final String ID = "id";
    public static final String OFFSET = "offset";
    public static final String LIFECYCLE_STATUS = "lifecycleStatus";
    public static final String LIMIT = "limit";
    public static final String FIELDS = "fields";
    public static final String BR_ID = "brId";
    public static final String STATUS = "status";
    public static final String TYPE = "type";
    public static final String MESSAGE = "message";
    public static final String CODE = "code";
    public static final String TRACE = "trace";
    public static final String DATA = "data";
    public static final String VERSION = "1.0";
    public static final String EMPTY_STRING = "";

    // Entity Field Names
    public static final String INDIVIDUAL_ID = "individualId";
    public static final String ORGANIZATION_ID = "organizationId";
    public static final String ORGANIZATION_NAME = "organization.name";
    public static final String ACCOUNT_OWNER = "ACCOUNT_OWNER";
    public static final String DESIGNATION = "DESIGNATION";
    public static final String CUSTOMER_PIN = "customerPin";
    public static final String ACCOUNT_NAME = "accountName";
    public static final String PROJECT_NAME = "projectName";

    // Quote Related Constants
    public static final String OFFERING_ID = "offeringId";
    public static final String PLAN_ID = "planId";
    public static final String PLAN_IDS = "planIds";
    public static final String QUOTE_INFO_QUOTE_NAME = "quoteInfo.quoteName";
    public static final String QUOTE_NO = "quoteNo";
    public static final String ORDER = "ORDER";
    public static final String QUOTE_ID = "quoteId";
    public static final String EXTERNAL_ID = "externalId";
    public static final String OPPORTUNITY_ID = "opportunityId";
    public static final String LEAD_SOURCE = "LEAD_SOURCE";
    public static final String QUOTE_TEMPLATE_ID = "quoteTemplateId";
    public static final String QUOTE_DATE = "quoteDate";
    public static final String GET_QUOTE = "getQuote";
    public static final String QUOTE_TMF_LIST = "quoteTMFList";

    // Quote Terms
    public static final String TERM1 = "> This quotation is valid only for 30 days from the contract date. Any changes to the quotation after this \n" +
            "period may result in adjustments to the prices.";
    public static final String TERM2 = "> All prices are exclusive of applicable taxes, which will be added at the prevailing rate. The client is \n" +
            "responsible for any additional taxes or duties imposed by relevant authorities.";
    public static final String TERM3 = "> The client is responsible for providing necessary access and infrastructure for installation.";
    public static final String TERM4 = "> Once the package data quota is exhausted, usage based charges will be applied.\n";
    public static final String SIGN = "Approved By\n" +
            "\n" +
            "------------------\n";

    // Price Related Constants
    public static final String PRICE_BOOK_NAME = "bookName";
    public static final String BOOK_NAME = "bookName";
    public static final String BOOK_ID = "bookId";
    public static final String DISCOUNT = "discount";
    public static final String CURRENCY_ID = "currency-id";
    public static final String PRICE = "price";
    public static final String UNIT_PRICE_ONETIME = "unitPriceOnetime";
    public static final String IS_DISCOUNT = "isDiscount";
    public static final String UNIT_PRICE_RECURRING = "unitPriceRecurring";
    public static final String DISCOUNT_RECURRING = "discountRecurring";
    public static final String SINGLE_PRICE = "singlePrice";
    public static final String AMT = "amt";
    public static final String RECURRING_AMT = "recurringAmt";

    // Document Related Constants
    public static final String ID_DOC = "_id";
    public static final String QUOTE_INFO_STATUS = "quoteInfo.status";
    public static final String QUOTE_INFO_LAST_UPDATE = "quoteInfo.lastUpdate";
    public static final String CHANGE_HISTORY_LIST = "changeHistoryList";
    public static final String CREATE_DATE = "createDate";
    public static final String USER_DIR = "user.dir";
    public static final String TEMP = "temp";
    public static final String TEMP_FILE = "tempFile";
    public static final String PDF = ".pdf";
    public static final String DRAFT = "Draft";
    public static final String IN_REVIEW = "In Review";
    public static final String DOCUMENT_INFO = "documentInfo";
    public static final String TEMPLATE_ID = "templateId";
    public static final String PDF_FORMAT = "pdf";
    public static final String CREATED_DATE = "createdDate";

    // Date Related Constants
    public static final String START_DATE = "startDate";
    public static final String END_DATE = "endDate";
    public static final String CONTRACT_INFO_START_DATE = "contractInfo.startDate";
    public static final String CONTRACT_INFO_END_DATE = "contractInfo.endDate";

    // Payment Related Constants
    public static final String ONETIME = "onetime";
    public static final String OPTION_ID = "optionId";
    public static final String RECURRING = "recurring";
    public static final String AMOUNT = "amount";
    public static final String IS_SECONDARY = "isSecondary";

    // Exception Types
    public static final String VALIDATION_EXCEPTION = "ValidationException";
    public static final String CONTROLLER_EXCEPTION = "ControllerException";
    public static final String FILTER_EXCEPTION = "FilterException";
    public static final String DOMAIN_EXCEPTION = "DomainException";
    public static final String WEB_CLIENT_EXCEPTION = "WebClientException";

    // Address Related Constants
    public static final String ADDRESS = "address";
    public static final String TO = "To:";
    public static final String COMPANY_ADDRESS = "companyAddress";
    public static final String MAIL_ADDRESS = "mailAddress";

    // Product Related Constants
    public static final String PRODUCT_OFFERING_PRICE_LIST = "productOfferingPriceList";
    public static final String REPLACE = "replace";
    public static final String N_A = "N/A";

    // Sprint Related Constants
    public static final String SPRINT_FORMATTER = "Sprint %s";
    public static final String CTC_OH = "CTC+OH";
    public static final String CALIBRI_BODY = "Calibri (Body)";
    public static final String COUNTRY_RECRUITED = "Country Recruited";
    public static final String DISCIPLINE = "Discipline";
    public static final String CATEGORY = "Category";
    public static final String IS_OS = "IS/OS";
    public static final String RATE = "Rate";
    public static final String TOTAL_EFFORT_RESOURCE = "Total effort / resource";
    public static final String EFFORT_RESOURCE_WITH_BUFFER = "Effort/resource with buffer";
    public static final String SUB_TOTAL = "Sub total";
    public static final String RESOURCES_COST = "Resources | Cost";

    // Customer Related Constants
    public static final String INVALID_CUSTOMER_CODE = "2054";
    public static final String INVALID_CUSTOMER_PIN_NUMBER = "Invalid CustomerPin number";

    // TMF Field Lists
    public static final String GET_ORGANIZATION_LIST_FIELDS = "id,href,name,relatedParty,organizationIdentification,contactMedium,@type,@baseType";
    public static final String GET_INDIVIDUAL_LIST_FIELDS = "id,href,name,role,partyCharacteristic,@type,@baseType";
    public static final String GET_QUOTE_TMF_LIST_FIELDS = "id,href,version,name,externalId,quoteItem";
    public static final String GET_OFFERING_LIST_FIELDS = "id,href,name,category,productOfferingPrice,@type,@baseType";
}
