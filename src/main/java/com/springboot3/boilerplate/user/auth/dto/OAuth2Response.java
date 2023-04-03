package com.springboot3.boilerplate.user.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OAuth2Response {
    private String firstName;

    private String middleName;

    private String lastName;

    private String email;

    private String phoneNumber;

    private boolean verified;

    private String token;

    private List<String> roles;
}
