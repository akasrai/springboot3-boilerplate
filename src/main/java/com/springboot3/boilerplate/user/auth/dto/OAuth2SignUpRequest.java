package com.springboot3.boilerplate.user.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OAuth2SignUpRequest {
    @NotNull
    private String firstName;

    private String middleName;

    @NotNull
    private String lastName;

    @NotBlank
    @Size(min=6, max=10)
    private String phoneNumber;
}
