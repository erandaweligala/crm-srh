package com.adl.et.telco.crm.securerequesthandler.domain.service.serviceinterface.common;


import org.json.simple.parser.ParseException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public interface ExternalCrmExtensionAPICallService {

    String externalGetAPICall(String url, Map<String, String> requestParam) throws  ParseException;

    String externalPostAPICall(String url, Object request,Map<String, String> requestParam) throws  ParseException;

    String externalPutAPICall(String url, Object request,Map<String, String> valueMap) throws  ParseException;

    String externalDeleteAPICall(String url, Object request,Map<String, String> valueMap) throws  ParseException;

    InputStream externalGetFileAsAStreamForPostEndPoint(String url,Object request, Map<String, String> valueMap) throws IOException;

    String externalPostUploadAPICall(String url, MultipartFile multipartFile, Map<String, String> valueMap) throws IOException,  ParseException;



}
