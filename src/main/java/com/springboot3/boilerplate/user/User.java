package com.springboot3.boilerplate.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.springboot3.boilerplate.app.AuditModel;
import com.springboot3.boilerplate.app.enums.AuthProvider;
import com.springboot3.boilerplate.user.role.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

@Entity
@Getter
@Setter
@Inheritance(strategy = InheritanceType.JOINED)
public class User extends AuditModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private boolean emailVerified;

    @Column(unique = true)
    private String phoneNumber;

    @Column(nullable = false)
    private boolean phoneNumberVerified;

    @JsonIgnore
    private String password;

    @Column
    private boolean passwordExpired;

    @Column
    private int loginAttempts;

    @Column
    private boolean locked;

    @Column
    @Enumerated(EnumType.STRING)
    private UserLockReason lockedReason;

    @NotNull
    @Enumerated(EnumType.STRING)
    private AuthProvider provider;

    @Column
    private String providerId;

    @ManyToMany()
    @LazyCollection(LazyCollectionOption.FALSE)
    @JoinTable(
            name = "users_roles",
            joinColumns = @JoinColumn(
                    name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(
                    name = "role_id", referencedColumnName = "id"))
    private Collection<Role> roles;
}