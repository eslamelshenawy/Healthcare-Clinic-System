package com.clinic.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() throws Exception {
        jwtService = new JwtService();

        Field secretField = JwtService.class.getDeclaredField("jwtSecret");
        secretField.setAccessible(true);
        secretField.set(jwtService,
                "dGVzdC1zZWNyZXQta2V5LWZvci11bml0LXRlc3RzLW9ubHktMTIzNDU2Nzg5MA==");

        Field expirationField = JwtService.class.getDeclaredField("jwtExpirationMs");
        expirationField.setAccessible(true);
        expirationField.set(jwtService, 3600000L);

        jwtService.init();
    }

    @Test
    @DisplayName("Should generate and validate JWT token")
    void generateAndValidateToken() {
        UserDetails userDetails = User.builder()
                .username("testuser")
                .password("password")
                .roles("ADMIN")
                .build();

        String token = jwtService.generateToken(userDetails);

        assertNotNull(token);
        assertEquals("testuser", jwtService.extractUsername(token));
        assertTrue(jwtService.isTokenValid(token, userDetails));
    }

    @Test
    @DisplayName("Should extract correct username from token")
    void extractUsername() {
        UserDetails userDetails = User.builder()
                .username("admin")
                .password("password")
                .roles("ADMIN")
                .build();

        String token = jwtService.generateToken(userDetails);

        assertEquals("admin", jwtService.extractUsername(token));
    }

    @Test
    @DisplayName("Should return false for token with wrong username")
    void validateToken_WrongUser() {
        UserDetails creator = User.builder()
                .username("user1").password("p").roles("ADMIN").build();
        UserDetails other = User.builder()
                .username("user2").password("p").roles("ADMIN").build();

        String token = jwtService.generateToken(creator);

        assertFalse(jwtService.isTokenValid(token, other));
    }

    @Test
    @DisplayName("Should extract expiration date from token")
    void extractExpiration() {
        UserDetails userDetails = User.builder()
                .username("testuser")
                .password("password")
                .roles("ADMIN")
                .build();

        String token = jwtService.generateToken(userDetails);

        assertNotNull(jwtService.extractExpiration(token));
    }
}
