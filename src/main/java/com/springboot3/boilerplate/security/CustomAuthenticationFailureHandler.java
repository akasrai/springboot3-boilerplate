package com.springboot3.boilerplate.security;

import com.springboot3.boilerplate.app.Constants;
import com.springboot3.boilerplate.app.Messages;
import com.springboot3.boilerplate.app.enums.AuthProvider;
import com.springboot3.boilerplate.app.exception.ResourceNotFoundException;
import com.springboot3.boilerplate.miscellaneous.util.HttpServletRequestUtils;
import com.springboot3.boilerplate.user.User;
import com.springboot3.boilerplate.user.UserService;
import com.springboot3.boilerplate.user.auth.dto.LoginRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.http.entity.ContentType;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

@Component("authenticationFailureHandler")
public class CustomAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Autowired
    private Messages messages;

    @Autowired
    private UserService userService;

    @Override
    public void onAuthenticationFailure(final HttpServletRequest httpServletRequest,
                                        final HttpServletResponse httpServletResponse,
                                        final AuthenticationException exception)
            throws IOException, AuthenticationException {
        String error = exception.getMessage();

        if (error.equalsIgnoreCase("Bad credentials")) {
            AuthProvider provider = getProvider(httpServletRequest);

            if (provider.equals(AuthProvider.FACEBOOK) || provider.equals(AuthProvider.GOOGLE)) {
                error = messages.get("user.account.pleaseProceedWithSocialLogin", provider.getProvider());
            } else {
                Integer loginAttempts = getRemainingLoginAttempts(httpServletRequest);
                error = (loginAttempts > 0)
                        ? messages.get("user.account.badCredentials", loginAttempts.toString())
                        : messages.get("user.account.locked");
            }
        }

        if (error.contains("user not found with email")) {
            error = messages.get("user.account.userNotFoundWithGivenEmail");
        }

        JSONObject response = new JSONObject();
        response.put("status", HttpServletResponse.SC_UNAUTHORIZED);
        response.put("error", "Unauthorized");
        response.put("message", error);
        response.put("timestamp", Instant.now());

        httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        httpServletResponse.setContentType(ContentType.APPLICATION_JSON.getMimeType());
        httpServletResponse.getWriter().write(response.toString());

    }

    private AuthProvider getProvider(HttpServletRequest request) {
        byte[] bytes = HttpServletRequestUtils.getRequestReaderByte(request);
        String email = HttpServletRequestUtils.getAuthRequest(bytes).getEmail();

        try {
            User user = userService.findByEmail(email);

            return user.getProvider();
        } catch (ResourceNotFoundException e) {
            return AuthProvider.SYSTEM;
        }

    }

    private Integer getRemainingLoginAttempts(HttpServletRequest request) {
        byte[] bytes = HttpServletRequestUtils.getRequestReaderByte(request);
        LoginRequest authRequest = HttpServletRequestUtils.getAuthRequest(bytes);

        return Constants.LOGIN_ATTEMPT_LIMIT - userService.getLoginAttempts(authRequest.getEmail());
    }
}
