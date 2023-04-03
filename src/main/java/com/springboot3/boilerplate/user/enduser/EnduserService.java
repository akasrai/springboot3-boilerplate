package com.springboot3.boilerplate.user.enduser;

import com.springboot3.boilerplate.app.enums.AuthProvider;
import com.springboot3.boilerplate.app.enums.ContactType;
import com.springboot3.boilerplate.user.role.RoleType;
import com.springboot3.boilerplate.app.exception.ResourceNotFoundException;
import com.springboot3.boilerplate.miscellaneous.util.UUIDGenerator;
import com.springboot3.boilerplate.user.auth.dto.OAuth2SignUpRequest;
import com.springboot3.boilerplate.user.auth.dto.SignUpRequest;
import com.springboot3.boilerplate.user.enduser.dto.EnduserResponse;
import com.springboot3.boilerplate.user.role.Role;
import com.springboot3.boilerplate.user.role.RoleService;
import com.springboot3.boilerplate.user.verification.ContactVerificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class EnduserService {
    private static final Logger logger = LoggerFactory.getLogger(EnduserService.class);

    @Autowired
    private RoleService roleService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EnduserMapper enduserMapper;

    @Autowired
    private EnduserRepository enduserRepository;

    @Autowired
    private ContactVerificationService contactVerificationService;

    public Enduser findById(Long id) {
        return enduserRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member", "id", id));
    }

    public Enduser findByReferenceId(UUID referenceId) {
        return enduserRepository.findByReferenceId(referenceId)
                .orElseThrow(() -> new ResourceNotFoundException("Member", "id", referenceId));
    }

    public Enduser findByEmail(String email) {
        return enduserRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Member", "id", email));
    }

    public boolean isEmailDuplicate(String email) {
        return enduserRepository.existsByEmail(email);
    }

    public boolean isPhoneDuplicate(String phoneNumber) {
        return enduserRepository.existsByPhoneNumber(phoneNumber);
    }

    public Enduser create(SignUpRequest signUpRequest) {
        Role roleUser = roleService.findByName(RoleType.ROLE_USER);
        Enduser enduser = enduserMapper.toEnduser(signUpRequest);
        enduser.setProvider(AuthProvider.SYSTEM);
        enduser.setReferenceId(UUIDGenerator.randomUUID());
        enduser.setRoles(new ArrayList<>(Collections.singletonList(roleUser)));
        enduser.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));

        return enduserRepository.save(enduser);
    }

    public Enduser updateOauth2Fundraiser(OAuth2SignUpRequest oauth2SignupRequest, Long userId) {
        Enduser enduser = findById(userId);
        Role roleUser = roleService.findByName(RoleType.ROLE_USER);
        enduser.setFirstName(oauth2SignupRequest.getFirstName());
        enduser.setLastName(oauth2SignupRequest.getLastName());
        enduser.setPhoneNumber(oauth2SignupRequest.getPhoneNumber());
        enduser.setRoles(new ArrayList<>(Collections.singletonList(roleUser)));

        return enduserRepository.save(enduser);
    }

    public EnduserResponse getCurrentUser(String email) {
        Enduser enduser = findByEmail(email);

        return enduserMapper.toEnduserResponse(enduser);
    }

    public List<EnduserResponse> getLockedUsers() {
        List<Enduser> endusers = enduserRepository.findAllByLocked(true);

        return enduserMapper.toEnduserResponseList(endusers);
    }

    @Transactional
    public void unLock(UUID referenceId) {
        Enduser enduser = findByReferenceId(referenceId);

        if (!enduser.isEmailVerified())
            contactVerificationService.resetResendAttempts(enduser, ContactType.EMAIL);

        if (!enduser.isPhoneNumberVerified())
            contactVerificationService.resetResendAttempts(enduser, ContactType.PHONE);

        enduser.setLoginAttempts(0);
        enduser.setLocked(false);
        enduserRepository.save(enduser);
    }
}