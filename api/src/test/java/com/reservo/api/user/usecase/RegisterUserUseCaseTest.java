package com.reservo.api.user.usecase;

import com.reservo.api.user.domain.exception.UserAlreadyExistsException;
import com.reservo.api.user.domain.model.User;
import com.reservo.api.user.domain.model.UserRole;
import com.reservo.api.user.domain.port.PasswordEncoder;
import com.reservo.api.user.domain.port.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterUserUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private RegisterUserUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new RegisterUserUseCase(userRepository, passwordEncoder);
    }

    @Test
    @DisplayName("Should successfully hash password and register a new user")
    void shouldRegisterUserSuccessfullyWithHashedPassword() {
        // Arrange
        String rawPassword = "SecurePassword123!";
        String encodedHash = "$2a$10$hashedPasswordSample1234567890";

        when(userRepository.existsByEmail("user@example.com")).thenReturn(false);
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedHash);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RegisterUserUseCase.Command command = new RegisterUserUseCase.Command(
                "Jane Doe",
                "user@example.com",
                rawPassword,
                "+1-555-123456",
                UserRole.HOST
        );

        // Act
        User result = useCase.execute(command);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getName()).isEqualTo("Jane Doe");
        assertThat(result.getEmail()).isEqualTo("user@example.com");
        assertThat(result.getPasswordHash()).isEqualTo(encodedHash);
        assertThat(result.getPhone()).isEqualTo("+1-555-123456");
        assertThat(result.getRole()).isEqualTo(UserRole.HOST);
        assertThat(result.isHost()).isTrue();
        assertThat(result.getCreatedAt()).isNotNull();

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getPasswordHash()).isEqualTo(encodedHash);
    }

    @Test
    @DisplayName("Should throw UserAlreadyExistsException when email is already registered")
    void shouldThrowUserAlreadyExistsExceptionWhenEmailExists() {
        // Arrange
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        RegisterUserUseCase.Command command = new RegisterUserUseCase.Command(
                "Existing User",
                "existing@example.com",
                "password123",
                null,
                UserRole.CUSTOMER
        );

        // Act & Assert
        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("existing@example.com");

        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
    }
}
