package com.springboot3.boilerplate.user.enduser.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EnduserResponse {
    private String firstName;

    private String middleName;

    private String lastName;

    private String fullName;

    private String email;

    private String phoneNumber;

    private String imageUrl;
}
