package com.reservo.api.user.infrastructure.persistence;

import com.reservo.api.user.domain.exception.InvalidCredentialsException;
import com.reservo.api.user.domain.exception.UserAlreadyExistsException;
import com.reservo.api.user.domain.model.User;
import com.reservo.api.user.domain.model.UserRole;
import com.reservo.api.user.domain.port.PasswordEncoder;
import com.reservo.api.user.domain.port.TokenService;
import com.reservo.api.user.domain.port.UserRepository;
import com.reservo.api.user.usecase.LoginUserUseCase;
import com.reservo.api.user.usecase.RegisterUserUseCase;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class UserPersistenceIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SpringDataUserRepository springDataUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private RegisterUserUseCase registerUserUseCase;

    @Autowired
    private LoginUserUseCase loginUserUseCase;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("Should securely hash password with BCrypt and persist user into PostgreSQL")
    void shouldRegisterUserAndHashPasswordInDatabase() {
        String email = "alice_" + UUID.randomUUID() + "@example.com";
        String rawPassword = "SuperSecurePassword123!";

        RegisterUserUseCase.Command command = new RegisterUserUseCase.Command(
                "Alice Wonder",
                email,
                rawPassword,
                "+1-555-4321",
                UserRole.HOST
        );

        // Act
        User registered = registerUserUseCase.execute(command);

        // Assert - Retrieve through domain port
        Optional<User> found = userRepository.findById(registered.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Alice Wonder");
        assertThat(found.get().getEmail()).isEqualTo(email);
        assertThat(found.get().getRole()).isEqualTo(UserRole.HOST);

        // Assert - Inspect directly in PostgreSQL table to ensure password is not plaintext
        UserJpaEntity entity = springDataUserRepository.findById(registered.getId()).orElseThrow();
        assertThat(entity.getPasswordHash()).isNotEqualTo(rawPassword);
        assertThat(entity.getPasswordHash()).startsWith("$2a$");
        assertThat(passwordEncoder.matches(rawPassword, entity.getPasswordHash())).isTrue();
    }

    @Test
    @DisplayName("Should authenticate user and generate valid JWT token against PostgreSQL database")
    void shouldAuthenticateUserAgainstDatabaseAndGenerateValidJwt() {
        String email = "bob_" + UUID.randomUUID() + "@example.com";
        String rawPassword = "BobPassword2026!";

        // Register user
        User registered = registerUserUseCase.execute(new RegisterUserUseCase.Command(
                "Bob Builder",
                email,
                rawPassword,
                "+1-555-9876",
                UserRole.CUSTOMER
        ));

        // Act - Login
        LoginUserUseCase.Result loginResult = loginUserUseCase.execute(new LoginUserUseCase.Command(
                email,
                rawPassword
        ));

        // Assert
        assertThat(loginResult).isNotNull();
        assertThat(loginResult.user().getId()).isEqualTo(registered.getId());
        assertThat(loginResult.token()).isNotBlank();

        // Validate JWT token claims
        Optional<TokenService.TokenClaims> claims = tokenService.validateToken(loginResult.token());
        assertThat(claims).isPresent();
        assertThat(claims.get().userId()).isEqualTo(registered.getId());
        assertThat(claims.get().email()).isEqualTo(email);
        assertThat(claims.get().role()).isEqualTo("CUSTOMER");
    }

    @Test
    @DisplayName("Should reject login when password is incorrect against PostgreSQL")
    void shouldRejectLoginWithIncorrectPasswordAgainstDatabase() {
        String email = "charlie_" + UUID.randomUUID() + "@example.com";
        String correctPassword = "CharlieSecretPassword!";

        registerUserUseCase.execute(new RegisterUserUseCase.Command(
                "Charlie Brown",
                email,
                correctPassword,
                null,
                UserRole.CUSTOMER
        ));

        // Act & Assert
        assertThatThrownBy(() -> loginUserUseCase.execute(new LoginUserUseCase.Command(
                email,
                "WrongPassword123"
        ))).isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    @DisplayName("Should reject registering duplicate email in use case and enforce DB unique constraint")
    void shouldEnforceUniqueEmailAtDatabaseLevel() {
        String email = "duplicate_" + UUID.randomUUID() + "@example.com";

        registerUserUseCase.execute(new RegisterUserUseCase.Command(
                "First User",
                email,
                "Password123!",
                null,
                UserRole.CUSTOMER
        ));

        // Use case check
        assertThatThrownBy(() -> registerUserUseCase.execute(new RegisterUserUseCase.Command(
                "Second User",
                email,
                "DifferentPassword123!",
                null,
                UserRole.CUSTOMER
        ))).isInstanceOf(UserAlreadyExistsException.class);

        // Database unique constraint check
        User duplicateDbUser = User.createNew("Third User", email, "hash", null, UserRole.CUSTOMER);
        assertThatThrownBy(() -> {
            userRepository.save(duplicateDbUser);
            entityManager.flush();
        }).satisfies(thrown -> {
            boolean isConstraint = thrown instanceof DataIntegrityViolationException
                    || thrown instanceof org.hibernate.exception.ConstraintViolationException
                    || thrown instanceof jakarta.persistence.PersistenceException;
            assertThat(isConstraint).isTrue();
        });
    }
}
