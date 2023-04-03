package com.springboot3.boilerplate.user.auth;

import com.springboot3.boilerplate.app.enums.ContactType;
import com.springboot3.boilerplate.user.role.RoleType;
import com.springboot3.boilerplate.app.exception.BadRequestException;
import com.springboot3.boilerplate.miscellaneous.redis.AuthToken;
import com.springboot3.boilerplate.miscellaneous.redis.AuthTokenService;
import com.springboot3.boilerplate.security.TokenProvider;
import com.springboot3.boilerplate.security.UserPrincipal;
import com.springboot3.boilerplate.user.User;
import com.springboot3.boilerplate.user.UserService;
import com.springboot3.boilerplate.user.auth.dto.AccessTokenRequest;
import com.springboot3.boilerplate.user.auth.dto.AccessTokenResponse;
import com.springboot3.boilerplate.user.auth.dto.AuthResponse;
import com.springboot3.boilerplate.user.auth.dto.LoginRequest;
import com.springboot3.boilerplate.user.auth.dto.OAuth2Response;
import com.springboot3.boilerplate.user.auth.dto.OAuth2SignUpRequest;
import com.springboot3.boilerplate.user.auth.dto.SignUpRequest;
import com.springboot3.boilerplate.user.enduser.Enduser;
import com.springboot3.boilerplate.user.enduser.EnduserMapper;
import com.springboot3.boilerplate.user.enduser.EnduserService;
import com.springboot3.boilerplate.user.role.Role;
import com.springboot3.boilerplate.user.role.RoleService;
import com.springboot3.boilerplate.user.verification.ContactVerificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuthService {
    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private TokenProvider tokenProvider;

    @Autowired
    private EnduserMapper enduserMapper;

    @Autowired
    private AuthTokenService authTokenService;

    @Autowired
    private EnduserService enduserService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private ContactVerificationService contactVerificationService;

    private final Logger logger = LoggerFactory.getLogger(AuthService.class);

    public AuthResponse login(LoginRequest loginRequest) {
        String token = authenticate(loginRequest.getEmail(), loginRequest.getPassword());
        User user = userService.findByEmail(loginRequest.getEmail());

        return buildAuthResponse(user, token);
    }

    public AuthResponse signUp(SignUpRequest signUpRequest) {

        if (enduserService.isEmailDuplicate(signUpRequest.getEmail())) {
            throw new BadRequestException("Member with given email already exists.");
        }

        Enduser enduser = enduserService.create(signUpRequest);
        String token = authenticate(signUpRequest.getEmail(), signUpRequest.getPassword());

        contactVerificationService.createDeviceVerification(enduser.getId(), ContactType.EMAIL);
        contactVerificationService.createDeviceVerification(enduser.getId(), ContactType.PHONE);

        return buildAuthResponse(enduser, token);
    }

    private String authenticate(String email, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        email,
                        password
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        return tokenProvider.createAccessToken(userPrincipal.getId());
    }

    public AuthResponse buildAuthResponse(User user, String token) {
        AuthResponse authResponse = new AuthResponse();
        authResponse.setToken(token);
        authResponse.setRoles(user.getRoles().stream()
                .map(role -> RoleType.valueOf(role.getName().toString()).toString().split("_")[1])
                .collect(Collectors.toList()));

        return authResponse;
    }

    public OAuth2Response getOAuth2User(Long senderId) {
        Enduser enduser = enduserService.findById(senderId);
        Role role = roleService.findByName(RoleType.ROLE_GUEST);

        if(enduser.getRoles().contains(role)) {
            return enduserMapper.toOAuth2Response(enduser);
        }

        return buildOAuth2AuthResponse(enduser, senderId);
    }

    private OAuth2Response buildOAuth2AuthResponse(Enduser enduser, Long senderId) {
        OAuth2Response oAuth2Response = new OAuth2Response();
        oAuth2Response.setVerified(true);
        oAuth2Response.setToken(authTokenService.getReferenceTokenByUser(senderId));
        oAuth2Response.setRoles(enduser.getRoles().stream()
                .map(r -> RoleType.valueOf(r.getName().toString()).toString().split("_")[1])
                .collect(Collectors.toList()));

        return oAuth2Response;
    }

    public AuthResponse completeOauth2SignUp(OAuth2SignUpRequest oauth2SignUpRequest,
                                             UserPrincipal userPrincipal) {
        if (enduserService.isPhoneDuplicate(oauth2SignUpRequest.getPhoneNumber())) {
            throw new BadRequestException("Phone number already in use.");
        }

        Enduser enduser = enduserService.updateOauth2Fundraiser(oauth2SignUpRequest, userPrincipal.getId());
        grantNewAuthentication(enduser);
        contactVerificationService.createDeviceVerification(enduser.getId(), ContactType.PHONE);

        return buildAuthResponse(enduser,authTokenService.getReferenceTokenByUser(userPrincipal.getId()));
    }

    private void grantNewAuthentication(Enduser sender) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        List<GrantedAuthority> updatedAuthorities = new ArrayList<>(authentication.getAuthorities());

//        List<String> privileges = UserPrincipal.getPrivileges(sender.getRoles());
//
//        for (String privilege : privileges) {
//            updatedAuthorities.add(new SimpleGrantedAuthority(privilege));
//        }

        Authentication newAuthentication = new UsernamePasswordAuthenticationToken(authentication.getPrincipal(),
                authentication.getCredentials(),
                updatedAuthorities);
        SecurityContextHolder.getContext().setAuthentication(newAuthentication);
    }

    public AccessTokenResponse refreshAccessToken(AccessTokenRequest accessTokenRequest) {
        AuthToken authToken = authTokenService.getAuthToken(accessTokenRequest.getReferenceToken());
        String referenceToken = tokenProvider.createAccessToken(authToken.getUserId());

        logger.info("Removing expired pair of auth tokens from redis with referenceId:{}",
                accessTokenRequest.getReferenceToken());
        authTokenService.deleteAuthTokenByReferenceToken(accessTokenRequest.getReferenceToken());

        return new AccessTokenResponse(referenceToken);
    }
}
