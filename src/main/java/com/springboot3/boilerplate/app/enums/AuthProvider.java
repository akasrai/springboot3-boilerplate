package com.springboot3.boilerplate.app.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
public enum AuthProvider {
    SYSTEM("system"),
    FACEBOOK("facebook"),
    GOOGLE("google");

    private final String provider;

    AuthProvider(String provider) {
        this.provider = provider;
    }

    private static final Map<String, AuthProvider> authProviderHashMap = Arrays.stream(AuthProvider.values())
            .collect(Collectors.toMap(AuthProvider::getProvider, Function.identity()));

    public static AuthProvider get(String value) {
        return authProviderHashMap.get(value);
    }
}
