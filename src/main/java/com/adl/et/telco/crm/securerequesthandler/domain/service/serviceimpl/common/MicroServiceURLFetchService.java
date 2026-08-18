package com.adl.et.telco.crm.securerequesthandler.domain.service.serviceimpl.common;

import com.adl.et.telco.crm.securerequesthandler.external.repository.cache.RoutingCacheRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Map;
import java.util.Objects;

import static com.adl.et.telco.crm.securerequesthandler.application.util.constants.Constants.REDIS_KEY_ROUTING;
import static com.adl.et.telco.crm.securerequesthandler.application.util.constants.Constants.REDIS_ROUTING_CRM_EXTENSION;

@Service
@Slf4j
public class MicroServiceURLFetchService {
    private static final String LOG_PREFIX = "SRH|MicroServiceURLFetchService|";
    private static final String INVALID_URI_FORMAT = "Invalid URI format";
    private static final String NO_ROUTING_CONFIG = "No routing configuration found for the given URI";
    private static final String SLASH = "/";
    private static final String EMPTY = "";

    private final RoutingCacheRepository routingCacheRepository;

    @Autowired
    public MicroServiceURLFetchService(RoutingCacheRepository routingCacheRepository) {
        this.routingCacheRepository = routingCacheRepository;
    }

    public String checkAndReturnURL(String uri, Map<String, String> requestParams) throws HttpClientErrorException {
        log.debug("{}checkAndReturnURL|START|Processing URI: {}|{}", LOG_PREFIX, uri, requestParams);
        
        String[] parts = uri.split(SLASH);
        if (parts.length < 4) {
            log.error("{}checkAndReturnURL|ERROR|Invalid URI format: {}", LOG_PREFIX, uri);
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, INVALID_URI_FORMAT);
        }

        String baseUri = String.format("%s%s%s%s%s", SLASH, parts[1], SLASH, parts[2], SLASH, parts[3]);
        String lastExtensions = uri.contains(baseUri) ? uri.replace(baseUri, EMPTY) : EMPTY;
        
        log.debug("{}checkAndReturnURL|INFO|Base URI: {}, Last extensions: {}", LOG_PREFIX, baseUri, lastExtensions);
        
        String microServiceUrl = routingCacheRepository.findByKey(REDIS_ROUTING_CRM_EXTENSION, baseUri);
        if (microServiceUrl != null) {
            String finalUrl = microServiceUrl + lastExtensions;
            log.debug("{}checkAndReturnURL|END|Final URL: {}", LOG_PREFIX, finalUrl);
            return finalUrl;
        } else {
            log.error("{}checkAndReturnURL|ERROR|No routing configuration found for base URI: {}", LOG_PREFIX, baseUri);
            throw new HttpClientErrorException(HttpStatus.FORBIDDEN, NO_ROUTING_CONFIG);
        }
    }

    public String findAndReturnURL(String uri, Map<String, String> requestParams) throws HttpClientErrorException {
        log.debug("{}findAndReturnURL|START|Processing URI: {}|{}", LOG_PREFIX, uri, requestParams);
        
        String microServiceUrl = routingCacheRepository.findByKey(REDIS_KEY_ROUTING, uri);
        
        if (Objects.isNull(microServiceUrl)) {
            log.debug("{}findAndReturnURL|INFO|No direct match found, trying with path parameters", LOG_PREFIX);
            String pathParam = findPathParam(uri);
            String result = uri.substring(0, uri.lastIndexOf(SLASH));
            microServiceUrl = routingCacheRepository.findByKey(REDIS_KEY_ROUTING, result);
            
            if (Objects.nonNull(microServiceUrl)) {
                microServiceUrl = addPathParamsToUrl(microServiceUrl, pathParam);
                log.debug("{}findAndReturnURL|INFO|URL found with path parameters: {}", LOG_PREFIX, microServiceUrl);
            }
        }

        return microServiceUrl;

        /* TODO - commented this because of demo 02/09/2025
        if (Objects.nonNull(microServiceUrl)) {
            log.debug("{}findAndReturnURL|END|Final URL: {}", LOG_PREFIX, microServiceUrl);
            return microServiceUrl;
        } else {
            log.error("{}findAndReturnURL|ERROR|No routing configuration found for URI: {}", LOG_PREFIX, uri);
            throw new HttpClientErrorException(HttpStatus.FORBIDDEN, NO_ROUTING_CONFIG);
        }
         */
    }

    private String findPathParam(String input) {
        log.debug("{}findPathParam|START|Extracting path parameter from: {}", LOG_PREFIX, input);
        int lastSlashIndex = input.lastIndexOf(SLASH);
        String pathParam = input.substring(lastSlashIndex + 1);
        log.debug("{}findPathParam|END|Path parameter: {}", LOG_PREFIX, pathParam);
        return pathParam;
    }

    private String addPathParamsToUrl(String url, Object value) {
        log.debug("{}addPathParamsToUrl|START|Adding path parameter {} to URL: {}", LOG_PREFIX, value, url);
        String result = url + SLASH + value;
        log.debug("{}addPathParamsToUrl|END|Updated URL: {}", LOG_PREFIX, result);
        return result;
    }

    private String updateMicroserviceURL(String microServiceUrl, String apiUrl) {
        log.debug("{}updateMicroserviceURL|START|Updating microservice URL: {} with API URL: {}", LOG_PREFIX, microServiceUrl, apiUrl);
        StringBuilder param = new StringBuilder();
        
        for (int i = apiUrl.length() - 1; i > 0; i--) {
            char c = apiUrl.charAt(i);
            if (c == '/') {
                break;
            }
            param.append(c);
        }
        
        String result = microServiceUrl + SLASH + param.reverse().toString();
        log.debug("{}updateMicroserviceURL|END|Updated URL: {}", LOG_PREFIX, result);
        return result;
    }
}
