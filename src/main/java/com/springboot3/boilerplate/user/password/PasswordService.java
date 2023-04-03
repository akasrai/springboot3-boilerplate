package com.springboot3.boilerplate.user.password;

import com.springboot3.boilerplate.app.exception.BadRequestException;
import com.springboot3.boilerplate.app.exception.TokenExpiredException;
import com.springboot3.boilerplate.miscellaneous.util.JWTGenerator;
import com.springboot3.boilerplate.security.UserPrincipal;
import com.springboot3.boilerplate.user.User;
import com.springboot3.boilerplate.user.UserEmailEvent;
import com.springboot3.boilerplate.user.UserMailService;
import com.springboot3.boilerplate.user.UserService;
import com.springboot3.boilerplate.user.enduser.Enduser;
import com.springboot3.boilerplate.user.enduser.EnduserService;
import com.springboot3.boilerplate.user.verification.VerificationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class PasswordService {

    @Autowired
    private ResetPasswordRepository resetPasswordRepository;

    @Autowired
    private EnduserService senderService;

    @Autowired
    private UserMailService userMailService;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public ResetPassword create(Enduser sender) {
        ResetPassword previousRequest =
                resetPasswordRepository.findByUserIdAndExpiryDateIsAfter(sender.getId(), LocalDateTime.now());
        if (previousRequest != null) {
            previousRequest.setExpiryDate(LocalDateTime.now());
        }

        String token = JWTGenerator.generateJWTToken(sender.getEmail());
        ResetPassword resetPassword = new ResetPassword();

        resetPassword.setUser(sender);
        resetPassword.setToken(token);
        resetPassword.setExpiryDate(LocalDateTime.now().plusMinutes(30L));
        resetPassword.setResetAttempt(0);

        return resetPasswordRepository.save(resetPassword);
    }

    public void sendResetPasswordMail(String email) {
        Enduser sender = senderService.findByEmail(email);
        ResetPassword resetPassword = create(sender);
        userMailService.sendMail(sender, UserEmailEvent.RESET_PASSWORD, resetPassword.getToken());
    }

    public VerificationResponse resetPassword(String recoveryToken, String newPassword) {
        ResetPassword resetPassword = resetPasswordRepository.findByToken(recoveryToken);
        VerificationResponse verificationResponse = new VerificationResponse();

        if (resetPassword == null) {
            throw new TokenExpiredException("Invalid password reset link. Please send a new request.");
        }
        if (resetPassword.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new TokenExpiredException("Your password reset link has expired. Please send a new request.");
        }

        int resetAttempt = resetPassword.getResetAttempt() + 1;
        resetPassword.setResetAttempt(resetAttempt);
        User user = resetPassword.getUser();
        userService.resetPassword(user, newPassword);
        resetPassword.setExpiryDate(LocalDateTime.now());
        resetPasswordRepository.save(resetPassword);

        verificationResponse.setStatus(true);
        verificationResponse.setMessage("Your password has been reset. Please sign in");

        return verificationResponse;
    }

    public VerificationResponse resetPassword(UserPrincipal userPrincipal, String newPassword,
                                              String oldPassword) {
        VerificationResponse verificationResponse = new VerificationResponse();
        User user = userService.findById(userPrincipal.getId());
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BadRequestException("Your old password does not match");
        }
        userService.resetPassword(user, newPassword);

        verificationResponse.setStatus(true);
        verificationResponse.setMessage("Your password has been reset");

        return verificationResponse;
    }
}
