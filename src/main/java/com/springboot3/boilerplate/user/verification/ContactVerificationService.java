package com.springboot3.boilerplate.user.verification;

import com.springboot3.boilerplate.app.Constants;
import com.springboot3.boilerplate.app.Messages;
import com.springboot3.boilerplate.app.enums.ApplicationEnvironment;
import com.springboot3.boilerplate.app.enums.ContactType;
import com.springboot3.boilerplate.app.enums.ContactVerificationStatus;
import com.springboot3.boilerplate.miscellaneous.serversentevent.ServerSentEvent;
import com.springboot3.boilerplate.app.exception.BadRequestException;
import com.springboot3.boilerplate.app.exception.ResourceNotFoundException;
import com.springboot3.boilerplate.app.exception.TokenExpiredException;
import com.springboot3.boilerplate.miscellaneous.redis.AuthTokenService;
import com.springboot3.boilerplate.miscellaneous.serversentevent.ServerSentEventService;
import com.springboot3.boilerplate.miscellaneous.twilio.TwilioSMSService;
import com.springboot3.boilerplate.miscellaneous.twilio.VerificationCodeGenerator;
import com.springboot3.boilerplate.user.User;
import com.springboot3.boilerplate.user.UserEmailEvent;
import com.springboot3.boilerplate.user.UserLockReason;
import com.springboot3.boilerplate.user.UserMailService;
import com.springboot3.boilerplate.user.UserService;
import com.springboot3.boilerplate.user.enduser.Enduser;
import com.springboot3.boilerplate.user.enduser.EnduserRepository;
import com.springboot3.boilerplate.user.enduser.EnduserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ContactVerificationService {

    private static final Logger logger = LoggerFactory.getLogger(ContactVerificationService.class);

    @Autowired
    private Messages messages;

    @Autowired
    private Environment environment;

    @Autowired
    private UserMailService userMailService;

    @Autowired
    private ContactVerificationRepository contactVerificationRepository;

    @Autowired
    private EnduserService enduserService;

    @Autowired
    private EnduserRepository enduserRepository;

    @Autowired
    private TwilioSMSService twilioSmsService;

    @Autowired
    private AuthTokenService authTokenService;

    @Autowired
    private UserService userService;

    @Autowired
    private ServerSentEventService serverSentEventService;

    public void createDeviceVerification(Long senderId, ContactType contactType) {
        Enduser sender = enduserService.findById(senderId);

        if (!isDeviceVerified(sender, contactType))
            checkVerificationToken(sender, contactType);

        else
            throw new BadRequestException("Your " + contactType.name() + " is already verified.");
    }

    private boolean isDeviceVerified(Enduser sender, ContactType contactType) {
        return ContactType.PHONE.equals(contactType)
                ? sender.isPhoneNumberVerified()
                : sender.isEmailVerified();

    }

    private void checkVerificationToken(Enduser sender, ContactType contactType) {
        ContactVerification contactVerification = contactVerificationRepository.existsByUserAndType(sender, contactType)
                ? checkTwoFAVerificationResendAttempts(sender, contactType)
                : createVerificationToken(sender, contactType);

        sendVerificationMessage(sender, contactType, contactVerification.getToken());
    }

    private ContactVerification checkTwoFAVerificationResendAttempts(Enduser sender, ContactType contactType) {
        ContactVerification contactVerification = contactVerificationRepository.findByUserIdAndType(sender.getId(),
                contactType);

        if (contactVerification.getResendAttempt() >= Constants.RESEND_VERIFICATION_CODE_LIMIT) {
            lockAndInvalidateToken(contactVerification.getUser());

            throw new BadRequestException(messages.get("user.account.resendVerificationCodeLimitExceeded"));
        } else {
            return updateVerificationToken(contactVerification, contactType);
        }
    }

    @Transactional
    public ContactVerification createVerificationToken(Enduser sender, ContactType contactType) {
        ContactVerification contactVerification = new ContactVerification();

        contactVerification.setUser(sender);
        contactVerification.setType(contactType);
        contactVerification.setToken(generateVerificationCode(contactType));
        contactVerification.setExpiryDate(LocalDateTime.now().plusMinutes(30));
        contactVerification.setStatus(ContactVerificationStatus.PENDING);
        contactVerification.setVerificationAttempt(0);

        return contactVerificationRepository.save(contactVerification);
    }

    @Transactional
    public ContactVerification updateVerificationToken(ContactVerification contactVerification,
                                                       ContactType contactType) {
        contactVerification.setToken(generateVerificationCode(contactType));
        contactVerification.setExpiryDate(LocalDateTime.now().plusMinutes(30));
        contactVerification.setVerificationAttempt(0);
        contactVerification.setResendAttempt(contactVerification.getResendAttempt() + 1);

        return contactVerificationRepository.save(contactVerification);
    }

    private void lockAndInvalidateToken(User user) {
        userService.lock(user.getEmail(), UserLockReason.RESEND_VERIFICATION_CODE_LIMIT_EXCEEDED);
        authTokenService.deleteAuthTokenByUserId(user.getId());
    }

    @Transactional
    public void resetResendAttempts(Enduser sender, ContactType contactType) {
        ContactVerification contactVerification =
                contactVerificationRepository.findByUserAndType(sender, contactType)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "TwoFaVerification", "User", sender));
        contactVerification.setResendAttempt(0);
        contactVerificationRepository.save(contactVerification);
    }

    private String generateVerificationCode(ContactType contactType) {
        if (ContactType.PHONE.equals(contactType)) {
            if (environment.acceptsProfiles(Profiles.of(ApplicationEnvironment.PROD.getEnvironment()))) {
                return VerificationCodeGenerator.generate();
            }

            return Constants.DEFAULT_PHONE_VERIFICATION_CODE;
        }


        return VerificationCodeGenerator.generate();
    }

    private void sendVerificationMessage(Enduser sender, ContactType contactType, String token) {
        if (ContactType.PHONE.equals(contactType)) {
            sendPhoneVerificationCode(sender, token);
        } else {
            sendVerificationEmail(sender, token);
        }
    }

    private void sendVerificationEmail(Enduser sender, String token) {
        userMailService.sendMail(sender, UserEmailEvent.VERIFY_EMAIL, token);
    }

    private void sendPhoneVerificationCode(Enduser sender, String token) {
        if (environment.acceptsProfiles(Profiles.of(ApplicationEnvironment.PROD.getEnvironment()))) {
            twilioSmsService.sendVerificationCode(sender.getPhoneNumber(), token.concat(" is your verification code."));
        }
    }

    @Transactional
    public void verifyToken(Long id, String token, ContactType contactType) {
        ContactVerification contactVerification = contactVerificationRepository.findByUserIdAndType(id, contactType);
        Enduser enduser = enduserService.findById(id);

        if (contactVerification == null) {
            throw new BadRequestException("User has no verification data");
        }

        if (token.isEmpty()) {
            throw new BadRequestException("Verification token is empty.");
        }

        if (contactVerification.getExpiryDate().isBefore(LocalDateTime.now())) {
            logger.info("Verification token: [{}] expired with expiry date [{}] before [{}]", token,
                    contactVerification.getExpiryDate(), LocalDateTime.now());
            throw new TokenExpiredException("Verification token is expired.");
        }

        switch (contactType) {
            case PHONE:
                verifyPhone(enduser, contactVerification, token);
                serverSentEventService.emitEvent(ServerSentEvent.PHONE_NUMBER_VERIFIED, enduser.getId());
                break;

            case EMAIL:
                verifyEmail(contactVerification, token);
                Long referenceId = enduserService.findById(contactVerification.getUser().getId()).getId();
                serverSentEventService.emitEvent(ServerSentEvent.EMAIL_VERIFIED, referenceId);
                break;
        }
    }

    @Transactional
    public void verifyEmail(ContactVerification contactVerification, String token) {
        if (contactVerification.getToken().equals(token)) {
            contactVerification.setVerifiedAt(LocalDateTime.now());
            contactVerification.setStatus(ContactVerificationStatus.VERIFIED);
            contactVerification.getUser().setEmailVerified(true);
            contactVerification.setExpiryDate(LocalDateTime.now());
            contactVerificationRepository.save(contactVerification);
        } else {
            contactVerification.setVerificationAttempt(contactVerification.getVerificationAttempt() + 1);
            contactVerificationRepository.save(contactVerification);

            throw new BadRequestException("Invalid verification token.");
        }
    }

    @Transactional
    public void verifyPhone(Enduser sender, ContactVerification contactVerification, String token) {
        if (contactVerification.getToken().equals(token)) {
            contactVerification.setVerifiedAt(LocalDateTime.now());
            contactVerification.setStatus(ContactVerificationStatus.VERIFIED);
            contactVerification.setVerificationAttempt(contactVerification.getVerificationAttempt() + 1);
            contactVerificationRepository.save(contactVerification);

            sender.setPhoneNumberVerified(true);
            enduserRepository.save(sender);
        } else {
            contactVerification.setVerificationAttempt(contactVerification.getVerificationAttempt() + 1);
            contactVerificationRepository.save(contactVerification);

            throw new BadRequestException("Verification code does not match.");
        }
    }
}