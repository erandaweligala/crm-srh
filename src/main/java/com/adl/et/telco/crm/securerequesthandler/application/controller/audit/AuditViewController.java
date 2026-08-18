package com.adl.et.telco.crm.securerequesthandler.application.controller.audit;

import com.adl.et.telco.crm.securerequesthandler.application.controller.BaseController;
import com.adl.et.telco.crm.securerequesthandler.domain.dto.auditview.*;
import com.adl.et.telco.crm.securerequesthandler.domain.dto.common.CommonNorthBoundResponse;
import com.adl.et.telco.crm.securerequesthandler.application.util.annotations.ActionLog;
import com.adl.et.telco.crm.securerequesthandler.application.util.resultenum.AuditRecordsDataSortingEnum;
import com.adl.et.telco.crm.securerequesthandler.application.util.resultenum.SortingDirectionEnum;
import com.adl.et.telco.crm.securerequesthandler.domain.service.serviceinterface.auditview.AuditService;
import com.adl.et.telco.crm.securerequesthandler.domain.service.serviceimpl.access.JwtService;
import com.adl.et.telco.crm.securerequesthandler.domain.service.serviceimpl.common.MicroServiceURLFetchService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for handling audit-related operations.
 * Provides endpoints for retrieving action logs, search types, status codes, user names, and activities.
 */
@Slf4j
@RestController
@RequestMapping("/srh/audit")
public class AuditViewController extends BaseController {
    private static final String LOG_PREFIX = "SRH|AuditViewController|";

    private final AuditService auditViewService;

    public AuditViewController(JwtService jwtService, MicroServiceURLFetchService microServiceURLFetchService, AuditService auditViewService) {
        super(jwtService, microServiceURLFetchService);
        this.auditViewService = auditViewService;
    }

    /**
     * Retrieves action logs based on specified criteria.
     *
     * @param offSet The offset for pagination
     * @param itemsPerPage The number of items per page
     * @param httpServletRequest The HTTP request
     * @param fromDate Optional start date for filtering
     * @param toDate Optional end date for filtering
     * @param searchTypeId Optional search type ID
     * @param subjectTypeValue Optional subject type value
     * @param subjectValue Optional subject value
     * @param status Optional status
     * @param statusId Optional status ID
     * @param userId Optional user ID
     * @param user Optional username
     * @param transactionId Optional transaction ID
     * @param activityValue Optional activity value
     * @param sortBy Optional field to sort by
     * @param sortOrder Optional sort direction (default: DESC)
     * @return ResponseEntity containing the list of action logs
     */
    @GetMapping("/get-action-log")
    @ActionLog(actionID = 45, httpRequestParamIndex = 2)
    public ResponseEntity<CommonNorthBoundResponse<List<ActionLogResponseDTO>>> getActionLogs(
            @RequestParam(required = true) int offSet,
            @RequestParam(required = true) int itemsPerPage,
            HttpServletRequest httpServletRequest,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            @RequestParam(required = false) String searchTypeId,
            @RequestParam(required = false) String subjectTypeValue,
            @RequestParam(required = false) String subjectValue,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String statusId,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String user,
            @RequestParam(required = false) String transactionId,
            @RequestParam(required = false) String activityValue,
            @RequestParam(required = false) AuditRecordsDataSortingEnum sortBy,
            @RequestParam(required = false, defaultValue = "DESC") SortingDirectionEnum sortOrder) {
        
        log.debug("{}getActionLogs|Start|Offset: {}|ItemsPerPage: {}", LOG_PREFIX, offSet, itemsPerPage);

        ActionLogPramDTO actionLogPramDTO = new ActionLogPramDTO();
        actionLogPramDTO.setOffset(offSet);
        actionLogPramDTO.setItemsPerPage(itemsPerPage);
        actionLogPramDTO.setFromDate(fromDate);
        actionLogPramDTO.setToDate(toDate);
        actionLogPramDTO.setSubjectTypeValue(subjectTypeValue);
        actionLogPramDTO.setSubjectValue(subjectValue);
        actionLogPramDTO.setStatus(status);
        actionLogPramDTO.setUserName(user);
        actionLogPramDTO.setTransactionId(transactionId);
        actionLogPramDTO.setActivityValue(activityValue);
        actionLogPramDTO.setSortBy(sortBy);
        actionLogPramDTO.setSortOrder(sortOrder);

        CommonNorthBoundResponse<List<ActionLogResponseDTO>> response = auditViewService.getActionAudits(actionLogPramDTO);
        log.info("{}getActionLogs|End|Success|TotalItems: {}", LOG_PREFIX, response.getData().size());
        
        return setResponseEntity(response);
    }

    /**
     * Retrieves the list of available search types.
     *
     * @return ResponseEntity containing the list of search types
     */
    @GetMapping("/get-search-type-list")
    public ResponseEntity<CommonNorthBoundResponse<List<SearchTypeResponseDTO>>> getSearchTypes() {
        log.debug("{}getSearchTypes|Start", LOG_PREFIX);
        CommonNorthBoundResponse<List<SearchTypeResponseDTO>> response = auditViewService.getSearchTypes();
        log.info("{}getSearchTypes|End|Success|TotalTypes: {}", LOG_PREFIX, response.getData().size());
        return setResponseEntity(response);
    }

    /**
     * Retrieves the list of available status codes.
     *
     * @return ResponseEntity containing the list of status codes
     */
    @GetMapping("/get-status-code-list")
    public ResponseEntity<CommonNorthBoundResponse<List<StatusCodeResponseDTO>>> getStatusCodesList() {
        log.debug("{}getStatusCodesList|Start", LOG_PREFIX);
        CommonNorthBoundResponse<List<StatusCodeResponseDTO>> response = auditViewService.getStatusCodes();
        log.info("{}getStatusCodesList|End|Success|TotalCodes: {}", LOG_PREFIX, response.getData().size());
        return setResponseEntity(response);
    }

    /**
     * Retrieves the list of available usernames.
     *
     * @return ResponseEntity containing the list of usernames
     */
    @GetMapping("/get-user-name-list")
    public ResponseEntity<CommonNorthBoundResponse<List<UserNameResponseDTO>>> getUserNameList() {
        log.debug("{}getUserNameList|Start", LOG_PREFIX);
        CommonNorthBoundResponse<List<UserNameResponseDTO>> response = auditViewService.getUserNames();
        log.info("{}getUserNameList|End|Success|TotalUsers: {}", LOG_PREFIX, response.getData().size());
        return setResponseEntity(response);
    }

    /**
     * Retrieves the list of available activities.
     *
     * @return ResponseEntity containing the list of activities
     */
    @GetMapping("/get-activity-list")
    public ResponseEntity<CommonNorthBoundResponse<List<ActivityCodesResponseDTO>>> getActivityList() {
        log.debug("{}getActivityList|Start", LOG_PREFIX);
        CommonNorthBoundResponse<List<ActivityCodesResponseDTO>> response = auditViewService.getActivities();
        log.info("{}getActivityList|End|Success|TotalActivities: {}", LOG_PREFIX, response.getData().size());
        return setResponseEntity(response);
    }
}
