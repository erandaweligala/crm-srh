package com.adl.et.telco.crm.securerequesthandler.domain.service.serviceimpl.common;

import com.adl.et.telco.crm.securerequesthandler.domain.dto.common.CommonAdaptorResp;
import com.adl.et.telco.crm.securerequesthandler.domain.dto.common.CommonNorthBoundResponse;
import com.adl.et.telco.crm.securerequesthandler.domain.dto.common.Result;
import com.adl.et.telco.crm.securerequesthandler.application.exception.ClientException;
import com.adl.et.telco.crm.securerequesthandler.domain.service.serviceimpl.access.JwtService;
import com.adl.et.telco.crm.securerequesthandler.domain.service.serviceinterface.common.ExternalCrmExtensionAPICallService;
import com.adl.et.telco.crm.securerequesthandler.application.util.constants.Constants;
import com.adl.et.telco.crm.securerequesthandler.application.util.exception.BaseException;
import com.adl.et.telco.crm.securerequesthandler.application.util.exception.ExceptionHandler;
import com.adl.et.telco.crm.securerequesthandler.application.util.resultenum.DisplayResultCodeEnum;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
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

@Service
@Slf4j
public class ExternalCrmExtensionAPICallServiceImpl implements ExternalCrmExtensionAPICallService {
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
    public ExternalCrmExtensionAPICallServiceImpl(RestTemplate restTemplate,
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
    }

    @Override
    public String externalGetAPICall(String url, Map<String, String> valueMap) throws BaseException, ParseException {
        log.info("SRH|Starting GET API call to URL: {}", url);
        long startTime = System.nanoTime();
        try {
            String finalUrl = getFinalURL(url, valueMap);
            HttpEntity<?> requestEntity = getHttpEntityWithHeaders(null, MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON);
            return invokeRestApi(finalUrl, HttpMethod.GET, requestEntity);
        } finally {
            logExecutionTime("GET", url, startTime);
        }
    }

    @Override
    public String externalPostAPICall(String url, Object request, Map<String, String> valueMap) throws BaseException, ParseException {
        log.info("SRH|Starting POST API call to URL: {}", url);
        long startTime = System.nanoTime();
        try {
            HttpEntity<?> requestEntity = getHttpEntityWithHeaders(request, MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON);
            String finalUrl = getFinalURL(url, valueMap);
            return invokeRestApi(finalUrl, HttpMethod.POST, requestEntity);
        } finally {
            logExecutionTime("POST", url, startTime);
        }
    }

    @Override
    public String externalPutAPICall(String url, Object request, Map<String, String> valueMap) throws BaseException, ParseException {
        log.info("SRH|Starting PUT API call to URL: {}", url);
        long startTime = System.nanoTime();
        try {
            HttpEntity<?> requestEntity = getHttpEntityWithHeaders(request, MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON);
            String finalUrl = getFinalURL(url, valueMap);
            return invokeRestApi(finalUrl, HttpMethod.PUT, requestEntity);
        } finally {
            logExecutionTime("PUT", url, startTime);
        }
    }

    @Override
    public String externalDeleteAPICall(String url, Object request, Map<String, String> valueMap) throws BaseException, ParseException {
        log.info("SRH|Starting DELETE API call to URL: {}", url);
        long startTime = System.nanoTime();
        try {
            HttpEntity<?> requestEntity = getHttpEntityWithHeaders(request, MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON);
            String finalUrl = getFinalURL(url, valueMap);
            return invokeRestApi(finalUrl, HttpMethod.DELETE, requestEntity);
        } finally {
            logExecutionTime("DELETE", url, startTime);
        }
    }

    @Override
    public InputStream externalGetFileAsAStreamForPostEndPoint(String url, Object request, Map<String, String> valueMap) throws IOException {
        log.info("SRH|Starting file stream request to URL: {}", url);
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
    public String externalPostUploadAPICall(String url, MultipartFile multipartFile, Map<String, String> valueMap) throws IOException, BaseException, ParseException {
        log.info("SRH|Starting file upload to URL: {}", url);
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
            log.error("SRH|Multipart file is null or empty");
            throw new IllegalArgumentException("Multipart file cannot be null or empty");
        }
    }

    /**
     * Reads the caller's username from the JWT when one happens to be present.
     *
     * AUTHENTICATION BYPASS: a request without a token, or with a token that cannot
     * be parsed, is perfectly valid. In that case this returns null and the request
     * is forwarded without the user name header instead of being rejected.
     *
     * @return the caller's username, or null when the request carries no readable token
     */
    private String optionalUserName() {
        try {
            String jwtToken = jwtService.tokenExtractor(httpServletRequest);
            if (Objects.isNull(jwtToken) || jwtToken.isBlank()) {
                return null;
            }
            Claims claims = jwtService.extractAllClaims(jwtToken);
            return Objects.nonNull(claims) ? claims.getSubject() : null;
        } catch (Exception e) {
            log.debug("SRH|No readable token on the request, forwarding without user details: {}", e.getMessage());
            return null;
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
            log.info("SRH|File upload response status: {}", response.getStatusCode());
            return response.getBody();
        } catch (HttpStatusCodeException e) {
            return handleFileUploadError(e);
        }
    }

    private String handleFileUploadError(HttpStatusCodeException e) throws ParseException {
        HttpStatus status = HttpStatus.valueOf(e.getStatusCode().value());
        if (HttpStatus.BAD_REQUEST.equals(status) || HttpStatus.NOT_FOUND.equals(status)) {
            String responseAsString = e.getResponseBodyAsString();
            if (!responseAsString.isEmpty()) {
                JSONObject responseAsJson = (JSONObject) new JSONParser().parse(responseAsString);
                log.error("SRH|Error response from file upload: {}", responseAsJson);
                JSONObject responseHeaderVal = (JSONObject) responseAsJson.get("responseHeader");
                String responseDesc = responseHeaderVal != null ? 
                    (String) responseHeaderVal.get("message") : 
                    (String) responseAsJson.get("message");
                Object data = responseHeaderVal != null ? 
                    responseHeaderVal.get("data") : 
                    responseAsJson.get("data");
                throw new ClientException(responseDesc, responseDesc, status, "400", e.getStackTrace(), data);
            }
        }
        throw e;
    }

    private String getFinalURL(String url, Map<String, String> valueMap) {
        log.debug("SRH|Building URL from base: {}", url);
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
        if (Objects.nonNull(valueMap) && !valueMap.isEmpty()) {
            valueMap.forEach(builder::queryParam);
        }
        String finalUrl = builder.toUriString();
        log.debug("SRH|Final URL: {}", finalUrl);
        return finalUrl;
    }

    private String invokeRestApi(String url, HttpMethod method, HttpEntity<?> requestEntity) throws BaseException, ParseException {
        try {
            ResponseEntity<String> response = restTemplate.exchange(url, method, requestEntity, String.class);
            String responseAsString = response.getBody();
            log.debug("SRH|API response received for {} {}: {}", method, url, responseAsString);
            return responseAsString;
        } catch (HttpStatusCodeException e) {
            return handleHttpError(e);
        }
    }

    private String handleHttpError(HttpStatusCodeException e) throws ParseException {
        HttpStatus status = HttpStatus.valueOf(e.getStatusCode().value());
        if (HttpStatus.BAD_REQUEST.equals(status) || HttpStatus.NOT_FOUND.equals(status)) {
            String responseAsString = e.getResponseBodyAsString();
            if (!responseAsString.isEmpty()) {
                JSONObject responseAsJson = (JSONObject) new JSONParser().parse(responseAsString);
                log.error("SRH|Error response: {}", responseAsJson);
                throw exceptionHandler.clientExceptionHandler(e, DisplayResultCodeEnum.GET_USER_BASIC_INFO_FAILED);
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

    private HttpEntity<?> getHttpEntityWithHeaders(Object request, MediaType contentType, MediaType acceptType) {
        HttpHeaders headers = new HttpHeaders();
        if (Objects.nonNull(contentType)) {
            headers.setContentType(contentType);
        }
        if (Objects.nonNull(acceptType)) {
            headers.setAccept(Collections.singletonList(acceptType));
        }
        headers.add(TRACE_ID, UUID.randomUUID().toString());
        headers.add(Constants.TENANT_ID, tenantId);
        return new HttpEntity<>(request, headers);
    }

    private void logExecutionTime(String operation, String url, long startTime) {
        long duration = Duration.ofNanos(System.nanoTime() - startTime).toMillis();
        log.info("SRH|{} operation completed for URL: {} in {} ms", operation, url, duration);
    }
}
