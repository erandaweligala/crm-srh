package com.adl.et.telco.crm.securerequesthandler.domain.service.serviceimpl.access;

import com.adl.et.telco.crm.securerequesthandler.application.client.accessclient.CrmUserDetailClient;
import com.adl.et.telco.crm.securerequesthandler.application.util.constants.Constants;
import com.adl.et.telco.crm.securerequesthandler.domain.dto.authentication.UserBasicInfo;
import com.adl.et.telco.crm.securerequesthandler.domain.dto.common.CommonSouthBoundResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import static org.junit.jupiter.api.Assertions.*;
        import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserDetailServiceImplTest {

    @Mock
    private CrmUserDetailClient userDetailClient;

    @Mock
    private Logger logger;

    @InjectMocks
    private UserDetailsServiceImplementation userDetailsService; // Assuming your class is named CustomUserDetailsService

    @Test
    void loadUserByUsername_shouldReturnUserDetails_whenUserExists() {
        // Arrange
        String email = "test@example.com";
        UserBasicInfo userInfo = new UserBasicInfo();
        userInfo.setEmail(email);
        CommonSouthBoundResponse<UserBasicInfo> response = new CommonSouthBoundResponse<>();
        response.setResponseData(userInfo);

        when(userDetailClient.getBasicUserDetails(email)).thenReturn(response);

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        // Assert
        assertNotNull(userDetails);
        assertEquals(email, userDetails.getUsername());
        assertTrue(userDetails.getAuthorities().isEmpty());

        verify(userDetailClient).getBasicUserDetails(email);
    }

    @Test
    void loadUserByUsername_shouldThrowUsernameNotFoundException_whenClientThrowsException() {
        // Arrange
        String email = "nonexistent@example.com";
        Exception expectedException = new RuntimeException("Service unavailable");

        when(userDetailClient.getBasicUserDetails(email)).thenThrow(expectedException);

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername(email)
        );

        assertEquals(Constants.USER_NAME_NOT_FOUND, exception.getMessage());

        verify(userDetailClient).getBasicUserDetails(email);
    }

    @Test
    void loadUserByUsername_shouldThrowUsernameNotFoundException_whenResponseDataIsNull() {
        // Arrange
        String email = "invalid@example.com";
        CommonSouthBoundResponse<UserBasicInfo> response = new CommonSouthBoundResponse<>();
        response.setResponseData(null);

        when(userDetailClient.getBasicUserDetails(email)).thenReturn(response);

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername(email)
        );

        assertEquals(Constants.USER_NAME_NOT_FOUND, exception.getMessage());

        verify(userDetailClient).getBasicUserDetails(email);
    }

    @Test
    void loadUserByUsername_shouldThrowUsernameNotFoundException_whenUserEmailIsNull() {
        // Arrange
        String email = "nulluser@example.com";
        UserBasicInfo userInfo = new UserBasicInfo();
        userInfo.setEmail(null);
        CommonSouthBoundResponse<UserBasicInfo> response = new CommonSouthBoundResponse<>();
        response.setResponseData(userInfo);

        when(userDetailClient.getBasicUserDetails(email)).thenReturn(response);

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername(email)
        );

        assertEquals(Constants.USER_NAME_NOT_FOUND, exception.getMessage());

        verify(userDetailClient).getBasicUserDetails(email);
    }
}