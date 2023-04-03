package com.springboot3.boilerplate.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.http.entity.ContentType;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final Logger logger = LoggerFactory.getLogger(RestAuthenticationEntryPoint.class);

    @Override
    public void commence(HttpServletRequest httpServletRequest,
                         HttpServletResponse httpServletResponse,
                         AuthenticationException e) throws IOException, ServletException {
        JSONObject response = new JSONObject();
        response.put("status", HttpServletResponse.SC_UNAUTHORIZED);
        response.put("error", "Unauthorized");
        response.put("message", e.getLocalizedMessage());
        response.put("timestamp", Instant.now());

        logger.error("Responding with unauthorized error. Message - {}", e.getMessage());
        httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        httpServletResponse.setContentType(ContentType.APPLICATION_JSON.getMimeType());
        httpServletResponse.getWriter().write(response.toString());
    }
}