package com.springboot3.boilerplate.app.enums;

import lombok.Getter;

@Getter
public enum ApplicationEnvironment {
    DEV("dev"),
    UAT("uat"),
    PROD("prod");

    private final String environment;

    ApplicationEnvironment(String env) {
        this.environment = env;
    }
}
