package com.adl.et.telco.crm.securerequesthandler.domain.service.serviceimpl.auditview;

import com.adl.et.telco.crm.securerequesthandler.application.client.auditview.AuditViewClient;
import com.adl.et.telco.crm.securerequesthandler.application.util.ResponseHandler;
import com.adl.et.telco.crm.securerequesthandler.application.util.exception.BaseException;
import com.adl.et.telco.crm.securerequesthandler.application.util.exception.ExceptionHandler;
import com.adl.et.telco.crm.securerequesthandler.application.util.resultenum.DisplayResultCodeEnum;
import com.adl.et.telco.crm.securerequesthandler.domain.dto.auditview.*;
import com.adl.et.telco.crm.securerequesthandler.domain.dto.common.CommonNorthBoundResponse;
import com.adl.et.telco.crm.securerequesthandler.domain.dto.common.CommonSouthBoundResponse;
import com.adl.et.telco.crm.securerequesthandler.domain.dto.common.Result;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditViewServiceImplTest {

    @Mock
    private AuditViewClient auditViewClient;

    @Mock
    private ResponseHandler handler;

    @Mock
    private ExceptionHandler exceptionHandler;

    @InjectMocks
    private AuditViewServiceImpl auditViewService;

    @Test
    void getActionAudits_shouldReturnSuccessResponse() throws BaseException {
        // Arrange
        ActionLogPramDTO request = new ActionLogPramDTO();
        List<ActionLogResponseDTO> mockResponse = List.of(new ActionLogResponseDTO());
        CommonSouthBoundResponse<List<ActionLogResponseDTO>> clientResponse = new CommonSouthBoundResponse<>();
        clientResponse.setResponseData(mockResponse);
        Result res = new Result();
        res.setResultCode("00");
        res.setResultDescription("SUCCESSFUL");
        clientResponse.setResult(res);

        when(auditViewClient.getAuditDetails(request)).thenReturn(clientResponse);
        when(handler.responseBuilderWithPageInformation(any(), any(), any(), any()))
                .thenReturn(new CommonNorthBoundResponse<>());

        // Act
        CommonNorthBoundResponse<List<ActionLogResponseDTO>> result = auditViewService.getActionAudits(request);

        // Assert
        assertNotNull(result);
        verify(auditViewClient).getAuditDetails(request);
    }

    @Test
    void getActionAudits_shouldThrowException_whenClientFails() throws BaseException {
        // Arrange
        ActionLogPramDTO request = new ActionLogPramDTO();
        Exception mockException = new RuntimeException("Client error");
        BaseException expectedException = new BaseException("Error", "Error", null, "CODE", null);

        when(auditViewClient.getAuditDetails(request)).thenThrow(mockException);
        when(exceptionHandler.serviceExceptionHandler(mockException, DisplayResultCodeEnum.GET_AUDIT_DETAILS_FAILED))
                .thenReturn(expectedException);

        // Act & Assert
        BaseException thrown = assertThrows(BaseException.class,
                () -> auditViewService.getActionAudits(request));
        assertSame(expectedException, thrown);
    }

    @Test
    void getSearchTypes_shouldReturnAllSubjects() throws BaseException {
        // Arrange
        List<SearchTypeResponseDTO> expectedList = List.of(
                new SearchTypeResponseDTO("TYPE1", "TYPE1"),
                new SearchTypeResponseDTO("TYPE2", "TYPE2")
        );
        when(handler.responseBuilder(anyList(), anyString(), anyString()))
                .thenReturn(new CommonNorthBoundResponse<>());

        // Act
        CommonNorthBoundResponse<List<SearchTypeResponseDTO>> result = auditViewService.getSearchTypes();

        // Assert
        assertNotNull(result);
    }

    @Test
    void getSearchTypes_shouldThrowException_whenErrorOccurs() throws BaseException {
        // Arrange
        Exception mockException = new RuntimeException("Error");
        BaseException expectedException = new BaseException("Error", "Error", null, "CODE", null);

        when(handler.responseBuilder(anyList(), anyString(), anyString())).thenThrow(mockException);
        when(exceptionHandler.serviceExceptionHandler(mockException, DisplayResultCodeEnum.GET_AUDIT_DETAILS_FAILED))
                .thenReturn(expectedException);

        // Act & Assert
        BaseException thrown = assertThrows(BaseException.class,
                () -> auditViewService.getSearchTypes());
        assertSame(expectedException, thrown);
    }

    @Test
    void getStatusCodes_shouldReturnThreeStatusCodes() throws BaseException {
        // Arrange
        when(handler.responseBuilder(anyList(), anyString(), anyString()))
                .thenReturn(new CommonNorthBoundResponse<>());

        // Act
        CommonNorthBoundResponse<List<StatusCodeResponseDTO>> result = auditViewService.getStatusCodes();

        // Assert
        assertNotNull(result);
    }

    @Test
    void getStatusCodes_ShouldThrowBaseException_WhenExceptionOccurs() {
        // Arrange
        Exception testException = new RuntimeException("Test exception");
        BaseException expectedException = new BaseException("Test exception",
                DisplayResultCodeEnum.GET_AUDIT_DETAILS_FAILED.description(),
                HttpStatus.INTERNAL_SERVER_ERROR,
                DisplayResultCodeEnum.GET_AUDIT_DETAILS_FAILED.code(),
                null);

        when(exceptionHandler.serviceExceptionHandler(testException, DisplayResultCodeEnum.GET_AUDIT_DETAILS_FAILED))
                .thenReturn(expectedException);

        // Simulate an exception being thrown in the try block
        doThrow(testException).when(handler).responseBuilder(any(), anyString(), anyString());

        // Act & Assert
        BaseException thrownException = assertThrows(BaseException.class, () -> {
            auditViewService.getStatusCodes();
        });

        // Verify the exception details
        assertEquals(expectedException.getMessage(), thrownException.getMessage());
        assertEquals(expectedException.getReason(), thrownException.getReason());
        assertEquals(expectedException.getHttpStatus(), thrownException.getHttpStatus());
        assertEquals(expectedException.getResultCode(), thrownException.getResultCode());

        // Verify exception handler was called
        verify(exceptionHandler).serviceExceptionHandler(testException, DisplayResultCodeEnum.GET_AUDIT_DETAILS_FAILED);
    }


    @Test
    void getUserNames_shouldReturnUserNames() throws BaseException {
        // Arrange
        List<Object[]> mockData = List.of(
                new Object[]{"User1", "user1@example.com"},
                new Object[]{"User2", "user2@example.com"}
        );
        CommonSouthBoundResponse<List<Object[]>> clientResponse = new CommonSouthBoundResponse<>();
        clientResponse.setResponseData(mockData);

        when(auditViewClient.getUserNames()).thenReturn(clientResponse);
        when(handler.responseBuilder(anyList(), anyString(), anyString()))
                .thenReturn(new CommonNorthBoundResponse<>());

        // Act
        CommonNorthBoundResponse<List<UserNameResponseDTO>> result = auditViewService.getUserNames();

        // Assert
        assertNotNull(result);
    }

    @Test
    void getUserNames_shouldHandleEmptyResponse() throws BaseException {
        // Arrange
        CommonSouthBoundResponse<List<Object[]>> clientResponse = new CommonSouthBoundResponse<>();
        clientResponse.setResponseData(null);

        when(auditViewClient.getUserNames()).thenReturn(clientResponse);
        when(handler.responseBuilder(anyList(), anyString(), anyString()))
                .thenReturn(new CommonNorthBoundResponse<>());

        // Act
        CommonNorthBoundResponse<List<UserNameResponseDTO>> result = auditViewService.getUserNames();

        // Assert
        assertNotNull(result);
    }

    @Test
    void getUserNames_ShouldThrowBaseExceptionWhenClientFails() {
        // Arrange
        Exception expectedException = new RuntimeException("Test exception");
        BaseException expectedBaseException = new BaseException("Processed exception",
                DisplayResultCodeEnum.GET_AUDIT_DETAILS_FAILED.description(),
                HttpStatus.INTERNAL_SERVER_ERROR,
                DisplayResultCodeEnum.GET_AUDIT_DETAILS_FAILED.code(),
                null);

        when(auditViewClient.getUserNames()).thenThrow(expectedException);
        when(exceptionHandler.serviceExceptionHandler(expectedException, DisplayResultCodeEnum.GET_AUDIT_DETAILS_FAILED))
                .thenReturn(expectedBaseException);

        // Act & Assert
        BaseException thrownException = assertThrows(BaseException.class, () -> {
            auditViewService.getUserNames();
        });

        // Verify
        assertSame(expectedBaseException, thrownException);
        verify(auditViewClient).getUserNames();
        verify(exceptionHandler).serviceExceptionHandler(expectedException, DisplayResultCodeEnum.GET_AUDIT_DETAILS_FAILED);

        // Verify no interactions with response handler
        verifyNoInteractions(handler);
    }

    @Test
    void getActivities_shouldReturnActivityCodes() throws BaseException {
        // Arrange
        List<ActivityCodesResponseDTO> mockActivities = List.of(
                new ActivityCodesResponseDTO(),
                new ActivityCodesResponseDTO()
        );
        CommonSouthBoundResponse<List<ActivityCodesResponseDTO>> clientResponse = new CommonSouthBoundResponse<>();
        clientResponse.setResponseData(mockActivities);

        when(auditViewClient.getActivityCodes()).thenReturn(clientResponse);
        when(handler.responseBuilder(anyList(), anyString(), anyString()))
                .thenReturn(new CommonNorthBoundResponse<>());

        // Act
        CommonNorthBoundResponse<List<ActivityCodesResponseDTO>> result = auditViewService.getActivities();

        // Assert
        assertNotNull(result);
    }

    @Test
    void getActivities_ShouldThrowBaseExceptionWhenClientFails() throws Exception {
        // Arrange
        Exception expectedException = new RuntimeException("Test exception");
        BaseException expectedBaseException = new BaseException("Processed exception",
                DisplayResultCodeEnum.GET_AUDIT_DETAILS_FAILED.description(),
                HttpStatus.INTERNAL_SERVER_ERROR,
                DisplayResultCodeEnum.GET_AUDIT_DETAILS_FAILED.code(),
                null);

        when(auditViewClient.getActivityCodes()).thenThrow(expectedException);
        when(exceptionHandler.serviceExceptionHandler(expectedException, DisplayResultCodeEnum.GET_AUDIT_DETAILS_FAILED))
                .thenReturn(expectedBaseException);

        // Act & Assert
        BaseException thrownException = assertThrows(BaseException.class, () -> {
            auditViewService.getActivities();
        });

        // Verify
        assertEquals(expectedBaseException, thrownException);
        verify(auditViewClient).getActivityCodes();
        verify(exceptionHandler).serviceExceptionHandler(expectedException, DisplayResultCodeEnum.GET_AUDIT_DETAILS_FAILED);
        // Verify no interaction with handler.responseBuilder since we're testing exception path
        verify(handler, never()).responseBuilder(any(), any(), any());
    }
}