package com.springboot3.boilerplate.miscellaneous.redis;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthTokenRepository extends CrudRepository<AuthToken, String> {
    Optional<AuthToken> findByReferenceToken(String referenceToken);

    Optional<AuthToken> findByUserId(Long userId);
}
