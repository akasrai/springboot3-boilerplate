package com.springboot3.boilerplate.app.exception;

import com.springboot3.boilerplate.security.RestAuthenticationEntryPoint;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import javax.naming.AuthenticationException;
import java.time.Instant;

@RestControllerAdvice
public class ExceptionHandlerAdvice {
    @Autowired
    private RestAuthenticationEntryPoint restAuthenticationEntryPoint;

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Object> handleAuthenticationException(AuthenticationException ex, WebRequest request) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Object> handleBadRequestException(Exception ex, WebRequest request) {
        JSONObject response = getJSONResponse(ex.getMessage());
        response.put("status", HttpServletResponse.SC_BAD_REQUEST);
        response.put("error", HttpStatus.BAD_REQUEST.getReasonPhrase());

        return new ResponseEntity<>(response.toString(), getHttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleResourceNotFoundException(Exception ex, WebRequest request) {
        JSONObject response = getJSONResponse(ex.getMessage());
        response.put("status", HttpServletResponse.SC_NOT_FOUND);
        response.put("error", HttpStatus.NOT_FOUND.getReasonPhrase());


        return new ResponseEntity<>(response.toString(), getHttpHeaders(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<Object> handleTokenExpiredException(Exception ex, WebRequest request) {
        JSONObject response = getJSONResponse(ex.getMessage());
        response.put("status", HttpServletResponse.SC_GONE);
        response.put("error", HttpStatus.GONE.getReasonPhrase());


        return new ResponseEntity<>(response.toString(), getHttpHeaders(), HttpStatus.GONE);
    }

    private JSONObject getJSONResponse(String message) {
        JSONObject response = new JSONObject();
        response.put("message", message);
        response.put("timestamp", Instant.now());

        return response;
    }

    private HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        return headers;
    }
}
