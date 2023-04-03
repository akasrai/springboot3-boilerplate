package com.springboot3.boilerplate.address;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column
    private String street;

    @Column
    private String city;

    @Column
    private String zipCode;

    @Column
    private String state;

    @Column
    private String country;

    @Column
    private String district;

    @Column
    @Enumerated(EnumType.STRING)
    private AddressType type;
}
