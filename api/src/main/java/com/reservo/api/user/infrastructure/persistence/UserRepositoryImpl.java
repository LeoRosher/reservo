package com.reservo.api.user.infrastructure.persistence;

import com.reservo.api.user.domain.model.User;
import com.reservo.api.user.domain.port.UserRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

@Component
public class UserRepositoryImpl implements UserRepository {

    private final SpringDataUserRepository springDataUserRepository;

    public UserRepositoryImpl(SpringDataUserRepository springDataUserRepository) {
        this.springDataUserRepository = springDataUserRepository;
    }

    @Override
    public User save(User user) {
        UserJpaEntity entity = toEntity(user);
        UserJpaEntity saved = springDataUserRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return springDataUserRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return springDataUserRepository.findByEmail(email).map(this::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return springDataUserRepository.existsByEmail(email);
    }

    private UserJpaEntity toEntity(User domain) {
        Instant createdAtInstant = domain.getCreatedAt() != null
                ? domain.getCreatedAt().toInstant(ZoneOffset.UTC)
                : Instant.now();
        Instant updatedAtInstant = domain.getUpdatedAt() != null
                ? domain.getUpdatedAt().toInstant(ZoneOffset.UTC)
                : Instant.now();

        return UserJpaEntity.builder()
                .id(domain.getId())
                .name(domain.getName())
                .email(domain.getEmail())
                .passwordHash(domain.getPasswordHash())
                .phone(domain.getPhone())
                .role(domain.getRole())
                .createdAt(createdAtInstant)
                .updatedAt(updatedAtInstant)
                .build();
    }

    private User toDomain(UserJpaEntity entity) {
        LocalDateTime createdAt = entity.getCreatedAt() != null
                ? LocalDateTime.ofInstant(entity.getCreatedAt(), ZoneOffset.UTC)
                : null;
        LocalDateTime updatedAt = entity.getUpdatedAt() != null
                ? LocalDateTime.ofInstant(entity.getUpdatedAt(), ZoneOffset.UTC)
                : null;

        return User.builder()
                .id(entity.getId())
                .name(entity.getName())
                .email(entity.getEmail())
                .passwordHash(entity.getPasswordHash())
                .phone(entity.getPhone())
                .role(entity.getRole())
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }
}
