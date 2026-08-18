package com.adl.et.telco.crm.securerequesthandler.domain.service.serviceimpl.access;

import com.adl.et.telco.crm.securerequesthandler.application.client.accessclient.CrmUserDetailClient;
import com.adl.et.telco.crm.securerequesthandler.domain.dto.authentication.UserBasicInfo;
import com.adl.et.telco.crm.securerequesthandler.domain.dto.common.CommonSouthBoundResponse;
import com.adl.et.telco.crm.securerequesthandler.application.util.constants.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class UserDetailsServiceImplementation implements UserDetailsService {
    private static final Logger logger = LoggerFactory.getLogger(UserDetailsServiceImplementation.class);

    @Autowired
    private CrmUserDetailClient userDetailClient;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        logger.debug("Loading user details for email: {}", email);
        try {
            CommonSouthBoundResponse<UserBasicInfo> userCredentials = userDetailClient.getBasicUserDetails(email);
            logger.info("Successfully loaded user details for email: {}", email);
            return new User(email, userCredentials.getResponseData().getEmail(), new ArrayList<>());
        } catch (Exception exp) {
            logger.error("Failed to load user details for email: {} - {}", email, exp.getMessage(), exp);
            throw new UsernameNotFoundException(Constants.USER_NAME_NOT_FOUND);
        }
    }
}
