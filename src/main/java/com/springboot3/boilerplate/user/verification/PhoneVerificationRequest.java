package com.springboot3.boilerplate.user.verification;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PhoneVerificationRequest {
    @NotBlank
    private String verificationCode;
}
