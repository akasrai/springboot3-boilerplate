package com.springboot3.boilerplate.miscellaneous.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.util.Date;

public class JWTGenerator {
    JWTGenerator() {
    }

    public static String generateJWTToken(String subject) {
        // Note: we are using JWT as tokens for different purpose like reset password.
        // This JWT doesn't contain any information and is bing only used
        // as reference token not login token
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date())
                .signWith(Keys.secretKeyFor(SignatureAlgorithm.HS512))
                .compact();
    }
}
