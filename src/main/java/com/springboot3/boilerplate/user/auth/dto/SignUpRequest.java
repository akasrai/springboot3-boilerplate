package com.springboot3.boilerplate.user.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SignUpRequest {
    @NotNull
    private String firstName;

    private String middleName;

    @NotNull
    private String lastName;

    @NotNull
    @Email
    private String email;

    private  String countryCode;

    @Size(min=6, max=10)
    private String phoneNumber;

    @NotNull
    private String password;
}