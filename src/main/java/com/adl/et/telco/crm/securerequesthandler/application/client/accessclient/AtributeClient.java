package com.adl.et.telco.crm.securerequesthandler.application.client.accessclient;

import com.adl.et.telco.crm.securerequesthandler.application.client.BaseClient;
import com.adl.et.telco.crm.securerequesthandler.domain.dto.authentication.AttributeDetails;
import com.adl.et.telco.crm.securerequesthandler.domain.dto.common.CommonSouthBoundResponse;
import com.adl.et.telco.crm.securerequesthandler.application.util.exception.BaseException;
import com.adl.et.telco.crm.securerequesthandler.application.util.resultenum.DisplayResultCodeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
public class AtributeClient extends BaseClient {
    @Value("${ums.get-attribute-details.url}")
    private String getAttributeDetailsUrl;

    @Autowired
    private RestTemplate restTemplate;

    public CommonSouthBoundResponse<List<AttributeDetails>> getAttributeDetails() throws BaseException {
        try {

            ParameterizedTypeReference<CommonSouthBoundResponse<List<AttributeDetails>>> typeRef = new ParameterizedTypeReference<CommonSouthBoundResponse<List<AttributeDetails>>>() {
            };
            ResponseEntity<CommonSouthBoundResponse<List<AttributeDetails>>> exchange = restTemplate
                    .exchange(getAttributeDetailsUrl, HttpMethod.GET, populateRequestEntity(), typeRef);
            return exchange.getBody();
        } catch (HttpStatusCodeException ex) {
            throw new BaseException(ex.getMessage(), ex.getMessage(), HttpStatus.SERVICE_UNAVAILABLE,
                    DisplayResultCodeEnum.GET_ALL_ATTRIBUTE_DETAILS_FOR_CACHE_FAILED.code(), ex.getStackTrace());
        } catch (Exception ex) {
            throw new BaseException(ex.getMessage(),
                    DisplayResultCodeEnum.GET_ALL_ATTRIBUTE_DETAILS_FOR_CACHE_FAILED.description(),
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    DisplayResultCodeEnum.GET_ALL_ATTRIBUTE_DETAILS_FOR_CACHE_FAILED.code(), ex.getStackTrace());
        }
    }
}
