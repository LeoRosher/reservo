package com.reservo.api.user.domain.port;

import com.reservo.api.user.domain.model.User;

import java.util.Optional;
import java.util.UUID;

public interface TokenService {
    String generateToken(User user);
    Optional<TokenClaims> validateToken(String token);

    record TokenClaims(
            UUID userId,
            String email,
            String role
    ) {}
}
