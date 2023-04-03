package com.springboot3.boilerplate.miscellaneous.redis;

import com.springboot3.boilerplate.app.exception.ResourceNotFoundException;
import com.springboot3.boilerplate.miscellaneous.util.JWTGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthTokenService {

    @Autowired
    private AuthTokenRepository authTokenRepository;

    private final Logger logger = LoggerFactory.getLogger(AuthTokenService.class);

    public String create(String accessToken, Long userId) {
        AuthToken authToken = new AuthToken();
        String referenceToken = createReferenceToken(String.valueOf(userId));

        authToken.setUserId(userId);
        authToken.setJWTToken(accessToken);
        authToken.setReferenceToken(referenceToken);
        logger.info("Creating auth token for userId: {}", userId);

        return authTokenRepository.save(authToken).getReferenceToken();
    }

    private AuthToken findByUserId(Long userId) {
        return authTokenRepository.findByUserId(userId).orElseThrow(() -> new ResourceNotFoundException("AuthToken",
                        "userId", userId));
    }

    public String getReferenceTokenByUser(Long userId) {
        AuthToken authToken = findByUserId(userId);

        return authToken.getReferenceToken();
    }

    public AuthToken getAuthToken(String referenceToken) {
        return authTokenRepository.findByReferenceToken(referenceToken)
                .orElseThrow(() -> new ResourceNotFoundException("AuthToken", "reference_token", referenceToken));
    }

    public String getJWTToken(String referenceToken) {
        Optional<AuthToken> authToken = authTokenRepository.findByReferenceToken(referenceToken);

        // Note: returning null since JWT unauthorized case is handled in token filter class
        // for accessing unauthorized resources if token isn't available
        return authToken.isPresent() ? authToken.get().getJWTToken() : null;
    }

    public void deleteAuthTokenByUserId(Long userId) {
        AuthToken authToken =findByUserId(userId);
        authTokenRepository.delete(authToken);
    }

    public void deleteAuthTokenByReferenceToken(String referenceToken) {
        Optional<AuthToken> authToken = authTokenRepository.findByReferenceToken(referenceToken);

        if (authToken.isPresent()) {
            logger.info("Deleting authToken with referenceId: {}", referenceToken);
            authTokenRepository.delete(authToken.get());
        } else {
            logger.error("Auth token not found with referenceId: {}", referenceToken);
            throw new ResourceNotFoundException("AuthToken", "reference_token", referenceToken);
        }
    }

    private String createReferenceToken(String subject) {
        // Note: we are using JWT as reference token. This JWT doesn't contain any information and is bing only used
        // as reference to original JWT access token which is never passed to client
        return JWTGenerator.generateJWTToken(subject);
    }
}
