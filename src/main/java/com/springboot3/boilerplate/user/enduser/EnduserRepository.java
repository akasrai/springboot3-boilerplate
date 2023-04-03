package com.springboot3.boilerplate.user.enduser;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EnduserRepository extends JpaRepository<Enduser, Long> {

    Optional<Enduser> findByEmail(String email);

    Optional<Enduser> findByReferenceId(UUID referenceId);

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);

    List<Enduser> findAllByLocked(boolean isLocked);
}