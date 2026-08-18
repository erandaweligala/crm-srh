package com.adl.et.telco.crm.securerequesthandler.domain.service.serviceimpl.common;

import com.adl.et.telco.crm.securerequesthandler.application.util.exception.ExceptionHandler;
import com.adl.et.telco.crm.securerequesthandler.domain.service.serviceimpl.access.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExternalCrmExtensionAPICallServiceImplTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private ExceptionHandler exceptionHandler;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private ExternalCrmExtensionAPICallServiceImpl service;

    @Test
    void externalGetAPICall_shouldReturnResponse() throws Exception {
        // Arrange
        String url = "http://test.com";
        Map<String, String> params = Map.of("key", "value");
        ResponseEntity<String> mockResponse = new ResponseEntity<>("success", HttpStatus.OK);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(String.class)))
                .thenReturn(mockResponse);

        // Act
        String result = service.externalGetAPICall(url, params);

        // Assert
        assertEquals("success", result);
    }

    @Test
    void externalPostAPICall_shouldReturnResponse() throws Exception {
        // Arrange
        String url = "http://test.com";
        Object request = new Object();
        Map<String, String> params = Map.of("key", "value");
        ResponseEntity<String> mockResponse = new ResponseEntity<>("success", HttpStatus.OK);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(String.class)))
                .thenReturn(mockResponse);

        // Act
        String result = service.externalPostAPICall(url, request, params);

        // Assert
        assertEquals("success", result);
    }

    @Test
    void externalPostAPICall_shouldHandleException() {
        // Arrange
        String url = "http://test.com";
        Object request = new Object();
        Map<String, String> params = Map.of("key", "value");

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(String.class)))
                .thenThrow(new RuntimeException("Test exception"));

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            service.externalPostAPICall(url, request, params);
        });
        assertEquals("Test exception", exception.getMessage());
    }

    @Test
    void externalPutAPICall_shouldReturnResponse() throws Exception {
        // Arrange
        String url = "http://test.com";
        Object request = new Object();
        Map<String, String> params = Map.of("key", "value");
        ResponseEntity<String> mockResponse = new ResponseEntity<>("success", HttpStatus.OK);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.PUT), any(), eq(String.class)))
                .thenReturn(mockResponse);

        // Act
        String result = service.externalPutAPICall(url, request, params);

        // Assert
        assertEquals("success", result);
    }

    @Test
    void externalPutAPICall_shouldHandleException() throws Exception {
        // Arrange
        String url = "http://test.com";
        Object request = new Object();
        Map<String, String> params = Map.of("key", "value");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.PUT), any(), eq(String.class)))
                .thenThrow(new RuntimeException("Test exception"));

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            service.externalPutAPICall(url, request, params);
        });
        assertEquals("Test exception", exception.getMessage());
    }

    @Test
    void externalDeleteAPICall_shouldReturnResponse() throws Exception {
        // Arrange
        String url = "http://test.com";
        Object request = new Object();
        Map<String, String> params = Map.of("key", "value");
        ResponseEntity<String> mockResponse = new ResponseEntity<>("success", HttpStatus.OK);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.DELETE), any(), eq(String.class)))
                .thenReturn(mockResponse);

        // Act
        String result = service.externalDeleteAPICall(url, request, params);

        // Assert
        assertEquals("success", result);
    }

    @Test
    void externalDeleteAPICall_shouldHandleException() throws Exception {
        // Arrange
        String url = "http://test.com";
        Object request = new Object();
        Map<String, String> params = Map.of("key", "value");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.DELETE), any(), eq(String.class)))
                .thenThrow(new RuntimeException("Test exception"));

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            service.externalDeleteAPICall(url, request, params);
        });
        assertEquals("Test exception", exception.getMessage());
    }

    @Test
    void externalPostUploadAPICall_shouldReturnResponse() throws Exception {
        // Arrange
        String url = "http://test.com";
        MockMultipartFile file = new MockMultipartFile("file", "test.txt",
                "text/plain", "content".getBytes());
        Map<String, String> params = Map.of("key", "value");
        ResponseEntity<String> mockResponse = new ResponseEntity<>("success", HttpStatus.OK);

        when(jwtService.tokenExtractor(httpServletRequest)).thenReturn("token");
        when(jwtService.extractUsername("token")).thenReturn("user");
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                .thenReturn(mockResponse);

        // Act
        String result = service.externalPostUploadAPICall(url, file, params);

        // Assert
        assertEquals("success", result);
    }

    @Test
    void externalPostUploadAPICall_shouldThrowWhenFileEmpty() {
        // Arrange
        String url = "http://test.com";
        MockMultipartFile emptyFile = new MockMultipartFile("file", new byte[0]);
        Map<String, String> params = Map.of("key", "value");

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> service.externalPostUploadAPICall(url, emptyFile, params));
    }

    @Test
    void externalGetAPICall_shouldHandleHttpError() throws Exception {
        // Arrange
        String url = "http://test.com";
        Map<String, String> params = Map.of("key", "value");
        HttpStatusCodeException exception = new HttpClientErrorException(HttpStatus.BAD_REQUEST);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(String.class)))
                .thenThrow(exception);

        // Act & Assert
        assertThrows(HttpClientErrorException.class,
                () -> service.externalGetAPICall(url, params));
    }
}