package com.springboot3.boilerplate.user.auth;

import com.springboot3.boilerplate.app.enums.ContactType;
import com.springboot3.boilerplate.miscellaneous.redis.AuthTokenService;
import com.springboot3.boilerplate.security.CurrentUser;
import com.springboot3.boilerplate.security.UserPrincipal;
import com.springboot3.boilerplate.user.auth.dto.AccessTokenRequest;
import com.springboot3.boilerplate.user.auth.dto.AccessTokenResponse;
import com.springboot3.boilerplate.user.auth.dto.AuthResponse;
import com.springboot3.boilerplate.user.auth.dto.OAuth2Response;
import com.springboot3.boilerplate.user.auth.dto.OAuth2SignUpRequest;
import com.springboot3.boilerplate.user.auth.dto.SignUpRequest;
import com.springboot3.boilerplate.user.password.ForgotPasswordRequest;
import com.springboot3.boilerplate.user.password.PasswordService;
import com.springboot3.boilerplate.user.password.ResetPasswordRequest;
import com.springboot3.boilerplate.user.verification.ContactVerificationService;
import com.springboot3.boilerplate.user.verification.VerificationResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private AuthTokenService authTokenService;

    @Autowired
    private ContactVerificationService contactVerificationService;

    @Autowired
    private PasswordService passwordService;

    @PostMapping("/users")
    public AuthResponse registerUser(@Valid @RequestBody SignUpRequest signUpRequest) {
        return authService.signUp(signUpRequest);
    }

    @GetMapping("/verify/{type}/{token}")
    public void verify(@CurrentUser UserPrincipal userPrincipal,
                       @PathVariable("type") ContactType contactType, @PathVariable("token") String token) {
        contactVerificationService.verifyToken(userPrincipal.getId(), token, contactType);
    }

    @GetMapping("/resend-verification/{type}")
    @PreAuthorize("hasRole('USER')")
    public void resendVerification(@CurrentUser UserPrincipal userPrincipal,
                                   @PathVariable("type") ContactType contactType) {
        contactVerificationService.createDeviceVerification(userPrincipal.getId(), contactType);
    }

    @GetMapping("/oauth2")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public OAuth2Response getOAuth2Response(@CurrentUser UserPrincipal userPrincipal) {
        return authService.getOAuth2User(userPrincipal.getId());
    }

    @PostMapping("/oauth2")
    @PreAuthorize("hasRole('GUEST')")
    public AuthResponse completeOauth2SignUp(@Valid @RequestBody OAuth2SignUpRequest oauth2SignUpRequest,
                                                   @CurrentUser UserPrincipal userPrincipal) {
        return authService.completeOauth2SignUp(oauth2SignUpRequest, userPrincipal);
    }

    @GetMapping("/sign-out")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public void logOut(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null) {
            String referenceToken = authHeader.replace("Bearer", "").trim();
            authTokenService.deleteAuthTokenByReferenceToken(referenceToken);
        }
    }

    @PostMapping("/forgot-password")
    public void forgotPassword(@RequestBody ForgotPasswordRequest forgotPasswordRequest) {
        passwordService.sendResetPasswordMail(forgotPasswordRequest.getEmail());
    }

    @PutMapping("/reset-password/{recoveryToken}")
    public VerificationResponse resetPassword(@PathVariable String recoveryToken,
                                              @RequestBody @Valid ResetPasswordRequest resetPasswordRequest) {
        return passwordService.resetPassword(recoveryToken, resetPasswordRequest.getNewPassword());
    }

    @PutMapping("/reset-password")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public VerificationResponse resetLoggedInUserPassword(@CurrentUser UserPrincipal userPrincipal,
                                                          @RequestBody @Valid ResetPasswordRequest resetPasswordRequest) {
        return passwordService.resetPassword(userPrincipal, resetPasswordRequest.getNewPassword(),
                resetPasswordRequest.getOldPassword());
    }

    @PostMapping("/token")
    public AccessTokenResponse refreshAccessToken(@Valid @RequestBody AccessTokenRequest accessTokenRequest) {
        return authService.refreshAccessToken(accessTokenRequest);
    }
}