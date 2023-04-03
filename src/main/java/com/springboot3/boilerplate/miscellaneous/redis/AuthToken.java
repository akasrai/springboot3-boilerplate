package com.springboot3.boilerplate.miscellaneous.redis;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import java.io.Serializable;

@Setter
@Getter
@RedisHash("AuthToken")
public class AuthToken implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Indexed
    private Long userId;

    @Indexed
    private String JWTToken;

    @Indexed
    private String referenceToken;
}
