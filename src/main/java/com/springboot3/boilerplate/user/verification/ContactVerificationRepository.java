package com.springboot3.boilerplate.user.verification;

import com.springboot3.boilerplate.app.enums.ContactType;
import com.springboot3.boilerplate.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface ContactVerificationRepository extends JpaRepository<ContactVerification, String> {

    ContactVerification findByToken(String token);

    ContactVerification findByUserIdAndType(Long id, Enum type);

    boolean existsByUserAndType(User user, ContactType contactType);

    Optional<ContactVerification> findByUserAndType(User user, ContactType contactType);
}