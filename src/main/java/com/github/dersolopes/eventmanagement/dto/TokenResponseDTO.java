package com.github.dersolopes.eventmanagement.dto;

import java.util.UUID;

public record TokenResponseDTO(
        String token,
        String type,
        UUID userId,
        String email,
        String role
) {
    // Construtor auxiliar mantendo o tipo "Bearer" por padrão
    public TokenResponseDTO(String token, UUID userId, String email, String role) {
        this(token, "Bearer", userId, email, role);
    }
}