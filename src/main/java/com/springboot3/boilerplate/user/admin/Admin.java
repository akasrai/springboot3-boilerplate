package com.springboot3.boilerplate.user.admin;

import com.springboot3.boilerplate.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.stream.Collectors;
import java.util.stream.Stream;

@Entity
@Getter
@Setter
@ToString
public class Admin extends User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstName;

    @Column
    private String middleName;

    @Column(nullable = false)
    private String lastName;

    @Transient
    private String fullName;

    @Column
    private String imageUrl;

    public String getFullName() {
        return Stream
                .of(this.firstName, this.middleName, this.lastName)
                .filter(s -> s != null && !s.isEmpty())
                .collect(Collectors.joining(" "));
    }
}
