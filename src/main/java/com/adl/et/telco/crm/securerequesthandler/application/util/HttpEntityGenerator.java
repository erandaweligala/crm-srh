package com.adl.et.telco.crm.securerequesthandler.application.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

/**
 * Utility class for generating HTTP headers with common configurations.
 * Provides methods to create standardized HTTP headers for API requests.
 */
@Component
public class HttpEntityGenerator {
    private static final Logger logger = LoggerFactory.getLogger(HttpEntityGenerator.class);
    private static final String LOG_PREFIX = "SRH|HttpEntityGenerator|";

    /**
     * Generates HTTP headers with JSON content type.
     * 
     * @return HttpHeaders object configured with JSON content type
     */
    public HttpHeaders httpHeaderGenerator() {
        logger.info("{}httpHeaderGenerator|START|Generating HTTP headers", LOG_PREFIX);
        HttpHeaders headers = new HttpHeaders();
        
        try {
            headers.setContentType(MediaType.APPLICATION_JSON);
            logger.info("{}httpHeaderGenerator|END|HTTP headers generated with content type: {}", LOG_PREFIX, MediaType.APPLICATION_JSON);
        } catch (Exception e) {
            logger.error("{}httpHeaderGenerator|ERROR|Failed to set content type: {}", LOG_PREFIX, e.getMessage());
            // Return empty headers instead of null
            return new HttpHeaders();
        }
        
        return headers;
    }
}
