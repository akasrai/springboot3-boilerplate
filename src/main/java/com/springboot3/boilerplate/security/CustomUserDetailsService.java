package com.springboot3.boilerplate.security;

import com.springboot3.boilerplate.app.Messages;
import com.springboot3.boilerplate.app.exception.AccountLockedException;
import com.springboot3.boilerplate.user.User;
import com.springboot3.boilerplate.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private Messages messages;

    @Autowired
    private UserService userService;

    @Autowired
    private LoginAttemptService loginAttemptService;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        if (loginAttemptService.isLocked(email)) {
            throw new AccountLockedException(messages.get("user.account.locked"));
        }

        User user = userService.findByEmail(email);

        return UserPrincipal.create(user);
    }

    @Transactional
    public UserDetails loadUserById(Long id) {
        User user = userService.findById(id);

        return UserPrincipal.create(user);
    }
}