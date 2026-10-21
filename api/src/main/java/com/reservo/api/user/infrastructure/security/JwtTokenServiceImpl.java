package com.reservo.api.user.infrastructure.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.reservo.api.user.domain.model.User;
import com.reservo.api.user.domain.port.TokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
public class JwtTokenServiceImpl implements TokenService {

    private final Algorithm algorithm;
    private final String issuer;
    private final long expirationMs;
    private final JWTVerifier verifier;

    public JwtTokenServiceImpl(
            @Value("${jwt.secret:default-secret-key-that-is-at-least-256-bits-long-for-hmac-sha256}") String secret,
            @Value("${jwt.issuer:reservo-api}") String issuer,
            @Value("${jwt.expiration-ms:86400000}") long expirationMs
    ) {
        this.algorithm = Algorithm.HMAC256(secret);
        this.issuer = issuer;
        this.expirationMs = expirationMs;
        this.verifier = JWT.require(this.algorithm)
                .withIssuer(this.issuer)
                .build();
    }

    @Override
    public String generateToken(User user) {
        Date now = new Date();
        Date expiresAt = new Date(now.getTime() + expirationMs);

        return JWT.create()
                .withIssuer(issuer)
                .withSubject(user.getId().toString())
                .withClaim("email", user.getEmail())
                .withClaim("name", user.getName())
                .withClaim("role", user.getRole().name())
                .withIssuedAt(now)
                .withExpiresAt(expiresAt)
                .sign(algorithm);
    }

    @Override
    public Optional<TokenClaims> validateToken(String token) {
        try {
            DecodedJWT decodedJWT = verifier.verify(token);
            UUID userId = UUID.fromString(decodedJWT.getSubject());
            String email = decodedJWT.getClaim("email").asString();
            String role = decodedJWT.getClaim("role").asString();
            return Optional.of(new TokenClaims(userId, email, role));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
