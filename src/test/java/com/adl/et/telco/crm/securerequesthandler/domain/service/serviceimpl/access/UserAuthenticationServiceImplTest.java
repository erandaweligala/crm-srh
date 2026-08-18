package com.adl.et.telco.crm.securerequesthandler.domain.service.serviceimpl.access;

import com.adl.et.telco.crm.securerequesthandler.application.client.accessclient.CrmUserDetailClient;
import com.adl.et.telco.crm.securerequesthandler.application.client.ums.CustomPropertiesClient;
import com.adl.et.telco.crm.securerequesthandler.application.util.ResponseHandler;
import com.adl.et.telco.crm.securerequesthandler.application.util.constants.ServiceConstants;
import com.adl.et.telco.crm.securerequesthandler.application.util.exception.BaseException;
import com.adl.et.telco.crm.securerequesthandler.application.util.resultenum.AuthCodeEnum;
import com.adl.et.telco.crm.securerequesthandler.domain.dto.authentication.PermissionDTO;
import com.adl.et.telco.crm.securerequesthandler.domain.dto.authentication.UserBasicInfo;
import com.adl.et.telco.crm.securerequesthandler.domain.dto.authentication.UserDetailsForToken;
import com.adl.et.telco.crm.securerequesthandler.domain.dto.authentication.ViewDTO;
import com.adl.et.telco.crm.securerequesthandler.domain.dto.common.CommonNorthBoundResponse;
import com.adl.et.telco.crm.securerequesthandler.domain.dto.common.CommonSouthBoundResponse;
import com.adl.et.telco.crm.securerequesthandler.domain.dto.security.AccessTokenResponse;
import com.adl.et.telco.crm.securerequesthandler.domain.dto.security.TokenEnhancementRequest;
import com.adl.et.telco.crm.securerequesthandler.domain.dto.security.UserLoginRequest;
import com.adl.et.telco.crm.securerequesthandler.domain.dto.ums.customproperties.CustomPropertyDTO;
import com.adl.et.telco.crm.securerequesthandler.external.repository.cache.AuthCacheRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserAuthenticationServiceImplTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthCacheRepository cacheRepository;

    @Mock
    private CrmUserDetailClient crmUserDetailClient;

    @Mock
    private CustomPropertiesClient customPropertiesClient;

    @Mock
    private ResponseHandler responseHandler;

    @Mock
    private HttpServletRequest httpServletRequest;

    private UserAuthenticationServiceImpl userAuthenticationService;

    private final String testUserId = "testUser";
    private final String testTenant = "testTenant";
    private final String testProductId = "123";
    private final String testToken = "testToken";
    private final String testRvt = "testRvt";

    @BeforeEach
    void setUp() {
        // Initialize with constructor parameters
        userAuthenticationService = new UserAuthenticationServiceImpl(
                jwtService,
                cacheRepository,
                crmUserDetailClient,
                customPropertiesClient,
                responseHandler,
                300L,  // refreshTimeRange
                3600L, // timeLimitAC
                "prefix_", // prefixAC
                "temp_"    // tempTokenPrefix
        );
    }

    @Test
    void login_Success() throws BaseException {
        // Arrange
        UserLoginRequest loginRequest = new UserLoginRequest();
        loginRequest.setTenant(testTenant);

        CommonSouthBoundResponse<UserBasicInfo> userInfoResponse = new CommonSouthBoundResponse<>();
        UserBasicInfo userBasicInfo = new UserBasicInfo();
        userBasicInfo.setUserId(testUserId);
        userInfoResponse.setResponseData(userBasicInfo);

        when(crmUserDetailClient.getUserDetailByUserName(anyString())).thenReturn(userInfoResponse);

        List<Integer> accessSystems = Collections.singletonList(123);
        CommonSouthBoundResponse<List<Integer>> accessResponse = new CommonSouthBoundResponse<>();
        accessResponse.setResponseData(accessSystems);
        when(crmUserDetailClient.getUserAccessSystem(anyString())).thenReturn(accessResponse);

        // Mock the UserDetailsForToken response
        CommonSouthBoundResponse<UserDetailsForToken> userDetailsResponse = new CommonSouthBoundResponse<>();
        UserDetailsForToken userDetails = new UserDetailsForToken();
        PermissionDTO permissionDTO = new PermissionDTO();
        List<ViewDTO> viewDTOS = new ArrayList<>();
        permissionDTO.setComponents(viewDTOS);
        userDetails.setPermission(permissionDTO);
        userDetailsResponse.setResponseData(userDetails);
        when(crmUserDetailClient.getUserDetails(anyString(), "1")).thenReturn(userDetailsResponse);

        // Mock custom properties response
        CommonSouthBoundResponse<List<CustomPropertyDTO>> customPropsResponse = new CommonSouthBoundResponse<>();
        customPropsResponse.setResponseData(Collections.emptyList());
        when(customPropertiesClient.getAllCustomPropertiesByUserId(anyString())).thenReturn(customPropsResponse);

        // For void methods, use doNothing() or doAnswer()
        doAnswer(invocation -> {
            Map<String, String> map = invocation.getArgument(2);
            map.put(ServiceConstants.TOKEN, testToken);
            return null;
        }).when(jwtService).generateAccessToken(anyString(), anyMap(), anyMap());

        CommonNorthBoundResponse<AccessTokenResponse> expectedResponse = new CommonNorthBoundResponse<>();
        when(responseHandler.responseBuilder(any(AccessTokenResponse.class), anyString(), anyString())).thenReturn(expectedResponse);

        // Act
        CommonNorthBoundResponse<AccessTokenResponse> result = userAuthenticationService.login(testRvt, loginRequest, testProductId);

        // Assert
        assertNotNull(result);
        verify(cacheRepository).deleteKey(anyString());
        verify(cacheRepository).save(anyString(), anyString(), anyLong());
    }

    @Test
    void login_UserNoAccess() throws BaseException {
        // Arrange
        UserLoginRequest loginRequest = new UserLoginRequest();
        loginRequest.setTenant(testTenant);

        CommonSouthBoundResponse<UserBasicInfo> userInfoResponse = new CommonSouthBoundResponse<>();
        UserBasicInfo userBasicInfo = new UserBasicInfo();
        userBasicInfo.setUserId(testUserId);
        userInfoResponse.setResponseData(userBasicInfo);

        when(crmUserDetailClient.getUserDetailByUserName(anyString())).thenReturn(userInfoResponse);

        CommonSouthBoundResponse<List<Integer>> accessResponse = new CommonSouthBoundResponse<>();
        accessResponse.setResponseData(Collections.emptyList());
        when(crmUserDetailClient.getUserAccessSystem(anyString())).thenReturn(accessResponse);

        // Act & Assert
        BaseException exception = assertThrows(BaseException.class, () -> {
            userAuthenticationService.login(testRvt, loginRequest, testProductId);
        });

        assertEquals(HttpStatus.FORBIDDEN, exception.getHttpStatus());
        assertEquals(AuthCodeEnum.USER_NOT_HAVE_ACCESS_TO_SYSTEM.code(), exception.getResultCode());
    }

    @Test
    void login_shouldWrapGenericExceptionInBaseException() {
        // Arrange
        String requestVerificationToken = "test-token";
        UserLoginRequest userLoginRequest = new UserLoginRequest();
        userLoginRequest.setTenant("test-tenant");
        String productId = "123";

        CommonSouthBoundResponse<UserBasicInfo> userInfoResponse = new CommonSouthBoundResponse<>();
        UserBasicInfo userBasicInfo = new UserBasicInfo();
        userBasicInfo.setUserId(testUserId);
        userInfoResponse.setResponseData(userBasicInfo);
        when(crmUserDetailClient.getUserDetailByUserName(anyString())).thenReturn(userInfoResponse);

        // Act & Assert
        when(crmUserDetailClient.getUserAccessSystem(anyString())).thenThrow(new RuntimeException("Test exception"));

        // Act & Assert
        BaseException exception = assertThrows(BaseException.class, () -> {
            userAuthenticationService.login(requestVerificationToken, userLoginRequest, productId);
        });

        assertEquals("Test exception", exception.getMessage());
        assertEquals(AuthCodeEnum.LOGIN_INTERNAL_SERVER_ERROR.description(), exception.getReason());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getHttpStatus());
        assertEquals(AuthCodeEnum.LOGIN_INTERNAL_SERVER_ERROR.code(), exception.getResultCode());
    }

    @Test
    void newAccessToken_Success() throws BaseException {
        // Arrange
        when(jwtService.tokenExtractor(any())).thenReturn(testToken);

        Date futureDate = new Date(System.currentTimeMillis() + 5000);
        when(jwtService.extractExpiration(testToken)).thenReturn(futureDate);

        when(jwtService.extractUsername(testToken)).thenReturn(testUserId);

        // Mock the UserDetailsForToken response
        CommonSouthBoundResponse<UserDetailsForToken> userDetailsResponse = new CommonSouthBoundResponse<>();
        UserDetailsForToken userDetails = new UserDetailsForToken();
        PermissionDTO permissionDTO = new PermissionDTO();
        List<ViewDTO> viewDTOS = new ArrayList<>();
        permissionDTO.setComponents(viewDTOS);
        userDetails.setPermission(permissionDTO);
        userDetailsResponse.setResponseData(userDetails);
        when(crmUserDetailClient.getUserDetails(anyString(), "1")).thenReturn(userDetailsResponse);

        // Mock custom properties response
        CommonSouthBoundResponse<List<CustomPropertyDTO>> customPropsResponse = new CommonSouthBoundResponse<>();
        customPropsResponse.setResponseData(Collections.emptyList());
        when(customPropertiesClient.getAllCustomPropertiesByUserId(anyString())).thenReturn(customPropsResponse);

        // For void methods, use doNothing() or doAnswer()
        doAnswer(invocation -> {
            Map<String, String> map = invocation.getArgument(2);
            map.put(ServiceConstants.TOKEN, "newToken");
            return null;
        }).when(jwtService).generateAccessToken(anyString(), anyMap(), anyMap());

        CommonNorthBoundResponse<AccessTokenResponse> expectedResponse = new CommonNorthBoundResponse<>();
        when(responseHandler.responseBuilder(any(AccessTokenResponse.class), anyString(), anyString())).thenReturn(expectedResponse);

        // Act
        CommonNorthBoundResponse<AccessTokenResponse> result =
                userAuthenticationService.newAccessToken(testRvt, httpServletRequest, testProductId);

        // Assert
        assertNotNull(result);
        verify(cacheRepository).save(anyString(), anyString(), anyLong());
    }

    @Test
    void newAccessToken_NotInRefreshTime() throws BaseException {
        // Arrange
        when(jwtService.tokenExtractor(any())).thenReturn(testToken);

        Date distantFutureDate = new Date(System.currentTimeMillis() + 10000000);
        when(jwtService.extractExpiration(testToken)).thenReturn(distantFutureDate);

        // Act & Assert
        BaseException exception = assertThrows(BaseException.class, () -> {
            userAuthenticationService.newAccessToken(testRvt, httpServletRequest, testProductId);
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
        assertEquals(AuthCodeEnum.NOT_IN_REFRESH_TIME.code(), exception.getResultCode());
    }

    @Test
    void newAccessToken_ShouldThrowBaseException_WhenNotInRefreshTimeRange() throws BaseException {
        // Arrange
        when(jwtService.tokenExtractor(any())).thenThrow(new RuntimeException("Test Exception"));

        // Act & Assert
        BaseException exception = assertThrows(BaseException.class, () -> {
            userAuthenticationService.newAccessToken(testRvt, httpServletRequest, testProductId);
        });

        assertEquals(AuthCodeEnum.CREATE_ACCESS_TOKEN_FAILED.description(), exception.getReason());
        assertEquals(AuthCodeEnum.CREATE_ACCESS_TOKEN_FAILED.code(), exception.getResultCode());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getHttpStatus());
    }

    @Test
    void userLogOut_Success() {
        // Arrange
        when(jwtService.tokenExtractor(any())).thenReturn(testToken);
        when(jwtService.extractUsername(testToken)).thenReturn(testUserId);
        when(cacheRepository.existsByUserId(testUserId)).thenReturn(true);

        CommonNorthBoundResponse<Object> expectedResponse = new CommonNorthBoundResponse<>();
        when(responseHandler.responseBuilder(isNull(), anyString(), anyString())).thenReturn(expectedResponse);

        // Act
        CommonNorthBoundResponse<String> result = userAuthenticationService.userLogOut(httpServletRequest);

        // Assert
        assertNotNull(result);
        verify(cacheRepository).delete(testUserId);
    }

    @Test
    void userLogOut_ThrowBaseException() {
        // Arrange
        when(jwtService.tokenExtractor(any())).thenThrow(new BaseException("Error", "Error", HttpStatus.INTERNAL_SERVER_ERROR, "CODE", null));

        // Act
        BaseException exception = assertThrows(BaseException.class, () -> {
            userAuthenticationService.userLogOut(httpServletRequest);
        });

        // Assert
        assertEquals("Error", exception.getReason());
        assertEquals("CODE", exception.getResultCode());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getHttpStatus());
    }

    @Test
    void userLogOut_ThrowGenericException() {
        // Arrange
        when(jwtService.tokenExtractor(any())).thenThrow(new RuntimeException("Test Exception"));

        // Act
        BaseException exception = assertThrows(BaseException.class, () -> {
            userAuthenticationService.userLogOut(httpServletRequest);
        });

        // Assert
        assertEquals(AuthCodeEnum.LOGOUT_INTERNAL_SERVER_ERROR.description(), exception.getReason());
        assertEquals(AuthCodeEnum.LOGOUT_INTERNAL_SERVER_ERROR.code(), exception.getResultCode());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getHttpStatus());
    }

    @Test
    void enhanceAccessToken_Success() throws BaseException {
        // Arrange
        TokenEnhancementRequest request = new TokenEnhancementRequest();
        request.setLoginSystemId(123);

        when(jwtService.tokenExtractor(any())).thenReturn(testToken);

        Date futureDate = new Date(System.currentTimeMillis() + 5000);
        when(jwtService.extractExpiration(testToken)).thenReturn(futureDate);

        when(jwtService.extractUsername(testToken)).thenReturn(testUserId);

        // Mock the UserDetailsForToken response
        CommonSouthBoundResponse<UserDetailsForToken> userDetailsResponse = new CommonSouthBoundResponse<>();
        UserDetailsForToken userDetails = new UserDetailsForToken();

        PermissionDTO permissionDTO = new PermissionDTO();
        List<ViewDTO> viewDTOS = new ArrayList<>();
        permissionDTO.setComponents(viewDTOS);
        userDetails.setPermission(permissionDTO);
        userDetailsResponse.setResponseData(userDetails);
        when(crmUserDetailClient.getUserDetails(anyString(), "1")).thenReturn(userDetailsResponse);

        // Mock custom properties response
        CommonSouthBoundResponse<List<CustomPropertyDTO>> customPropsResponse = new CommonSouthBoundResponse<>();
        customPropsResponse.setResponseData(Collections.emptyList());
        when(customPropertiesClient.getAllCustomPropertiesByUserId(anyString())).thenReturn(customPropsResponse);

        // For void methods, use doNothing() or doAnswer()
        doAnswer(invocation -> {
            Map<String, String> map = invocation.getArgument(2);
            map.put(ServiceConstants.TOKEN, "newToken");
            return null;
        }).when(jwtService).generateAccessToken(anyString(), anyMap(), anyMap());

        CommonNorthBoundResponse<AccessTokenResponse> expectedResponse = new CommonNorthBoundResponse<>();
        when(responseHandler.responseBuilder(any(AccessTokenResponse.class), anyString(), anyString())).thenReturn(expectedResponse);

        // Act
        CommonNorthBoundResponse<AccessTokenResponse> result =
                userAuthenticationService.enhanceAccessToken(request, testRvt, httpServletRequest, testProductId);

        // Assert
        assertNotNull(result);
        verify(cacheRepository).save(anyString(), anyString(), anyLong());
    }

    @Test
    void enhanceAccessToken_NotWithinRefreshTime() {
        TokenEnhancementRequest request = new TokenEnhancementRequest();
        request.setLoginSystemId(123);

        // Arrange
        when(jwtService.tokenExtractor(any())).thenReturn(testToken);

        Date futureDate = new Date(System.currentTimeMillis() + 500000);
        when(jwtService.extractExpiration(testToken)).thenReturn(futureDate);

        // Act
        BaseException exception = assertThrows(BaseException.class, () -> {
            userAuthenticationService.enhanceAccessToken(request, testRvt, httpServletRequest, testProductId);
        });

        // Assert
        assertEquals(AuthCodeEnum.NOT_IN_REFRESH_TIME.description(), exception.getReason());
        assertEquals(AuthCodeEnum.NOT_IN_REFRESH_TIME.code(), exception.getResultCode());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
    }

    @Test
    void enhanceAccessToken_ThrowBaseException() {
        TokenEnhancementRequest request = new TokenEnhancementRequest();
        request.setLoginSystemId(123);
        // Arrange
        when(jwtService.tokenExtractor(any())).thenThrow(new BaseException("Error", "Error", HttpStatus.INTERNAL_SERVER_ERROR, "CODE", null));

        // Act
        BaseException exception = assertThrows(BaseException.class, () -> {
            userAuthenticationService.enhanceAccessToken(request, testRvt, httpServletRequest, testProductId);
        });

        // Assert
        assertEquals("Error", exception.getReason());
        assertEquals("CODE", exception.getResultCode());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getHttpStatus());
    }

    @Test
    void enhanceAccessToken_ThrowGenericException() {
        TokenEnhancementRequest request = new TokenEnhancementRequest();
        request.setLoginSystemId(123);
        // Arrange
        when(jwtService.tokenExtractor(any())).thenThrow(new RuntimeException("Test Exception"));

        // Act
        BaseException exception = assertThrows(BaseException.class, () -> {
            userAuthenticationService.enhanceAccessToken(request, testRvt, httpServletRequest, testProductId);
        });

        // Assert
        assertEquals(AuthCodeEnum.CREATE_ACCESS_TOKEN_FAILED.description(), exception.getReason());
        assertEquals(AuthCodeEnum.CREATE_ACCESS_TOKEN_FAILED.code(), exception.getResultCode());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getHttpStatus());
    }

    @Test
    void extractUsername_WithEmail() {
        String email = "user@domain.com";
        String result = UserAuthenticationServiceImpl.extractUsername(email);
        assertEquals("user", result);
    }

    @Test
    void extractUsername_WithoutEmail() {
        String username = "plainusername";
        String result = UserAuthenticationServiceImpl.extractUsername(username);
        assertEquals("plainusername", result);
    }
}