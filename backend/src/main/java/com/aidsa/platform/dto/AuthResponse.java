package com.aidsa.platform.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AuthResponse(
        Long id,
        String fullName,
        String email,
        String token,
        String message,
        boolean success
) {
    public static AuthResponse success(Long id, String fullName, String email, String message) {
        return new AuthResponse(id, fullName, email, null, message, true);
    }

    public static AuthResponse withToken(Long id, String fullName, String email, String token, String message) {
        return new AuthResponse(id, fullName, email, token, message, true);
    }
}
