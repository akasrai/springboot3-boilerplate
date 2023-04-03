package com.springboot3.boilerplate.security;

import com.springboot3.boilerplate.miscellaneous.util.HttpServletRequestUtils;
import com.springboot3.boilerplate.user.auth.dto.LoginRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

public class CustomUsernamePasswordAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        byte[] bytes = HttpServletRequestUtils.getRequestReaderByte(request);
        LoginRequest authRequest = HttpServletRequestUtils.getAuthRequest(bytes);
        UsernamePasswordAuthenticationToken token
                = new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPassword());
        setDetails(request, token);

        return this.getAuthenticationManager().authenticate(token);
    }
}
