package com.adl.et.telco.crm.securerequesthandler.domain.service.serviceimpl.common;

import com.adl.et.telco.crm.securerequesthandler.external.repository.cache.RoutingCacheRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MicroServiceURLFetchServiceTest {

    @Mock
    private RoutingCacheRepository routingCacheRepository;

    @InjectMocks
    private MicroServiceURLFetchService microServiceURLFetchService;

    private static final String REDIS_ROUTING_CRM_EXTENSION = "CRM_ROUTING_CRM_EXTENSION";
    private static final String REDIS_KEY_ROUTING = "CRM_TELCO_DEV_ROUTING";

    @Test
    void checkAndReturnURL_shouldReturnValidUrl() {
        // Arrange
        String uri = "/service/module/endpoint";
        Map<String, String> params = Map.of();
        String returnUrl = "http://service/module/";
        String expectedUrl = returnUrl + "endpoint";

        when(routingCacheRepository.findByKey(REDIS_ROUTING_CRM_EXTENSION, "/service/module/"))
                .thenReturn(returnUrl);

        // Act
        String result = microServiceURLFetchService.checkAndReturnURL(uri, params);

        // Assert
        assertEquals(expectedUrl , result);
    }

    @Test
    void checkAndReturnURL_shouldThrowForInvalidUriFormat() {
        // Arrange
        String invalidUri = "/too/short";
        Map<String, String> params = Map.of();

        // Act & Assert
        HttpClientErrorException exception = assertThrows(
                HttpClientErrorException.class,
                () -> microServiceURLFetchService.checkAndReturnURL(invalidUri, params)
        );
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void checkAndReturnURL_shouldThrowWhenNoRoutingConfig() {
        // Arrange
        String uri = "/service/module/endpoint";
        Map<String, String> params = Map.of();

        when(routingCacheRepository.findByKey(REDIS_ROUTING_CRM_EXTENSION, "/service/module/"))
                .thenReturn(null);

        // Act & Assert
        HttpClientErrorException exception = assertThrows(
                HttpClientErrorException.class,
                () -> microServiceURLFetchService.checkAndReturnURL(uri, params)
        );
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    }

    @Test
    void findAndReturnURL_shouldReturnDirectMatch() {
        // Arrange
        String uri = "/service/module/endpoint";
        Map<String, String> params = Map.of();
        String expectedUrl = "http://service/module/endpoint";

        when(routingCacheRepository.findByKey(REDIS_KEY_ROUTING, uri))
                .thenReturn(expectedUrl);

        // Act
        String result = microServiceURLFetchService.findAndReturnURL(uri, params);

        // Assert
        assertEquals(expectedUrl, result);
    }

    @Test
    void findAndReturnURL_shouldHandlePathParameters() {
        // Arrange
        String uri = "/service/module/endpoint";
        Map<String, String> params = Map.of();
        String baseUrl = "http://service/module/endpoint";
        String expectedUrl = baseUrl + "/endpoint";

        when(routingCacheRepository.findByKey(REDIS_KEY_ROUTING, uri))
                .thenReturn(null);
        when(routingCacheRepository.findByKey(REDIS_KEY_ROUTING, "/service/module"))
                .thenReturn(baseUrl);

        // Act
        String result = microServiceURLFetchService.findAndReturnURL(uri, params);

        // Assert
        assertEquals(expectedUrl, result);
    }

    @Test
    void findAndReturnURL_shouldThrowWhenNoRoutingConfig() {
        // Arrange
        String uri = "/service/module/endpoint";
        Map<String, String> params = Map.of();

        when(routingCacheRepository.findByKey(REDIS_KEY_ROUTING, uri))
                .thenReturn(null);
        when(routingCacheRepository.findByKey(REDIS_KEY_ROUTING, "/service/module"))
                .thenReturn(null);

        // Act & Assert
        HttpClientErrorException exception = assertThrows(
                HttpClientErrorException.class,
                () -> microServiceURLFetchService.findAndReturnURL(uri, params)
        );
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    }
}