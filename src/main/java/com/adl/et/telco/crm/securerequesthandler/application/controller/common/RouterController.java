package com.adl.et.telco.crm.securerequesthandler.application.controller.common;

import com.adl.et.telco.crm.securerequesthandler.application.controller.BaseController;
import com.adl.et.telco.crm.securerequesthandler.domain.service.serviceinterface.common.ExternalCrmExtensionAPICallService;
import com.adl.et.telco.crm.securerequesthandler.domain.service.serviceimpl.access.JwtService;
import com.adl.et.telco.crm.securerequesthandler.domain.service.serviceimpl.common.MicroServiceURLFetchService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.parser.ParseException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;

/**
 * Controller for handling CRM extension routing requests.
 * Forwards requests to appropriate microservices based on the request path.
 */
@Slf4j
@RestController
@RequestMapping("/srh")
@CrossOrigin(origins = "*")
public class RouterController extends BaseController {

    private final ExternalCrmExtensionAPICallService externalAPICallService;
    private final ObjectMapper objectMapper;

    public RouterController(JwtService jwtService,
                          MicroServiceURLFetchService microServiceURLFetchService,
                          ExternalCrmExtensionAPICallService externalAPICallService,
                          ObjectMapper objectMapper) {
        super(jwtService, microServiceURLFetchService);
        this.externalAPICallService = externalAPICallService;
        this.objectMapper = objectMapper;
    }

    /**
     * Intercept all the POST requests that start with /crm-extension
     *
     * @param request       http request
     * @param requestParams request parameters, can be null or empty. if found will be forwarded into the micro service
     * @return response String from the service
     * @throws IOException IOException
     */
    @Operation(summary = "For forwarding all post api calls start with /crm-extension to external micro services")
    @PostMapping(value = "/crm-extension/**")
    public ResponseEntity<String> forwardPostRequest(HttpServletRequest request,
                                                   @RequestParam(required = false) Map<String, String> requestParams) throws IOException, ParseException {
        String msUrl = performCommonAction(request, requestParams);
        Object requestContent = readRequestBody(request);
        log.debug("SRH|POST request received with content {} containing parameters {}", requestContent, requestParams);
        
        String response = externalAPICallService.externalPostAPICall(msUrl, requestContent, requestParams);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(response);
    }

    /**
     * Intercept all the GET requests that start with /crm-extension
     *
     * @param request       http request
     * @param requestParams request parameters, can be null or empty. if found will be forwarded into the micro service
     * @return response String from the service
     */
    @GetMapping(value = "/crm-extension/**", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> forwardGetRequest(HttpServletRequest request,
                                                  @RequestParam(required = false) Map<String, String> requestParams) throws ParseException {
        String msUrl = performCommonAction(request, requestParams);
        log.debug("SRH|GET request received containing parameters {}", requestParams);
        
        String response = externalAPICallService.externalGetAPICall(msUrl, requestParams);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(response);
    }

    /**
     * Intercept all the PUT requests that start with /crm-extension
     *
     * @param request       http request
     * @param requestParams request parameters, can be null or empty. if found will be forwarded into the micro service
     * @return response String from the service
     * @throws IOException IOException
     */
    @PutMapping(value = "/crm-extension/**")
    public ResponseEntity<String> forwardPutRequest(HttpServletRequest request,
                                                  @RequestParam(required = false) Map<String, String> requestParams) throws IOException, ParseException {
        String msUrl = performCommonAction(request, requestParams);
        Object requestContent = readRequestBody(request);
        log.debug("SRH|PUT request received with content {} containing parameters {}", requestContent, requestParams);
        
        String response = externalAPICallService.externalPutAPICall(msUrl, requestContent, requestParams);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(response);
    }

    /**
     * Intercept all the DELETE requests that start with /crm-extension
     *
     * @param request       http request
     * @param requestParams request parameters, can be null or empty. if found will be forwarded into the micro service
     * @return response String from the service
     * @throws IOException IOException
     */
    @DeleteMapping(value = "/crm-extension/**")
    public ResponseEntity<String> forwardDeleteRequest(HttpServletRequest request,
                                                     @RequestParam(required = false) Map<String, String> requestParams) throws IOException, ParseException {
        String msUrl = performCommonAction(request, requestParams);
        Object requestContent = readRequestBody(request);
        log.debug("SRH|DELETE request received with content {} containing parameters {}", requestContent, requestParams);
        
        String response = externalAPICallService.externalDeleteAPICall(msUrl, requestContent, requestParams);
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
