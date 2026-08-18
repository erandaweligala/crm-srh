package com.adl.et.telco.crm.securerequesthandler.domain.service.serviceimpl.access;

import com.adl.et.telco.crm.securerequesthandler.application.util.ResponseHandler;
import com.adl.et.telco.crm.securerequesthandler.application.util.exception.BaseException;
import com.adl.et.telco.crm.securerequesthandler.application.util.resultenum.AuthCodeEnum;
import com.adl.et.telco.crm.securerequesthandler.domain.dto.common.CommonNorthBoundResponse;
import com.adl.et.telco.crm.securerequesthandler.domain.dto.security.SamlRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserAuthenticationManageServiceImplTest {

    @Mock
    private ResponseHandler handler;

    @InjectMocks
    private UserAuthenticationManageServiceImpl service = new UserAuthenticationManageServiceImpl(
            handler,
            300L, // expiryTime
            "test-tenant-id", // azureTenantId
            "https://test-issuer.com", // issuerUrl
            "https://test-redirect.com/{tenantId}?samlRequest={uriEncode}" // redirectionUrl
    );

    @Test
    void createSamlRequest_shouldReturnSuccessResponse() throws Exception {
        // Arrange
        String redirectionUrl = "https://test-redirect.com/{tenantId}?samlRequest={uriEncode}";
        UserAuthenticationManageServiceImpl service = new UserAuthenticationManageServiceImpl(
                handler,
                300L,
                "test-tenant-id",
                "https://test-issuer.com",
                redirectionUrl
        );

        CommonNorthBoundResponse<SamlRequest> expectedResponse = new CommonNorthBoundResponse<>();
        when(handler.responseBuilder(any(SamlRequest.class), anyString(), anyString()))
                .thenReturn(expectedResponse);

        // Act
        CommonNorthBoundResponse<SamlRequest> result = service.createSamlRequest(UUID.randomUUID().toString());

        // Assert
        assertSame(expectedResponse, result);
        verify(handler).responseBuilder(any(SamlRequest.class),
                eq(AuthCodeEnum.AUTH_REQUEST_SUCCESS.description()),
                eq(AuthCodeEnum.AUTH_REQUEST_SUCCESS.code()));
    }

    @Test
    void createSamlRequest_shouldThrowBaseException_whenInvalidRedirectionUrl() {
        // Arrange - Using invalid template that will cause exception during URL building
        String invalidRedirectionUrl = "https://test.com/{missingParam}";
        UserAuthenticationManageServiceImpl service = new UserAuthenticationManageServiceImpl(
                handler,
                300L,
                "test-tenant-id",
                "https://test-issuer.com",
                invalidRedirectionUrl
        );

        // Act & Assert
        assertThrows(
                BaseException.class,
                () -> service.createSamlRequest(UUID.randomUUID().toString())
        );
    }

    @Test
    void createSamlRequest_shouldThrowBaseException_whenHandlerThrows() throws Exception {
        // Arrange
        UserAuthenticationManageServiceImpl service = new UserAuthenticationManageServiceImpl(
                handler,
                300L,
                "test-tenant-id",
                "https://test-issuer.com",
                "https://test-redirect.com/{tenantId}?samlRequest={uriEncode}"
        );

        BaseException expectedException = new BaseException(
                "Handler error",
                "Error reason",
                HttpStatus.BAD_REQUEST,
                "ERROR_CODE",
                null
        );

        when(handler.responseBuilder(any(), any(), any()))
                .thenThrow(expectedException);

        // Act & Assert
        BaseException thrownException = assertThrows(
                BaseException.class,
                () -> service.createSamlRequest(UUID.randomUUID().toString())
        );

        assertSame(expectedException.getHttpStatus(), thrownException.getHttpStatus());
    }
}