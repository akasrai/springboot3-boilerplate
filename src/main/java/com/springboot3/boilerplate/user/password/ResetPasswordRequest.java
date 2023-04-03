package com.springboot3.boilerplate.user.password;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

@Getter
public class ResetPasswordRequest {
    @NotBlank
    @Pattern(regexp = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{6,20}$", message = "Invalid password format.")
    private String newPassword;

    private String oldPassword;
}
