package com.adl.et.telco.crm.securerequesthandler.domain.service.serviceimpl.access;

import com.adl.et.telco.crm.securerequesthandler.domain.dto.common.CommonNorthBoundResponse;
import com.adl.et.telco.crm.securerequesthandler.domain.dto.security.SamlRequest;
import com.adl.et.telco.crm.securerequesthandler.application.util.ResponseHandler;
import com.adl.et.telco.crm.securerequesthandler.application.util.exception.BaseException;
import com.adl.et.telco.crm.securerequesthandler.application.util.resultenum.AuthCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

@Service
@Slf4j
public class UserAuthenticationManageServiceImpl implements UserAuthenticationManageService {
    private static final String LOG_PREFIX = "SRH|UserAuthenticationManageService|";
    private static final String START = "START";
    private static final String ERROR = "ERROR";
    private static final String SUCCESS = "SUCCESS";

    private final ResponseHandler handler;
    private final long expiryTime;
    private final String azureTenantId;
    private final String issuerUrl;
    private final String redirectionUrl;

    public UserAuthenticationManageServiceImpl(
            ResponseHandler handler,
            @Value("${saml.request.requestId.cache.time.sec}") long expiryTime,
            @Value("${azure.ad.tenant-id}") String azureTenantId,
            @Value("${saml.request.issuer.url}") String issuerUrl,
            @Value("${saml.request.redirection.url}") String redirectionUrl) {
        this.handler = handler;
        this.expiryTime = expiryTime;
        this.azureTenantId = azureTenantId;
        this.issuerUrl = issuerUrl;
        this.redirectionUrl = redirectionUrl;
    }

    /**
     * Generates SAML request and redirection URL for authentication.
     *
     * @param uuid Unique identifier for the request
     * @return CommonNorthBoundResponse containing the SAML request details
     * @throws BaseException if there's an error during SAML request generation
     */
    public CommonNorthBoundResponse<SamlRequest> createSamlRequest(String uuid) throws BaseException {
        log.info("{}|{}|Creating SAML request for UUID: {}", LOG_PREFIX, START, uuid);
        try {
            SamlRequest samlRequest = new SamlRequest();
            String requestId = UUID.randomUUID().toString();
            log.debug("{}|Generated request ID: {}", LOG_PREFIX, requestId);
            
            DateTime issueInstant = new DateTime();
            String uriEncode = generateSamlRequest(requestId, issueInstant);
            log.debug("{}|Generated SAML request URI encoding", LOG_PREFIX);
            
            samlRequest.setUrl(getRedirectionUrl(uriEncode));
            log.info("{}|{}|Successfully created SAML request with redirection URL|{}", LOG_PREFIX, SUCCESS, expiryTime);
            
            return handler.responseBuilder(
                samlRequest,
                AuthCodeEnum.AUTH_REQUEST_SUCCESS.description(),
                AuthCodeEnum.AUTH_REQUEST_SUCCESS.code()
            );
        } catch (BaseException ex) {
            log.error("{}|{}|BaseException while creating SAML request: {}", LOG_PREFIX, ERROR, ex.getMessage(), ex);
            throw new BaseException(
                ex.getMessage(),
                ex.getReason(),
                ex.getHttpStatus(),
                ex.getResultCode(),
                ex.getStackTraceElements()
            );
        } catch (Exception ex) {
            log.error("{}|{}|Unexpected error while creating SAML request: {}", LOG_PREFIX, ERROR, ex.getMessage(), ex);
            throw new BaseException(
                ex.getMessage(),
                AuthCodeEnum.LOGOUT_INTERNAL_SERVER_ERROR.description(),
                HttpStatus.INTERNAL_SERVER_ERROR,
                AuthCodeEnum.LOGOUT_INTERNAL_SERVER_ERROR.code(),
                ex.getStackTrace()
            );
        }
    }

    /**
     * Generates redirection URL by substituting SAML request and application ID.
     *
     * @param uriEncode Encoded SAML request
     * @return Complete redirection URL
     */
    private String getRedirectionUrl(String uriEncode) {
        log.debug("{}|{}|Building redirection URL with tenant ID: {}", LOG_PREFIX, START, azureTenantId);
        Map<String, String> urlParams = new HashMap<>();
        urlParams.put("tenantId", azureTenantId);
        urlParams.put("uriEncode", uriEncode);
        
        String url = UriComponentsBuilder.fromUriString(redirectionUrl)
            .buildAndExpand(urlParams)
            .toString();
            
        log.debug("{}|Generated redirection URL: {}", LOG_PREFIX, url);
        return url;
    }

    /**
     * Generates SAML request with the given parameters.
     *
     * @param requestId Unique identifier for the request
     * @param issueInstant Timestamp of request creation
     * @return Encoded SAML request
     * @throws IOException if there's an error during encoding
     */
    private String generateSamlRequest(String requestId, DateTime issueInstant) throws IOException {
        log.debug("{}|{}|Generating SAML request with request ID: {} and issue instant: {}", 
            LOG_PREFIX, START, requestId, issueInstant);
            
        Map<String, String> values = new HashMap<>();
        values.put("requestId", requestId);
        values.put("issueInstant", issueInstant.toString());
        values.put("issuerUrl", issuerUrl);
        
        String requestTemplate = StrSubstitutor.replace(
            new StringBuilder()
                .append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
                .append("<samlp:AuthnRequest xmlns:samlp=\"urn:oasis:names:tc:SAML:2.0:protocol\" ")
                .append("xmlns=\"urn:oasis:names:tc:SAML:2.0:metadata\" ")
                .append("ID=\"id${requestId}\" Version=\"2.0\" IssueInstant=\"${issueInstant}\">\n")
                .append("<Issuer xmlns=\"urn:oasis:names:tc:SAML:2.0:assertion\">${issuerUrl}</Issuer>\n")
                .append("</samlp:AuthnRequest>")
                .toString(),
            values,
            "${", "}"
        );

        log.debug("{}|Generated SAML request template", LOG_PREFIX);

        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             DeflaterOutputStream deflaterOutputStream = new DeflaterOutputStream(
                 byteArrayOutputStream,
                 new Deflater(Deflater.DEFLATED, true)
             )) {
            deflaterOutputStream.write(requestTemplate.getBytes());

            String encodedBytes = Base64.encodeBase64String(byteArrayOutputStream.toByteArray());
            log.debug("{}|Successfully encoded SAML request", LOG_PREFIX);
            return URLEncoder.encode(encodedBytes, StandardCharsets.UTF_8.toString());
        } catch (IOException e) {
            log.error("{}|{}|Failed to encode SAML request: {}", LOG_PREFIX, ERROR, e.getMessage(), e);
            throw e;
        }
    }
}

