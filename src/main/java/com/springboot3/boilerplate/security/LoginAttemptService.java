package com.springboot3.boilerplate.security;

import com.springboot3.boilerplate.app.Constants;
import com.springboot3.boilerplate.app.enums.AuthProvider;
import com.springboot3.boilerplate.app.exception.ResourceNotFoundException;
import com.springboot3.boilerplate.user.User;
import com.springboot3.boilerplate.user.UserLockReason;
import com.springboot3.boilerplate.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LoginAttemptService {

    @Autowired
    private UserService userService;

    public void loginSucceeded(String email) {
        User user = userService.findByEmail(email);
        user.setLoginAttempts(0);
        userService.save(user);
    }

    public void loginFailed(String email) {
        User user = userService.findByEmail(email);
        if (!AuthProvider.FACEBOOK.equals(user.getProvider()) && !AuthProvider.GOOGLE.equals(user.getProvider())) {
            user.setLoginAttempts(user.getLoginAttempts() + 1);
            userService.save(user);
            lockIfUserHasMaxLoginAttempts(email);
        }
    }

    public boolean isLocked(String email) {
        try {
            User user = userService.findByEmail(email);

            return user.isLocked();
        } catch (ResourceNotFoundException e) {
            return false;
        }
    }

    private void lockIfUserHasMaxLoginAttempts(String email) {
        User user = userService.findByEmail(email);
        if (user.getLoginAttempts() >= Constants.LOGIN_ATTEMPT_LIMIT) {
            userService.lock(email, UserLockReason.LOGIN_ATTEMPT_LIMIT_EXCEEDED);
        }
    }
}
