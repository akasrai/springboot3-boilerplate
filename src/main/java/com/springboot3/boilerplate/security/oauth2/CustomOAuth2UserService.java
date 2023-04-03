package com.springboot3.boilerplate.security.oauth2;

import com.springboot3.boilerplate.app.enums.AuthProvider;
import com.springboot3.boilerplate.user.role.RoleType;
import com.springboot3.boilerplate.app.exception.OAuth2AuthenticationProcessingException;
import com.springboot3.boilerplate.miscellaneous.util.UUIDGenerator;
import com.springboot3.boilerplate.security.UserPrincipal;
import com.springboot3.boilerplate.security.oauth2.user.OAuth2UserInfo;
import com.springboot3.boilerplate.security.oauth2.user.OAuth2UserInfoFactory;
import com.springboot3.boilerplate.user.enduser.Enduser;
import com.springboot3.boilerplate.user.enduser.EnduserRepository;
import com.springboot3.boilerplate.user.role.Role;
import com.springboot3.boilerplate.user.role.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private EnduserRepository enduserRepository;

    @Autowired
    private RoleService roleService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(oAuth2UserRequest);

        try {
            return processOAuth2User(oAuth2UserRequest, oAuth2User);
        } catch (AuthenticationException ex) {
            throw ex;
        } catch (Exception ex) {
            // Throwing an instance of AuthenticationException will trigger the OAuth2AuthenticationFailureHandler
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex.getCause());
        }
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) {
        OAuth2UserInfo oAuth2UserInfo =
                OAuth2UserInfoFactory.getOAuth2UserInfo(oAuth2UserRequest.getClientRegistration().getRegistrationId()
                        , oAuth2User.getAttributes());
        if (StringUtils.isEmpty(oAuth2UserInfo.getEmail())) {
            throw new OAuth2AuthenticationProcessingException("Email not found from OAuth2 provider");
        }

        Optional<Enduser> fundraiserOptional = enduserRepository.findByEmail(oAuth2UserInfo.getEmail());
        Enduser enduser;

        if (fundraiserOptional.isPresent()) {
            enduser = fundraiserOptional.get();
            if (!enduser.getProvider().equals(AuthProvider.get(oAuth2UserRequest.getClientRegistration().getRegistrationId()))) {
                throw new OAuth2AuthenticationProcessingException(String.format("Looks like you're signed up with %s " +
                        "account. Please use your %s account to login.", enduser.getProvider(), enduser.getProvider()));
            }

            // fixme: updates data from OAuth2 in every sign up
            // fundraiser = updateExistingMember(fundraiser, oAuth2UserInfo);
        } else {
            enduser = registerMember(oAuth2UserRequest, oAuth2UserInfo);
        }

        return UserPrincipal.create(enduser, oAuth2User.getAttributes());
    }

    private Enduser registerMember(OAuth2UserRequest oAuth2UserRequest, OAuth2UserInfo oAuth2UserInfo) {
        Enduser enduser = new Enduser();
        Role roleUser = roleService.findByName(RoleType.ROLE_GUEST);

        enduser.setProvider(AuthProvider.get(oAuth2UserRequest.getClientRegistration().getRegistrationId()));
        enduser.setEmailVerified(true);
        enduser.setEmail(oAuth2UserInfo.getEmail());
        enduser.setProviderId(oAuth2UserInfo.getId());
        enduser.setImageUrl(oAuth2UserInfo.getImageUrl());
        enduser.setLastName(oAuth2UserInfo.getLastName());
        enduser.setReferenceId(UUIDGenerator.randomUUID());
        enduser.setFirstName(oAuth2UserInfo.getFirstName());
        enduser.setMiddleName(oAuth2UserInfo.getMiddleName());
        enduser.setRoles(new ArrayList<>(Collections.singletonList(roleUser)));

        return enduserRepository.save(enduser);
    }

    private Enduser updateExistingMember(Enduser existingEnduser, OAuth2UserInfo oAuth2UserInfo) {
        existingEnduser.setImageUrl(oAuth2UserInfo.getImageUrl());
        existingEnduser.setLastName(oAuth2UserInfo.getLastName());
        existingEnduser.setFirstName(oAuth2UserInfo.getFirstName());
        existingEnduser.setMiddleName(oAuth2UserInfo.getMiddleName());

        return enduserRepository.save(existingEnduser);
    }
}