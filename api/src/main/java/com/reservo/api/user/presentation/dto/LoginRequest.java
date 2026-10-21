package com.reservo.api.user.presentation.dto;

import com.reservo.api.user.usecase.LoginUserUseCase;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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
@Schema(description = "Request body for user login")
public class LoginRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Must be a valid email address")
    @Schema(description = "Registered email address", example = "jane.doe@example.com")
    private String email;

    @NotBlank(message = "Password is required")
    @Schema(description = "User password", example = "P@ssw0rd123!")
    private String password;

    public LoginUserUseCase.Command toCommand() {
        return new LoginUserUseCase.Command(this.email, this.password);
    }
}
