package com.springboot3.boilerplate.security;

import com.springboot3.boilerplate.miscellaneous.redis.AuthTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(TokenAuthenticationFilter.class);

    @Autowired
    private TokenProvider tokenProvider;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private AuthTokenService authTokenService;

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response,
                                    @NotNull FilterChain filterChain) throws ServletException, IOException {
        String jwtToken = getJWTToken(request);

        if (StringUtils.hasText(jwtToken) && !tokenProvider.isTokenExpired(jwtToken)) {
            Long userId = tokenProvider.extractUserId(jwtToken);

            try {
                if (tokenProvider.validateToken(jwtToken, userId)) {
                    UserDetails userDetails = customUserDetailsService.loadUserById(userId);
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (Exception ex) {
                LOGGER.error("Could not set user authentication in security context", ex);
            }
        }

        filterChain.doFilter(request, response);
    }

    private String getJWTToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        return (authHeader != null)
                ? authTokenService.getJWTToken(authHeader.replace("Bearer", "").trim())
                : "";
    }
}