package com.adl.et.telco.crm.securerequesthandler.application.client;

import com.adl.et.telco.crm.securerequesthandler.domain.dto.RequestContextDetail;
import com.adl.et.telco.crm.securerequesthandler.domain.dto.common.CommonSouthBoundResponse;
import com.adl.et.telco.crm.securerequesthandler.application.util.constants.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collections;

/**
 * Base client class providing common functionality for all HTTP clients.
 * Handles header population and request entity creation with proper logging.
 */
public abstract class BaseClient {
    private static final Logger logger = LoggerFactory.getLogger(BaseClient.class);
    private static final String LOG_PREFIX = "SRH|BaseClient|";

    @Value("${tenant-id}")
    private String tenantId;

    /**
     * Populates basic HTTP headers with correlation ID and content type.
     * @return HttpHeaders with basic configuration
     */
    public HttpHeaders populateHeaders() {
        logger.debug("{}populateHeaders|Start", LOG_PREFIX);
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Correlation-Id", RequestContextDetail.getRequestID());
        headers.setContentType(MediaType.APPLICATION_JSON);
        logger.debug("{}populateHeaders|End|Headers: {}", LOG_PREFIX, headers);
        return headers;
    }

    /**
     * Creates a request entity with null body and populated headers.
     * @return HttpEntity with headers and null body
     */
    public HttpEntity<String> populateRequestEntity() {
        logger.debug("{}populateRequestEntity|Start", LOG_PREFIX);
        HttpHeaders headers = populateHeadersWithTenant();
        HttpEntity<String> requestEntity = new HttpEntity<>(null, headers);
        logger.debug("{}populateRequestEntity|End|Entity: {}", LOG_PREFIX, requestEntity);
        return requestEntity;
    }

    /**
     * Creates a request entity with provided body and populated headers.
     * @param body The request body
     * @param <T> Type of the request body
     * @return HttpEntity with headers and provided body
     */
    public <T> HttpEntity<T> populateRequestEntity(T body) {
        logger.debug("{}populateRequestEntity|Start|BodyType: {}", LOG_PREFIX, body != null ? body.getClass().getSimpleName() : "null");
        HttpHeaders headers = populateHeadersWithTenant();
        HttpEntity<T> requestEntity = new HttpEntity<>(body, headers);
        logger.debug("{}populateRequestEntity|End|Entity: {}", LOG_PREFIX, requestEntity);
        return requestEntity;
    }

    /**
     * Populates HTTP headers with tenant ID, correlation ID, and content type.
     * @return HttpHeaders with tenant configuration
     */
    public HttpHeaders populateHeadersWithTenant() {
        logger.debug("{}populateHeadersWithTenant|Start|TenantId: {}", LOG_PREFIX, tenantId);
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Correlation-Id", RequestContextDetail.getRequestID());
        headers.set(Constants.TENANT_ID, tenantId);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        logger.debug("{}populateHeadersWithTenant|End|Headers: {}", LOG_PREFIX, headers);
        return headers;
    }

    /**
     * Custom ParameterizedTypeReference implementation for handling CommonSouthBoundResponse.
     * @param <T> The response type
     */
    @SuppressWarnings("EqualsHashCode")
    protected static class CustomParameterizedTypeReference<T> extends ParameterizedTypeReference<CommonSouthBoundResponse<T>> {
        private final Type responseType;

        public CustomParameterizedTypeReference() {
            logger.debug("{}CustomParameterizedTypeReference|Constructor|Start", LOG_PREFIX);
            ParameterizedType genericSuperclass = (ParameterizedType) getClass().getGenericSuperclass();
            this.responseType = genericSuperclass.getActualTypeArguments()[0];
            logger.debug("{}CustomParameterizedTypeReference|Constructor|End|ResponseType: {}", LOG_PREFIX, responseType);
        }

        @Override
        public Type getType() {
            return this.responseType;
        }
    }
}

