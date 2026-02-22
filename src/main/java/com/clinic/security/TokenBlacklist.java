package com.clinic.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class TokenBlacklist {

    private final Cache<String, Boolean> blacklistedTokens;

    public TokenBlacklist(@Value("${app.jwt.expiration-ms}") long jwtExpirationMs) {
        this.blacklistedTokens = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMillis(jwtExpirationMs))
                .maximumSize(10_000)
                .build();
    }

    public void blacklist(String token) {
        blacklistedTokens.put(token, Boolean.TRUE);
    }

    public boolean isBlacklisted(String token) {
        return blacklistedTokens.getIfPresent(token) != null;
    }
}
