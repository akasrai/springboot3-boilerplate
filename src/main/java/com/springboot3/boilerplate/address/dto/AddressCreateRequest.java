package com.springboot3.boilerplate.address.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddressCreateRequest {
    private String street;

    private String city;

    private String zipCode;

    private String state;

    private String country;

    private String district;

    private String type;
}
