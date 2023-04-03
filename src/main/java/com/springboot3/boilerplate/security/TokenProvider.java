package com.springboot3.boilerplate.security;

import com.springboot3.boilerplate.config.AppConfig;
import com.springboot3.boilerplate.miscellaneous.redis.AuthTokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class TokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(TokenProvider.class);

    private final AppConfig appConfig;

    @Autowired
    private AuthTokenService authTokenService;

    public TokenProvider(AppConfig appConfig) {
        this.appConfig = appConfig;
    }

    public String createAccessToken(Long userId) {
        logger.info("Generating JWT access token");
        String token = generateToken(new HashMap<>(), userId);

        return authTokenService.create(token, userId);
    }

    public Long extractUserId(String token) {
        return Long.parseLong(extractClaim(token, Claims::getSubject));
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        try {
            final Claims claims = extractAllClaims(token);

            return claimsResolver.apply(claims);
        } catch (MalformedJwtException ex) {
            logger.error("Invalid JWT token");

            throw ex;
        } catch (ExpiredJwtException ex) {
            logger.error("Expired JWT token");

            throw ex;
        } catch (UnsupportedJwtException ex) {
            logger.error("Unsupported JWT token");

            throw ex;
        } catch (IllegalArgumentException ex) {
            logger.error("JWT claims string is empty.");

            throw ex;
        }
    }

    public String generateToken(Map<String, Object> extraClaims, Long userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + appConfig.getAuth().getTokenExpirationMSec());

        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(Long.toString(userId))
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(expiryDate)
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token, Long userId) {
        final Long id = extractUserId(token);

        return (id.equals(userId)) && !isTokenExpired(token);
    }

    public boolean isTokenExpired(String token) {
        try {

        return extractExpiration(token).before(new Date());
        } catch (ExpiredJwtException ex) {
            logger.error("Expired JWT token");
        }

        return true;
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(appConfig.getAuth().getTokenSecret());

        return Keys.hmacShaKeyFor(keyBytes);
    }
}