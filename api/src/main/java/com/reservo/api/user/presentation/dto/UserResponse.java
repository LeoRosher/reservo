package com.reservo.api.user.presentation.dto;

import com.reservo.api.user.domain.model.User;
import com.reservo.api.user.domain.model.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User profile response details")
public class UserResponse {

    @Schema(description = "Unique user identifier", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "Full name of the user", example = "Jane Doe")
    private String name;

    @Schema(description = "Email address", example = "jane.doe@example.com")
    private String email;

    @Schema(description = "Contact phone number", example = "+1-555-1234567")
    private String phone;

    @Schema(description = "Role of the user (CUSTOMER or HOST)", example = "HOST")
    private UserRole role;

    @Schema(description = "Account creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Account update timestamp")
    private LocalDateTime updatedAt;

    public static UserResponse from(User user) {
        if (user == null) {
            return null;
        }
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
