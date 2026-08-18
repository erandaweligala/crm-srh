package com.adl.et.telco.crm.securerequesthandler.domain.service.serviceimpl.common;

import com.adl.et.telco.crm.securerequesthandler.application.client.accessclient.CrmUserDetailClient;
import com.adl.et.telco.crm.securerequesthandler.application.util.constants.ServiceConstants;
import com.adl.et.telco.crm.securerequesthandler.domain.dto.common.CommonAdaptorResp;
import com.adl.et.telco.crm.securerequesthandler.domain.dto.common.CommonNorthBoundResponse;
import com.adl.et.telco.crm.securerequesthandler.domain.dto.common.CommonSouthBoundResponse;
import com.adl.et.telco.crm.securerequesthandler.domain.dto.common.Result;
import com.adl.et.telco.crm.securerequesthandler.application.exception.ClientException;
import com.adl.et.telco.crm.securerequesthandler.domain.dto.ums.groups.UserGroupDto;
import com.adl.et.telco.crm.securerequesthandler.domain.service.serviceimpl.access.JwtService;
import com.adl.et.telco.crm.securerequesthandler.domain.service.serviceinterface.common.ExternalAPICallService;
import com.adl.et.telco.crm.securerequesthandler.application.util.constants.Constants;
import com.adl.et.telco.crm.securerequesthandler.application.util.exception.BaseException;
import com.adl.et.telco.crm.securerequesthandler.application.util.exception.ExceptionHandler;
import com.adl.et.telco.crm.securerequesthandler.application.util.resultenum.DisplayResultCodeEnum;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service implementation for making external API calls.
 * Handles various types of HTTP requests (GET, POST, PUT, DELETE) and file operations.
 */
@Service
@Slf4j
public class ExternalAPICallServiceImpl implements ExternalAPICallService {
    private static final String LOG_PREFIX = "SRH|ExternalAPICallServiceImpl|";
    private static final String DEFAULT = "default";
    private static final String CATALOG_ID = "catalogId";
    private static final String TRACE_ID = "traceId";
    private static final String USER_NAME = "userName";
    private static final int CONNECTION_TIMEOUT_MS = 6000;
    private static final int READ_TIMEOUT_MS = 6000;

    private final RestTemplate restTemplate;
    private final HttpServletRequest httpServletRequest;
    private final ExceptionHandler exceptionHandler;
    private final JwtService jwtService;
    private final ObjectMapper objectMapper;
    private final String tenantId;

    @Autowired
    private CrmUserDetailClient crmUserDetailClient;

    @Autowired
    public ExternalAPICallServiceImpl(RestTemplate restTemplate,
                                    HttpServletRequest httpServletRequest,
                                    ExceptionHandler exceptionHandler,
                                    JwtService jwtService,
                                    @Value("${tenant-id}") String tenantId) {
        this.restTemplate = restTemplate;
        this.httpServletRequest = httpServletRequest;
        this.exceptionHandler = exceptionHandler;
        this.jwtService = jwtService;
        this.objectMapper = new ObjectMapper();
        this.tenantId = tenantId;
        log.info("{}Initialized with tenant ID: {}", LOG_PREFIX, tenantId);
    }

    @Override
    public String externalGetAPICall(String url, Map<String, String> valueMap, Map<String, String> requestHeader) throws BaseException, ParseException {
        log.info("{}Starting GET API call to URL: {}", LOG_PREFIX, url);
        long startTime = System.nanoTime();
        try {
            String finalUrl = getFinalURL(url, valueMap);
            HttpEntity<?> requestEntity = getHttpEntityWithHeaders(null, requestHeader, MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON);
            return invokeRestApi(finalUrl, HttpMethod.GET, requestEntity);
        } finally {
            logExecutionTime("GET", url, startTime);
        }
    }

    @Override
    public String externalPostAPICall(String url, Object request, Map<String, String> valueMap, Map<String, String> requestHeader) throws BaseException, ParseException {
        log.info("{}Starting POST API call to URL: {}", LOG_PREFIX, url);
        long startTime = System.nanoTime();
        try {
            HttpEntity<?> requestEntity = getHttpEntityWithHeaders(request, requestHeader, MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON);
            String finalUrl = getFinalURL(url, valueMap);
            return invokeRestApi(finalUrl, HttpMethod.POST, requestEntity);
        } finally {
            logExecutionTime("POST", url, startTime);
        }
    }

    @Override
    public String externalPutAPICall(String url, Object request, Map<String, String> valueMap, Map<String, String> requestHeader) throws BaseException, ParseException {
        log.info("{}Starting PUT API call to URL: {}", LOG_PREFIX, url);
        long startTime = System.nanoTime();
        try {
            HttpEntity<?> requestEntity = getHttpEntityWithHeaders(request, requestHeader, MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON);
            String finalUrl = getFinalURL(url, valueMap);
            return invokeRestApi(finalUrl, HttpMethod.PUT, requestEntity);
        } finally {
            logExecutionTime("PUT", url, startTime);
        }
    }

    @Override
    public String externalPatchAPICall(String url, Object request, Map<String, String> valueMap, Map<String, String> requestHeader) throws BaseException, ParseException {
        log.info("{}Starting PATCH API call to URL: {}", LOG_PREFIX, url);
        long startTime = System.nanoTime();
        try {
            HttpEntity<?> requestEntity = getHttpEntityWithHeaders(request, requestHeader, MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON);
            String finalUrl = getFinalURL(url, valueMap);
            return invokeRestApi(finalUrl, HttpMethod.PATCH, requestEntity);
        } finally {
            logExecutionTime("PATCH", url, startTime);
        }
    }

    @Override
    public String externalDeleteAPICall(String url, Object request, Map<String, String> valueMap, Map<String, String> requestHeader) throws BaseException, ParseException {
        log.info("{}Starting DELETE API call to URL: {}", LOG_PREFIX, url);
        long startTime = System.nanoTime();
        try {
            HttpEntity<?> requestEntity = getHttpEntityWithHeaders(request, requestHeader, MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON);
            String finalUrl = getFinalURL(url, valueMap);
            return invokeRestApi(finalUrl, HttpMethod.DELETE, requestEntity);
        } finally {
            logExecutionTime("DELETE", url, startTime);
        }
    }

    @Override
    public InputStream externalGetFileAsAStreamForPostEndPoint(String url, Object request, Map<String, String> valueMap, Map<String, String> requestHeader) throws IOException {
        log.info("{}Starting file stream request to URL: {}", LOG_PREFIX, url);
        long startTime = System.nanoTime();
        try {
            String finalUrl = getFinalURL(url, valueMap);
            URL myUrl = new URL(finalUrl);
            HttpURLConnection connection = (HttpURLConnection) myUrl.openConnection();
            connection.setConnectTimeout(CONNECTION_TIMEOUT_MS);
            connection.setReadTimeout(READ_TIMEOUT_MS);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; utf-8");
            connection.setDoOutput(true);

            String requestAsString = objectMapper.writeValueAsString(request);
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = requestAsString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            return connection.getInputStream();
        } finally {
            logExecutionTime("File Stream", url, startTime);
        }
    }

    @Override
    public String externalPostUploadAPICall(String url, MultipartFile multipartFile, Map<String, String> valueMap, Map<String, String> requestHeader) throws IOException, BaseException, ParseException {
        log.info("{}Starting file upload to URL: {}", LOG_PREFIX, url);
        long startTime = System.nanoTime();
        try {
            validateMultipartFile(multipartFile);
            String user = optionalUserName();
            HttpHeaders headers = createUploadHeaders(user);
            String finalUrl = getFinalURL(url, valueMap);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = createFileUploadEntity(multipartFile, headers);
            return executeFileUpload(finalUrl, requestEntity);
        } finally {
            logExecutionTime("File Upload", url, startTime);
        }
    }

    private void validateMultipartFile(MultipartFile multipartFile) {
        if (multipartFile == null || multipartFile.isEmpty()) {
            log.error("{}Multipart file is null or empty", LOG_PREFIX);
            throw new IllegalArgumentException("Multipart file cannot be null or empty");
        }
    }

    private HttpHeaders createUploadHeaders(String user) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        String catalogId = httpServletRequest.getHeader(CATALOG_ID);
        headers.add(CATALOG_ID, DEFAULT.equalsIgnoreCase(catalogId) ? null : catalogId);
        headers.add(Constants.TENANT_ID, tenantId);
        headers.add(TRACE_ID, UUID.randomUUID().toString());
        headers.add(USER_NAME, user);
        return headers;
    }

    private HttpEntity<MultiValueMap<String, Object>> createFileUploadEntity(MultipartFile multipartFile, HttpHeaders headers) throws IOException {
        ContentDisposition contentDisposition = ContentDisposition
                .builder("form-data")
                .name("file")
                .filename(Objects.requireNonNull(multipartFile.getOriginalFilename()))
                .build();

        MultiValueMap<String, String> fileMap = new LinkedMultiValueMap<>();
        fileMap.add(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString());
        HttpEntity<byte[]> fileEntity = new HttpEntity<>(multipartFile.getBytes(), fileMap);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", fileEntity);

        return new HttpEntity<>(body, headers);
    }

    private String executeFileUpload(String finalUrl, HttpEntity<MultiValueMap<String, Object>> requestEntity) throws IOException, ParseException {
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(finalUrl, requestEntity, String.class);
            log.info("{}File upload response status: {}", LOG_PREFIX, response.getStatusCode());
            return response.getBody();
        } catch (HttpStatusCodeException e) {
            return handleFileUploadError(e);
        }
    }

    private String handleFileUploadError(HttpStatusCodeException e) throws ParseException {
        HttpStatusCode status = e.getStatusCode();
        if (status.value() == HttpStatus.BAD_REQUEST.value() || status.value() == HttpStatus.NOT_FOUND.value()) {
            String responseAsString = e.getResponseBodyAsString();
            if (!responseAsString.isEmpty()) {
                JSONObject responseAsJson = (JSONObject) new JSONParser().parse(responseAsString);
                log.error("{}Error response from file upload: {}", LOG_PREFIX, responseAsJson);
                JSONObject responseHeaderVal = (JSONObject) responseAsJson.get("responseHeader");
                String responseDesc = responseHeaderVal != null ? 
                    (String) responseHeaderVal.get("message") : 
                    (String) responseAsJson.get("message");
                Object data = responseHeaderVal != null ? 
                    responseHeaderVal.get("data") : 
                    responseAsJson.get("data");
                throw new ClientException(responseDesc, responseDesc, HttpStatus.valueOf(status.value()), "400", e.getStackTrace(), data);
            }
        }
        throw e;
    }

    private String getFinalURL(String url, Map<String, String> valueMap) {
        log.debug("{}Building URL from base: {}", LOG_PREFIX, url);
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
        if (Objects.nonNull(valueMap) && !valueMap.isEmpty()) {
            valueMap.forEach(builder::queryParam);
        }
        String finalUrl = builder.build().toUriString();
        log.debug("{}Final URL: {}", LOG_PREFIX, finalUrl);
        return finalUrl;
    }

    @SneakyThrows
    private String invokeRestApi(String url, HttpMethod method, HttpEntity<?> requestEntity) throws BaseException, ParseException {
        int maxRetries = 3;
        int retryCount = 0;
        ResourceAccessException lastException = null;

        while (retryCount < maxRetries) {
            try {
                ResponseEntity<String> response = restTemplate.exchange(url, method, requestEntity, String.class);

                // Defensive null check for response body
                String responseAsString = response.getBody();
                if (responseAsString == null || responseAsString.isEmpty()) {
                    log.warn("{}API response body is null or empty for {} {}", LOG_PREFIX, method, url);
                    responseAsString = "{\"result\":{\"resultCode\":\"200\",\"resultDescription\":\"Success\"},\"responseData\":null}";
                }

                log.debug("{}API response received for {} {}: {}", LOG_PREFIX, method, url, responseAsString);

                CommonAdaptorResp commonAdaptorResp = objectMapper.readValue(responseAsString, CommonAdaptorResp.class);
                CommonNorthBoundResponse<Object> convertedResponse = convert(commonAdaptorResp);
                return objectMapper.writeValueAsString(convertedResponse);

            } catch (HttpStatusCodeException e) {
                // HTTP errors should not be retried
                return handleHttpError(e);

            } catch (ResourceAccessException e) {
                lastException = e;
                retryCount++;

                // Check if it's a connection pool issue
                String errorMessage = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
                boolean isConnectionIssue = errorMessage.contains("connection") ||
                                           errorMessage.contains("timeout") ||
                                           errorMessage.contains("i/o error");

                if (isConnectionIssue && retryCount < maxRetries) {
                    log.warn("{}Connection issue detected (attempt {}/{}): {} - Retrying...",
                        LOG_PREFIX, retryCount, maxRetries, e.getMessage());

                    // Exponential backoff: 100ms, 200ms, 400ms
                    try {
                        Thread.sleep(100L * (long)(1 << (retryCount - 1)));
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw exceptionHandler.clientExceptionHandler(e, DisplayResultCodeEnum.RESOURCE_ACCESS_EXCEPTION);
                    }
                } else {
                    // Not a retryable error or max retries reached
                    log.error("{}Resource access error for {} {} (attempt {}/{}): {}",
                        LOG_PREFIX, method, url, retryCount, maxRetries, e.getMessage());
                    throw exceptionHandler.clientExceptionHandler(e, DisplayResultCodeEnum.RESOURCE_ACCESS_EXCEPTION);
                }

            } catch (Exception e) {
                // Unexpected error - log and rethrow
                log.error("{}Unexpected error during API call to {} {}: {}",
                    LOG_PREFIX, method, url, e.getMessage(), e);
                throw e;
            }
        }

        // Max retries reached
        log.error("{}Max retries ({}) reached for {} {}. Last error: {}",
            LOG_PREFIX, maxRetries, method, url, lastException != null ? lastException.getMessage() : "Unknown");
        throw exceptionHandler.clientExceptionHandler(lastException, DisplayResultCodeEnum.RESOURCE_ACCESS_EXCEPTION);
    }

    private String handleHttpError(HttpStatusCodeException e) throws ParseException, JsonProcessingException {
        HttpStatusCode status = e.getStatusCode();
        String responseAsString = e.getResponseBodyAsString();

        if ((status.value() == HttpStatus.BAD_REQUEST.value() || status.value() == HttpStatus.NOT_FOUND.value() || status.value() == HttpStatus.FORBIDDEN.value()) && (!responseAsString.isEmpty())) {
            JsonNode responseJsonNode = objectMapper.readTree(e.getResponseBodyAsString());
            log.error("{} | Error response from API: {}", LOG_PREFIX, responseJsonNode);

            // Case 1: message exists
            if(responseJsonNode.get("message") != null && responseJsonNode.get("data") != null){
                String responseDesc = responseJsonNode.get("message").asText();
                Object data = responseJsonNode.get("data");
                throw new ClientException(responseDesc, responseDesc, HttpStatus.valueOf(status.value()), "400", e.getStackTrace(), data);
            }

            // Case 2: responseHeader exists
            if(responseJsonNode.get("responseHeader") != null && responseJsonNode.get("responseHeader").get("message") != null && responseJsonNode.get("responseHeader").get("data") != null){
                String responseDesc = responseJsonNode.get("responseHeader").get("message").asText();
                Object data = responseJsonNode.get("responseHeader").get("data");
                throw new ClientException(responseDesc, responseDesc, HttpStatus.valueOf(status.value()), "401", e.getStackTrace(), data);
            }

            // Case 3: result exists
            if(responseJsonNode.get("result") != null && responseJsonNode.get("result").get("resultCode") != null && responseJsonNode.get("result").get("resultDescription") != null){
                String responseDesc = responseJsonNode.get("result").get("resultDescription").asText();
                String responseCode = responseJsonNode.get("result").get("resultCode").asText();
                Object data = responseJsonNode.get("responseData");
                throw new ClientException(responseDesc, responseDesc, HttpStatus.valueOf(status.value()), responseCode, e.getStackTrace(), data);
            }
        }
        throw e;
    }

    private CommonNorthBoundResponse<Object> convert(CommonAdaptorResp commonAdaptorResp) {
        CommonNorthBoundResponse<Object> response = new CommonNorthBoundResponse<>();
        Result result = commonAdaptorResp.getResult();
        if (result != null) {
            response.setCode(result.getResultCode());
            response.setMessage(result.getResultDescription());
            response.setDescription(result.getResultDescription());
            response.setPageDetail(result.getPageDetail());
        }
        response.setData(commonAdaptorResp.getResponseData());
        return response;
    }

    private HttpEntity<?> getHttpEntityWithHeaders(Object request, Map<String, String> requestHeader, MediaType contentType, MediaType acceptType) {
        HttpHeaders headers = new HttpHeaders();
        if (Objects.nonNull(contentType)) {
            headers.setContentType(contentType);
        }
        if (Objects.nonNull(acceptType)) {
            headers.setAccept(Collections.singletonList(acceptType));
        }
        headers.add(TRACE_ID, UUID.randomUUID().toString());
        headers.add(Constants.TENANT_ID, tenantId);
        if (Objects.nonNull(requestHeader.get(Constants.APPLICATION_ID))) {
            headers.add(Constants.APPLICATION_ID, requestHeader.get(Constants.APPLICATION_ID));
        } else {
            headers.add(Constants.APPLICATION_ID, Constants.DEFAULT_APPLICATION_ID);
        }

        Claims claims = optionalClaims();
        String userName = Objects.nonNull(claims) ? claims.getSubject() : null;
        String uName = Objects.nonNull(claims) ? claims.get(ServiceConstants.U_NAME, String.class) : null;

        if (Objects.nonNull(userName)) {
            CommonSouthBoundResponse<UserGroupDto> userGroupDetails = crmUserDetailClient.getUserGroupDetails(userName, String.valueOf(headers.get(Constants.APPLICATION_ID)));
            if (userGroupDetails != null && userGroupDetails.getResponseData() != null) {
                UserGroupDto userGroupDto = userGroupDetails.getResponseData();
                headers.add(Constants.USER_NAME, requestHeader.get(Constants.USER_NAME) != null? requestHeader.get(Constants.USER_NAME): uName);
                headers.add(Constants.X_USER, String.valueOf(userGroupDto.getUserId()));
                headers.add(Constants.X_ROLES, extractUserRoles(userGroupDto));
                headers.add(Constants.X_GROUP_LEVELS, extractUserGroups(userGroupDto));
            }
        }

        return new HttpEntity<>(request, headers);
    }

    /**
     * Reads the claims of the caller's JWT when one happens to be present.
     *
     * AUTHENTICATION BYPASS: a request without a token, or with a token that cannot
     * be parsed, is perfectly valid. In that case this returns null and the request
     * is forwarded without the user enrichment headers instead of being rejected.
     *
     * @return the token claims, or null when the request carries no readable token
     */
    private Claims optionalClaims() {
        try {
            String jwtToken = jwtService.tokenExtractor(httpServletRequest);
            if (Objects.isNull(jwtToken) || jwtToken.isBlank()) {
                return null;
            }
            return jwtService.extractAllClaims(jwtToken);
        } catch (Exception e) {
            log.debug("{}No readable token on the request, forwarding without user details: {}", LOG_PREFIX, e.getMessage());
            return null;
        }
    }

    /**
     * @return the caller's username when a readable token is present, otherwise null
     */
    private String optionalUserName() {
        Claims claims = optionalClaims();
        return Objects.nonNull(claims) ? claims.getSubject() : null;
    }

    private void logExecutionTime(String operation, String url, long startTime) {
        long duration = Duration.ofNanos(System.nanoTime() - startTime).toMillis();
        log.info("{}{} operation completed for URL: {} in {} ms", LOG_PREFIX, operation, url, duration);
    }



    private String extractUserRoles(UserGroupDto userGroupDto) {
        return userGroupDto.getRoleIds().stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
    }

    private String extractUserGroups(UserGroupDto userGroupDto) {
        return userGroupDto.getGroups().stream()
                .flatMap(group -> group.getLevels().stream()
                        .map(level -> group.getGroupId() + "." + level.getLevelId()))
                .collect(Collectors.joining(", "));
    }

}
