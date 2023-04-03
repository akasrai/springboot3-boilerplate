package com.springboot3.boilerplate.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Boolean existsByEmail(String email);

    Optional<User> findById(Long id);

    @Query("SELECT u.loginAttempts FROM User u WHERE u.email=?1")
    Integer getLoginAttempts(String email);
}