package com.springboot3.boilerplate.app;

public final class Constants {
    public static final int LOGIN_ATTEMPT_LIMIT = 3;
    public static final int RESEND_VERIFICATION_CODE_LIMIT = 3;
    public static final String DEFAULT_PHONE_VERIFICATION_CODE = "1234";
    public static final String PARSE_ERROR = "Something went wrong while parsing /login request body";

    private Constants() {
        throw new IllegalStateException("Constant class");
    }
}

