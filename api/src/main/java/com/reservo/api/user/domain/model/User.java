package com.reservo.api.user.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    private UUID id;
    private String name;
    private String email;
    private String passwordHash;
    private String phone;
    private UserRole role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public boolean isHost() {
        return UserRole.HOST.equals(this.role);
    }

    public boolean isCustomer() {
        return UserRole.CUSTOMER.equals(this.role);
    }

    public static User createNew(String name, String email, String passwordHash, String phone, UserRole role) {
        LocalDateTime now = LocalDateTime.now();
        return User.builder()
                .id(UUID.randomUUID())
                .name(name)
                .email(email)
                .passwordHash(passwordHash)
                .phone(phone)
                .role(role)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }
}
