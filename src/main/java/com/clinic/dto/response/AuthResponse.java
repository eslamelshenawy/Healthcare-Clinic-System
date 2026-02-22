package com.clinic.dto.response;

public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        String username,
        String role
) {
}
