package com.reservo.api.user.presentation.dto;

import com.reservo.api.user.domain.model.UserRole;
import com.reservo.api.user.usecase.RegisterUserUseCase;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body to register a new user")
public class RegisterUserRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 150, message = "Name must not exceed 150 characters")
    @Schema(description = "Full name of the user", example = "Jane Doe")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Must be a valid email address")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    @Schema(description = "Unique email address of the user", example = "jane.doe@example.com")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    @Schema(description = "Secure password (minimum 8 characters)", example = "P@ssw0rd123!")
    private String password;

    @Size(max = 50, message = "Phone must not exceed 50 characters")
    @Schema(description = "Contact phone number", example = "+1-555-1234567")
    private String phone;

    @NotNull(message = "Role is required")
    @Schema(description = "User role in the system (CUSTOMER or HOST)", example = "HOST")
    private UserRole role;

    public RegisterUserUseCase.Command toCommand() {
        return new RegisterUserUseCase.Command(
                this.name,
                this.email,
                this.password,
                this.phone,
                this.role
        );
    }
}
