package com.springboot3.boilerplate.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@Configuration
@ConfigurationProperties(prefix = "admin")
public class AdminCredentialsConfig {
    private String firstName;

    private String lastName;

    private String email;

    private String password;
}
