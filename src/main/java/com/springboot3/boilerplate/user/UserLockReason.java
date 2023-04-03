package com.springboot3.boilerplate.user;

import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
public enum UserLockReason {
    LOGIN_ATTEMPT_LIMIT_EXCEEDED("Login attempt limit exceeded"),
    RESEND_VERIFICATION_CODE_LIMIT_EXCEEDED("Resending verification code limit exceeded");

    private final String lockReason;

    UserLockReason(String lockReason) {
        this.lockReason = lockReason;
    }

    private static final Map<String, UserLockReason> lockReasonHashMap = Arrays.stream(UserLockReason.values())
            .collect(Collectors.toMap(UserLockReason::getLockReason, Function.identity()));

    public static UserLockReason get(String value) {
        return lockReasonHashMap.get(value);
    }

}
