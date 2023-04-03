package com.springboot3.boilerplate.user.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccessTokenRequest {
    @NotBlank
    private String referenceToken;
}
