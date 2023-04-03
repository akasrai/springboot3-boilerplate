package com.springboot3.boilerplate.user;

import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
public enum UserEmailEvent {
    RESET_PASSWORD("Reset Password", "reset-password.ftl"),
    VERIFY_EMAIL("Verify Email Address","email-verification.ftl");


    private final String subject;
    private final String template;

    UserEmailEvent(String subject, String template) {
        this.subject = subject;
        this.template = template;
    }

//    private static final Map<String, UserEmailEvent> EVENT_MAP = Arrays.stream(UserEmailEvent.values())
//            .collect(Collectors.toMap(UserEmailEvent::getSubject, Function.identity()));
//
//    public static UserEmailEvent get(String value) {
//        return EVENT_MAP.get(value);
//    }
}
