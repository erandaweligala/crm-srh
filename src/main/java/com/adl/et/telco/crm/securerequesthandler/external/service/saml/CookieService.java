package com.adl.et.telco.crm.securerequesthandler.external.service.saml;


import com.adl.et.telco.crm.securerequesthandler.application.util.constants.ServiceConstants;
import com.adl.et.telco.crm.securerequesthandler.application.util.exception.BaseException;
import com.adl.et.telco.crm.securerequesthandler.application.util.resultenum.ResponseCodeEnum;
import jakarta.servlet.http.Cookie;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;




@Service
public class CookieService {
    @Value("${cookie.max-age.sec}")
    private int cookieMaxAge;

    public Cookie createCookie(String value) throws BaseException {
        try {
            // create a cookie
            Cookie cookie = new Cookie(ServiceConstants.RV_TOKEN, value);
            cookie.setMaxAge(cookieMaxAge);
            // optional properties
            /**ToDo Should uncomment on production*/
//            cookie.setSecure(true);
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            return cookie;
        } catch (Exception ex) {
            throw new BaseException(ex.getMessage(), ResponseCodeEnum.EXCEPTION_SERVICE_LAYER.description(), HttpStatus.INTERNAL_SERVER_ERROR, ResponseCodeEnum.EXCEPTION_SERVICE_LAYER.code(), ex.getStackTrace());
        }
    }
}
