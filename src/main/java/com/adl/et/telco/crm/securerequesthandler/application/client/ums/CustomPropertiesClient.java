package com.adl.et.telco.crm.securerequesthandler.application.client.ums;

import com.adl.et.telco.crm.securerequesthandler.application.client.BaseClient;
import com.adl.et.telco.crm.securerequesthandler.domain.dto.common.CommonSouthBoundResponse;
import com.adl.et.telco.crm.securerequesthandler.domain.dto.ums.customproperties.CustomPropertiesItem;
import com.adl.et.telco.crm.securerequesthandler.domain.dto.ums.customproperties.CustomPropertyDTO;
import com.adl.et.telco.crm.securerequesthandler.application.util.exception.ExceptionHandler;
import com.adl.et.telco.crm.securerequesthandler.application.util.resultenum.DisplayResultCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
@Component
@Slf4j
public class CustomPropertiesClient extends BaseClient {

    @Autowired
    private ExceptionHandler exceptionHandler;
    @Autowired
    private RestTemplate restTemplate;
    @Value("${ums.get-custom-properties.url}")
    private String customerPropertiesUrl;

    public CommonSouthBoundResponse<List<CustomPropertiesItem>> getAllCustomProperties() {
        try {
            ParameterizedTypeReference<CommonSouthBoundResponse<List<CustomPropertiesItem>>> typeRef = new ParameterizedTypeReference<CommonSouthBoundResponse<List<CustomPropertiesItem>>>() {
            };
            UriComponentsBuilder adaptorUrl = UriComponentsBuilder.fromUriString(customerPropertiesUrl);
            ResponseEntity<CommonSouthBoundResponse<List<CustomPropertiesItem>>> exchange = restTemplate.exchange(adaptorUrl.build().toString(), HttpMethod.GET, populateRequestEntity(), typeRef);
            return exchange.getBody();
        } catch (Exception ex) {
            throw exceptionHandler.clientExceptionHandler(ex, DisplayResultCodeEnum.GET_USER_STATUS_META_DATA_FAILED);
        }
    }

    public CommonSouthBoundResponse<List<CustomPropertyDTO>> getAllCustomPropertiesByUserId(String userName) {
        try {
            ParameterizedTypeReference<CommonSouthBoundResponse<List<CustomPropertyDTO>>> typeRef = new ParameterizedTypeReference<CommonSouthBoundResponse<List<CustomPropertyDTO>>>() {
            };
            UriComponentsBuilder adaptorUrl = UriComponentsBuilder.fromUriString(customerPropertiesUrl + "/user/" + userName);
            log.info("PropertiesURL - " + customerPropertiesUrl + "/user/" + userName);
            ResponseEntity<CommonSouthBoundResponse<List<CustomPropertyDTO>>> exchange = restTemplate.exchange(adaptorUrl.build().toString(), HttpMethod.GET, populateRequestEntity(), typeRef);
            return exchange.getBody();
        } catch (Exception ex) {
            ex.printStackTrace();
            throw exceptionHandler.clientExceptionHandler(ex, DisplayResultCodeEnum.GET_USER_STATUS_META_DATA_FAILED);
        }
    }
}
