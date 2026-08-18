package com.adl.et.telco.crm.securerequesthandler.application.controller.common;

import com.adl.et.telco.crm.securerequesthandler.application.controller.BaseController;
import com.adl.et.telco.crm.securerequesthandler.domain.service.serviceimpl.access.JwtService;
import com.adl.et.telco.crm.securerequesthandler.domain.service.serviceimpl.common.MicroServiceURLFetchService;
import com.adl.et.telco.crm.securerequesthandler.domain.service.serviceinterface.common.ExternalAPICallService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.parser.ParseException;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * Controller for handling common routing of API requests to external services.
 * Provides endpoints for forwarding GET, POST, PUT, DELETE requests and file operations.
 *
 * AUTHENTICATION BYPASS: every request that reaches this controller is forwarded to
 * the resolved microservice without authentication, token validation or any
 * authorization check. Callers do not need to send a bearer token.
 */
@Slf4j
@RestController
@RequestMapping("/srh")
@CrossOrigin(origins = "*")
public class CommonRouteController extends BaseController {

    private final ExternalAPICallService externalAPICallService;
    private final ObjectMapper objectMapper;

    /**
     * Constructs a new CommonRouteController with required dependencies.
     *
     * @param jwtService Service for JWT token handling
     * @param microServiceURLFetchService Service for URL resolution
     * @param externalAPICallService Service for making external API calls
     * @param objectMapper ObjectMapper for JSON processing
     */
    public CommonRouteController(JwtService jwtService,
                               MicroServiceURLFetchService microServiceURLFetchService,
                               ExternalAPICallService externalAPICallService,
                               ObjectMapper objectMapper) {
        super(jwtService, microServiceURLFetchService);
        this.externalAPICallService = externalAPICallService;
        this.objectMapper = objectMapper;
    }

    /**
     * Forwards POST requests to external microservices.
     *
     * @param request HttpServletRequest containing the request details
     * @param requestParams Optional request parameters to be forwarded
     * @return ResponseEntity containing the service response
     * @throws IOException if there's an I/O error
     * @throws ParseException if there's an error parsing the request
     */
    @Operation(summary = "Forwards all POST API calls starting with /api to external microservices")
    @PostMapping(value = "/api/**")
    public ResponseEntity<String> forwardPostRequest(HttpServletRequest request,
                                                   @RequestParam(required = false) Map<String, String> requestParams,
                                                     @RequestHeader(required = false) Map<String, String> requestHeaders)
            throws IOException, ParseException {
        String msUrl = populateCommonAction(request, requestParams);
        Object requestContent = readRequestBody(request);
        log.debug("SRH|POST request received with content {} containing parameters {}", requestContent, requestParams);
        
        String response = externalAPICallService.externalPostAPICall(msUrl, requestContent, requestParams, requestHeaders);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(response);
    }

    /**
     * Forwards GET requests to external microservices.
     *
     * @param request HttpServletRequest containing the request details
     * @param requestParams Optional request parameters to be forwarded
     * @return ResponseEntity containing the service response
     * @throws ParseException if there's an error parsing the request
     */
    @GetMapping(value = "/api/**", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> forwardGetRequest(HttpServletRequest request,
                                                  @RequestParam(required = false) Map<String, String> requestParams,
                                                    @RequestHeader(required = false) Map<String, String> requestHeaders)
            throws ParseException {
        String msUrl = populateCommonAction(request, requestParams);
        log.debug("SRH|GET request received containing parameters {}", requestParams);
        
        String response = externalAPICallService.externalGetAPICall(msUrl, requestParams, requestHeaders);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(response);
    }

    /**
     * Forwards PUT requests to external microservices.
     *
     * @param request HttpServletRequest containing the request details
     * @param requestParams Optional request parameters to be forwarded
     * @return ResponseEntity containing the service response
     * @throws IOException if there's an I/O error
     * @throws ParseException if there's an error parsing the request
     */
    @PutMapping(value = "/api/**")
    public ResponseEntity<String> forwardPutRequest(HttpServletRequest request,
                                                  @RequestParam(required = false) Map<String, String> requestParams,
                                                    @RequestHeader(required = false) Map<String, String> requestHeaders)
            throws IOException, ParseException {
        String msUrl = populateCommonAction(request, requestParams);
        Object requestContent = readRequestBody(request);
        log.debug("SRH|PUT request received with content {} containing parameters {}", requestContent, requestParams);
        
        String response = externalAPICallService.externalPutAPICall(msUrl, requestContent, requestParams, requestHeaders);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(response);
    }

    /**
     * Forwards PATCH requests to external microservices.
     *
     * @param request HttpServletRequest containing the request details
     * @param requestParams Optional request parameters to be forwarded
     * @return ResponseEntity containing the service response
     * @throws IOException if there's an I/O error
     * @throws ParseException if there's an error parsing the request
     */
    @PatchMapping(value = "/api/**")
    public ResponseEntity<String> forwardPatchRequest(HttpServletRequest request,
                                                    @RequestParam(required = false) Map<String, String> requestParams,
                                                    @RequestHeader(required = false) Map<String, String> requestHeaders)
            throws IOException, ParseException {
        String msUrl = populateCommonAction(request, requestParams);
        Object requestContent = readRequestBody(request);
        log.debug("SRH|PATCH request received with content {} containing parameters {}", requestContent, requestParams);

        String response = externalAPICallService.externalPatchAPICall(msUrl, requestContent, requestParams, requestHeaders);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(response);
    }

    /**
     * Forwards DELETE requests to external microservices.
     *
     * @param request HttpServletRequest containing the request details
     * @param requestParams Optional request parameters to be forwarded
     * @return ResponseEntity containing the service response
     * @throws IOException if there's an I/O error
     * @throws ParseException if there's an error parsing the request
     */
    @DeleteMapping(value = "/api/**")
    public ResponseEntity<String> forwardDeleteRequest(HttpServletRequest request,
                                                     @RequestParam(required = false) Map<String, String> requestParams,
                                                       @RequestHeader(required = false) Map<String, String> requestHeaders)
            throws IOException, ParseException {
        String msUrl = populateCommonAction(request, requestParams);
        Object requestContent = readRequestBody(request);
        log.debug("SRH|DELETE request received with content {} containing parameters {}", requestContent, requestParams);
        
        String response = externalAPICallService.externalDeleteAPICall(msUrl, requestContent, requestParams, requestHeaders);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(response);
    }

    /**
     * Forwards file download requests to external microservices.
     *
     * @param request HttpServletRequest containing the request details
     * @param requestParams Optional request parameters to be forwarded
     * @return ResponseEntity containing the file as InputStreamResource
     * @throws IOException if there's an I/O error
     */
    @PostMapping(value = "/api/file-download/**")
    public ResponseEntity<InputStreamResource> forwardFileDownLoadRequest(HttpServletRequest request,
                                                                       @RequestParam(required = false) Map<String, String> requestParams,
                                                                          @RequestHeader(required = false) Map<String, String> requestHeaders)
            throws IOException {
        String msUrl = performCommonAction(request, requestParams);
        log.debug("SRH|GET request received for download files with param {}", requestParams);
        
        Object requestContent = readRequestBody(request);
        InputStream inputStreamResource = externalAPICallService
                .externalGetFileAsAStreamForPostEndPoint(msUrl, requestContent, requestParams, requestHeaders);
        
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=Template.xlsx");
        headers.setContentType(MediaType.TEXT_PLAIN);
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(new InputStreamResource(inputStreamResource));
    }

    /**
     * Forwards file upload requests to external microservices.
     *
     * @param request HttpServletRequest containing the request details
     * @param requestParams Optional request parameters to be forwarded
     * @param file The file to be uploaded
     * @return ResponseEntity containing the service response
     * @throws IOException if there's an I/O error
     * @throws ParseException if there's an error parsing the request
     */
    @PostMapping(value = "/api/upload-doc/**")
    public ResponseEntity<String> forwardPostUploadRequest(HttpServletRequest request,
                                                         @RequestParam(required = false) Map<String, String> requestParams,
                                                         @RequestParam("file") MultipartFile file,
                                                           @RequestHeader(required = false) Map<String, String> requestHeaders)
            throws IOException, ParseException {
        String msUrl = performCommonAction(request, requestParams);
        log.debug("SRH|Upload request received for file: {}", file.getOriginalFilename());
        
        String response = externalAPICallService.externalPostUploadAPICall(msUrl, file, requestParams, requestHeaders);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(response);
    }

    /**
     * Helper method to read request body
     *
     * @param request HttpServletRequest
     * @return Object parsed from request body
     * @throws IOException if reading fails
     */
    private Object readRequestBody(HttpServletRequest request) throws IOException {
        ServletInputStream bodyAsStream = request.getInputStream();
        if (bodyAsStream.available() == 0) {
            return null;
        }
        return objectMapper.readValue(request.getInputStream(), Object.class);
    }
}
