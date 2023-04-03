package com.springboot3.boilerplate.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot3.boilerplate.user.User;
import com.springboot3.boilerplate.user.UserService;
import com.springboot3.boilerplate.user.auth.AuthService;
import com.springboot3.boilerplate.user.auth.dto.AuthResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.http.entity.ContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;

    @Autowired
    private TokenProvider tokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        response.setStatus(HttpStatus.OK.value());
        response.setContentType(ContentType.APPLICATION_JSON.getMimeType());
        objectMapper.writeValue(response.getWriter(), getAuthResponse(authentication));
    }

    private AuthResponse getAuthResponse(Authentication authentication) {
        User user = userService.findByEmail(authentication.getName());
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        String token = tokenProvider.createAccessToken(userPrincipal.getId());

        return authService.buildAuthResponse(user, token);
    }
}
