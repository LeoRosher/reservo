package com.reservo.api.user.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Authentication response containing JWT token")
public class LoginResponse {

    @Schema(description = "JWT bearer access token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;

    @Schema(description = "Token type", example = "Bearer")
    @Builder.Default
    private String tokenType = "Bearer";

    @Schema(description = "Authenticated user profile")
    private UserResponse user;

    public static LoginResponse of(String token, UserResponse user) {
        return LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .user(user)
                .build();
    }
}
