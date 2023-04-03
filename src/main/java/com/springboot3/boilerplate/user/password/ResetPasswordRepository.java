package com.springboot3.boilerplate.user.password;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface ResetPasswordRepository extends JpaRepository<ResetPassword, Long> {
    ResetPassword findByToken(String token);

    ResetPassword findByUserIdAndExpiryDateIsAfter(Long userId, LocalDateTime localDateTime);
}
