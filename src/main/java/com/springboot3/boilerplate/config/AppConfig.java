package com.springboot3.boilerplate.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@ConfigurationProperties(prefix = "app")
public class AppConfig {
    private final Auth auth = new Auth();
    private final OAuth2 oauth2 = new OAuth2();
    private String subscriptionSecret;

    public static class Auth {
        private String tokenSecret;

        private long tokenExpirationMSec;

        public String getTokenSecret() {
            return tokenSecret;
        }

        public void setTokenSecret(String tokenSecret) {
            this.tokenSecret = tokenSecret;
        }

        public long getTokenExpirationMSec() {
            return tokenExpirationMSec;
        }

        public void setTokenExpirationMSec(long tokenExpirationMSec) {
            this.tokenExpirationMSec = tokenExpirationMSec;
        }
    }

    public static final class OAuth2 {
        private List<String> authorizedRedirectUris = new ArrayList<>();

        public List<String> getAuthorizedRedirectUris() {
            return authorizedRedirectUris;
        }

        public OAuth2 authorizedRedirectUris(List<String> authorizedRedirectUris) {
            this.authorizedRedirectUris = authorizedRedirectUris;
            return this;
        }
    }

    public Auth getAuth() {
        return auth;
    }

    public OAuth2 getOauth2() {
        return oauth2;
    }

    public String getSubscriptionSecret() {
        return subscriptionSecret;
    }

    public void setSubscriptionSecret(String subscriptionSecret) {
        this.subscriptionSecret = subscriptionSecret;
    }
}