package com.springboot3.boilerplate.miscellaneous.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot3.boilerplate.app.Constants;
import com.springboot3.boilerplate.user.auth.dto.LoginRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.InternalAuthenticationServiceException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class HttpServletRequestUtils {
    private static final Logger logger = LoggerFactory.getLogger(HttpServletRequestUtils.class);

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private HttpServletRequestUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static byte[] getRequestReaderByte(HttpServletRequest request) {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            IOUtils.copy(request.getReader(), byteArrayOutputStream, "UTF-8");
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            logger.error(Constants.PARSE_ERROR, e);
            throw new InternalAuthenticationServiceException(Constants.PARSE_ERROR, e);
        }
    }

    public static LoginRequest getAuthRequest(byte[] bytes) {
        try {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
            String requestBody = IOUtils.toString(byteArrayInputStream, "UTF-8");

            return objectMapper.readValue(requestBody, LoginRequest.class);
        } catch (IOException e) {
            logger.error(Constants.PARSE_ERROR, e);
            throw new InternalAuthenticationServiceException(Constants.PARSE_ERROR, e);
        }
    }
}
