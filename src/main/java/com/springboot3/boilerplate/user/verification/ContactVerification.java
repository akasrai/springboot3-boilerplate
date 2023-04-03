package com.springboot3.boilerplate.user.verification;

import com.springboot3.boilerplate.app.AuditModel;
import com.springboot3.boilerplate.app.enums.ContactType;
import com.springboot3.boilerplate.app.enums.ContactVerificationStatus;
import com.springboot3.boilerplate.user.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class ContactVerification extends AuditModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    @Enumerated(EnumType.STRING)
    private ContactType type;

    @Column(nullable = false)
    private String token;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ContactVerificationStatus status = ContactVerificationStatus.PENDING;

    @Column
    private Integer verificationAttempt;

    @Column
    private LocalDateTime expiryDate;

    @Column
    private LocalDateTime verifiedAt;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    private User user;

    @Column
    private int resendAttempt;
}
