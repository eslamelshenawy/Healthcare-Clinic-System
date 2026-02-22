package com.clinic.service;

import com.clinic.dto.request.LoginRequest;
import com.clinic.dto.response.AuthResponse;
import com.clinic.security.JwtService;
import com.clinic.security.TokenBlacklist;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtService jwtService;
    @Mock
    private TokenBlacklist tokenBlacklist;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("Should login successfully and return token")
    void login_Success() {
        LoginRequest request = new LoginRequest("admin", "admin123");

        UserDetails userDetails = new User("admin", "encoded",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtService.generateToken(userDetails)).thenReturn("jwt-token");
        when(jwtService.getExpirationMs()).thenReturn(86400000L);

        AuthResponse result = authService.login(request);

        assertNotNull(result);
        assertEquals("jwt-token", result.accessToken());
        assertEquals("Bearer", result.tokenType());
        assertEquals("admin", result.username());
    }

    @Test
    @DisplayName("Should throw on invalid credentials")
    void login_InvalidCredentials() {
        LoginRequest request = new LoginRequest("admin", "wrong");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(BadCredentialsException.class,
                () -> authService.login(request));
    }

    @Test
    @DisplayName("Should blacklist token on logout")
    void logout_Success() {
        String token = "some-jwt-token";
        when(jwtService.extractUsername(token)).thenReturn("admin");

        authService.logout("Bearer " + token);

        verify(tokenBlacklist).blacklist(token);
    }

    @Test
    @DisplayName("Should handle null authorization header on logout")
    void logout_NullHeader() {
        authService.logout(null);

        verify(tokenBlacklist, never()).blacklist(any());
    }
}
