package com.adl.et.telco.crm.securerequesthandler.domain.service.serviceimpl.common;

import com.adl.et.telco.crm.securerequesthandler.application.util.exception.BaseException;
import com.adl.et.telco.crm.securerequesthandler.application.util.exception.ExceptionHandler;
import com.adl.et.telco.crm.securerequesthandler.application.util.resultenum.DisplayResultCodeEnum;
import com.adl.et.telco.crm.securerequesthandler.domain.service.serviceimpl.access.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExternalAPICallServiceImplTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private ExceptionHandler exceptionHandler;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private ExternalAPICallServiceImpl externalAPICallService;

    @BeforeEach
    void setUp() {
        // Initialize the service with required dependencies
        externalAPICallService = new ExternalAPICallServiceImpl(
                restTemplate,
                httpServletRequest,
                exceptionHandler,
                jwtService,
                "test-tenant"
        );
    }

    @Test
    void externalGetAPICall_shouldReturnSuccessResponse() throws Exception {
        // Arrange
        String url = "http://test.com";
        Map<String, String> params = Map.of("param", "value");
        String expectedResponse = "{\n" +
                "  \"result\": {\n" +
                "    \"resultCode\": \"resultCode_87a21aeed93f\",\n" +
                "    \"resultDescription\": \"resultDescription_1f07dffb68ad\",\n" +
                "    \"pageDetail\": {\n" +
                "      \"pageNumber\": 0,\n" +
                "      \"pageElementCount\": 0,\n" +
                "      \"totalRecords\": 0\n" +
                "    }\n" +
                "  },\n" +
                "  \"responseData\": {}\n" +
                "}";

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(String.class)))
                .thenReturn(new ResponseEntity<>(expectedResponse, HttpStatus.OK));
        Map<String, String> headers = Map.of("param", "value");

        // Act
        String result = externalAPICallService.externalGetAPICall(url, params, headers);

        // Assert
        assertNotNull(result);
        verify(restTemplate).exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)
        );
    }

    @Test
    void externalPostAPICall_shouldReturnSuccessResponse() throws Exception {
        // Arrange
        String url = "http://test.com";
        Object request = new Object();
        Map<String, String> params = Map.of("param", "value");
        Map<String, String> headers = Map.of("param", "value");
        String expectedResponse = "{\n" +
                "  \"result\": {\n" +
                "    \"resultCode\": \"resultCode_87a21aeed93f\",\n" +
                "    \"resultDescription\": \"resultDescription_1f07dffb68ad\",\n" +
                "    \"pageDetail\": {\n" +
                "      \"pageNumber\": 0,\n" +
                "      \"pageElementCount\": 0,\n" +
                "      \"totalRecords\": 0\n" +
                "    }\n" +
                "  },\n" +
                "  \"responseData\": {}\n" +
                "}";

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(String.class)))
                .thenReturn(new ResponseEntity<>(expectedResponse, HttpStatus.OK));

        // Act
        String result = externalAPICallService.externalPostAPICall(url, request, params, headers);

        // Assert
        assertNotNull(result);
    }

    @Test
    void externalPutAPICall_shouldReturnSuccessResponse() throws Exception {
        // Arrange
        String url = "http://test.com";
        Object request = new Object();
        Map<String, String> params = Map.of("param", "value");
        Map<String, String> headers = Map.of("param", "value");
        String expectedResponse = "{\n" +
                "  \"result\": {\n" +
                "    \"resultCode\": \"resultCode_87a21aeed93f\",\n" +
                "    \"resultDescription\": \"resultDescription_1f07dffb68ad\",\n" +
                "    \"pageDetail\": {\n" +
                "      \"pageNumber\": 0,\n" +
                "      \"pageElementCount\": 0,\n" +
                "      \"totalRecords\": 0\n" +
                "    }\n" +
                "  },\n" +
                "  \"responseData\": {}\n" +
                "}";

        when(restTemplate.exchange(anyString(), eq(HttpMethod.PUT), any(), eq(String.class)))
                .thenReturn(new ResponseEntity<>(expectedResponse, HttpStatus.OK));

        // Act
        String result = externalAPICallService.externalPutAPICall(url, request, params, headers);

        // Assert
        assertNotNull(result);
    }

    @Test
    void externalDeleteAPICall_shouldReturnSuccessResponse() throws Exception {
        // Arrange
        String url = "http://test.com";
        Object request = new Object();
        Map<String, String> params = Map.of("param", "value");
        Map<String, String> headers = Map.of("param", "value");
        String expectedResponse = "{\n" +
                "  \"result\": {\n" +
                "    \"resultCode\": \"resultCode_87a21aeed93f\",\n" +
                "    \"resultDescription\": \"resultDescription_1f07dffb68ad\",\n" +
                "    \"pageDetail\": {\n" +
                "      \"pageNumber\": 0,\n" +
                "      \"pageElementCount\": 0,\n" +
                "      \"totalRecords\": 0\n" +
                "    }\n" +
                "  },\n" +
                "  \"responseData\": {}\n" +
                "}";

        when(restTemplate.exchange(anyString(), eq(HttpMethod.DELETE), any(), eq(String.class)))
                .thenReturn(new ResponseEntity<>(expectedResponse, HttpStatus.OK));

        // Act
        String result = externalAPICallService.externalDeleteAPICall(url, request, params, headers);

        // Assert
        assertNotNull(result);
    }

    @Test
    void externalPostUploadAPICall_shouldReturnSuccessResponse() throws Exception {
        // Arrange
        String url = "http://test.com";
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "content".getBytes());
        Map<String, String> params = Map.of("param", "value");
        Map<String, String> headers = Map.of("param", "value");
        String expectedResponse = "{\n" +
                "  \"result\": {\n" +
                "    \"resultCode\": \"resultCode_87a21aeed93f\",\n" +
                "    \"resultDescription\": \"resultDescription_1f07dffb68ad\",\n" +
                "    \"pageDetail\": {\n" +
                "      \"pageNumber\": 0,\n" +
                "      \"pageElementCount\": 0,\n" +
                "      \"totalRecords\": 0\n" +
                "    }\n" +
                "  },\n" +
                "  \"responseData\": {}\n" +
                "}";

        when(jwtService.tokenExtractor(httpServletRequest)).thenReturn("token");
        when(jwtService.extractUsername("token")).thenReturn("user");
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                .thenReturn(new ResponseEntity<>(expectedResponse, HttpStatus.OK));

        // Act
        String result = externalAPICallService.externalPostUploadAPICall(url, file, params, headers);

        // Assert
        assertNotNull(result);
    }

    @Test
    void externalPostUploadAPICall_shouldThrowWhenFileEmpty() {
        // Arrange
        String url = "http://test.com";
        MockMultipartFile emptyFile = new MockMultipartFile("file", new byte[0]);
        Map<String, String> params = Map.of("param", "value");
        Map<String, String> headers = Map.of("param", "value");

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> externalAPICallService.externalPostUploadAPICall(url, emptyFile, params, headers));
    }

    @Test
    void externalGetAPICall_shouldHandleHttpError() throws Exception {
        // Arrange
        String url = "http://test.com";
        Map<String, String> params = Map.of("param", "value");
        Map<String, String> headers = Map.of("param", "value");
        HttpStatusCodeException exception = new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Bad Request");

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(String.class)))
                .thenThrow(exception);

        // Act & Assert
        assertThrows(HttpClientErrorException.class,
                () -> externalAPICallService.externalGetAPICall(url, params, headers));
    }

    @Test
    void externalGetAPICall_shouldHandleResourceAccessError() throws Exception {
        // Arrange
        String url = "http://test.com";
        Map<String, String> params = Map.of("param", "value");
        Map<String, String> headers = Map.of("param", "value");
        ResourceAccessException exception = new ResourceAccessException("Connection failed");
        BaseException expectedException = new BaseException("Error", "Error", null, "CODE", null);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(String.class)))
                .thenThrow(exception);
        when(exceptionHandler.clientExceptionHandler(exception, DisplayResultCodeEnum.RESOURCE_ACCESS_EXCEPTION))
                .thenReturn(expectedException);

        // Act & Assert
        BaseException thrown = assertThrows(BaseException.class,
                () -> externalAPICallService.externalGetAPICall(url, params, headers));
        assertSame(expectedException, thrown);
    }
}