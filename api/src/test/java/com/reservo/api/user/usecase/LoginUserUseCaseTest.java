package com.reservo.api.user.usecase;

import com.reservo.api.user.domain.exception.InvalidCredentialsException;
import com.reservo.api.user.domain.model.User;
import com.reservo.api.user.domain.model.UserRole;
import com.reservo.api.user.domain.port.PasswordEncoder;
import com.reservo.api.user.domain.port.TokenService;
import com.reservo.api.user.domain.port.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginUserUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenService tokenService;

    private LoginUserUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new LoginUserUseCase(userRepository, passwordEncoder, tokenService);
    }

    @Test
    @DisplayName("Should successfully authenticate user with correct credentials and return token")
    void shouldAuthenticateUserSuccessfullyAndReturnToken() {
        // Arrange
        UUID userId = UUID.randomUUID();
        String email = "jane@example.com";
        String rawPassword = "CorrectPassword123";
        String passwordHash = "$2a$10$hashedPasswordSample1234567890";
        String expectedToken = "header.payload.signature";

        User user = User.builder()
                .id(userId)
                .name("Jane Doe")
                .email(email)
                .passwordHash(passwordHash)
                .role(UserRole.HOST)
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(rawPassword, passwordHash)).thenReturn(true);
        when(tokenService.generateToken(user)).thenReturn(expectedToken);

        LoginUserUseCase.Command command = new LoginUserUseCase.Command(email, rawPassword);

        // Act
        LoginUserUseCase.Result result = useCase.execute(command);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.token()).isEqualTo(expectedToken);
        assertThat(result.user()).isEqualTo(user);
        verify(tokenService).generateToken(user);
    }

    @Test
    @DisplayName("Should throw InvalidCredentialsException when email does not exist")
    void shouldThrowInvalidCredentialsExceptionWhenEmailNotFound() {
        // Arrange
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        LoginUserUseCase.Command command = new LoginUserUseCase.Command("unknown@example.com", "anyPassword");

        // Act & Assert
        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(passwordEncoder, never()).matches(any(), any());
        verify(tokenService, never()).generateToken(any());
    }

    @Test
    @DisplayName("Should throw InvalidCredentialsException when password does not match")
    void shouldThrowInvalidCredentialsExceptionWhenPasswordDoesNotMatch() {
        // Arrange
        String email = "jane@example.com";
        String wrongPassword = "WrongPassword";
        String passwordHash = "$2a$10$hashedPasswordSample1234567890";

        User user = User.builder()
                .id(UUID.randomUUID())
                .name("Jane Doe")
                .email(email)
                .passwordHash(passwordHash)
                .role(UserRole.CUSTOMER)
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(wrongPassword, passwordHash)).thenReturn(false);

        LoginUserUseCase.Command command = new LoginUserUseCase.Command(email, wrongPassword);

        // Act & Assert
        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(tokenService, never()).generateToken(any());
    }
}
