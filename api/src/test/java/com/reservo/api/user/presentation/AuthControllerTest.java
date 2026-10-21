package com.reservo.api.user.presentation;

import com.reservo.api.common.presentation.GlobalExceptionHandler;
import com.reservo.api.user.domain.exception.InvalidCredentialsException;
import com.reservo.api.user.domain.exception.UserAlreadyExistsException;
import com.reservo.api.user.domain.model.User;
import com.reservo.api.user.domain.model.UserRole;
import com.reservo.api.user.presentation.dto.LoginRequest;
import com.reservo.api.user.presentation.dto.RegisterUserRequest;
import com.reservo.api.user.usecase.LoginUserUseCase;
import com.reservo.api.user.usecase.RegisterUserUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(GlobalExceptionHandler.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RegisterUserUseCase registerUserUseCase;

    @MockitoBean
    private LoginUserUseCase loginUserUseCase;

    @Test
    @DisplayName("POST /api/v1/auth/register should return 201 Created when request is valid")
    void shouldReturn201CreatedWhenRegisterPayloadIsValid() throws Exception {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .name("Jane Doe")
                .email("jane@example.com")
                .passwordHash("$2a$10$hashedPassword")
                .phone("+1-555-123456")
                .role(UserRole.HOST)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(registerUserUseCase.execute(any())).thenReturn(user);

        RegisterUserRequest request = RegisterUserRequest.builder()
                .name("Jane Doe")
                .email("jane@example.com")
                .password("Password123!")
                .phone("+1-555-123456")
                .role(UserRole.HOST)
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/v1/users/" + userId)))
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.name").value("Jane Doe"))
                .andExpect(jsonPath("$.email").value("jane@example.com"))
                .andExpect(jsonPath("$.role").value("HOST"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist()) // Never expose password hash!
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    @DisplayName("POST /api/v1/auth/register should return 400 Bad Request when required fields are missing or invalid")
    void shouldReturn400BadRequestWhenRegisterPayloadIsInvalid() throws Exception {
        RegisterUserRequest invalidRequest = RegisterUserRequest.builder()
                .name("") // invalid: blank
                .email("not-an-email") // invalid: not email
                .password("short") // invalid: < 8 chars
                .role(null) // invalid: null
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.details.name").exists())
                .andExpect(jsonPath("$.details.email").exists())
                .andExpect(jsonPath("$.details.password").exists())
                .andExpect(jsonPath("$.details.role").exists());
    }

    @Test
    @DisplayName("POST /api/v1/auth/register should return 409 Conflict when user already exists")
    void shouldReturn409ConflictWhenUserAlreadyExists() throws Exception {
        when(registerUserUseCase.execute(any()))
                .thenThrow(new UserAlreadyExistsException("existing@example.com"));

        RegisterUserRequest request = RegisterUserRequest.builder()
                .name("Jane Doe")
                .email("existing@example.com")
                .password("Password123!")
                .role(UserRole.CUSTOMER)
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("USER_ALREADY_EXISTS"))
                .andExpect(jsonPath("$.message").value(containsString("existing@example.com")));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login should return 200 OK and Bearer token when credentials are valid")
    void shouldReturn200OkWhenLoginCredentialsAreValid() throws Exception {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .name("Jane Doe")
                .email("jane@example.com")
                .role(UserRole.HOST)
                .build();

        String token = "jwt.token.string";
        when(loginUserUseCase.execute(any())).thenReturn(new LoginUserUseCase.Result(token, user));

        LoginRequest request = LoginRequest.builder()
                .email("jane@example.com")
                .password("Password123!")
                .build();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(token))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.user.id").value(userId.toString()))
                .andExpect(jsonPath("$.user.email").value("jane@example.com"))
                .andExpect(jsonPath("$.user.role").value("HOST"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login should return 401 Unauthorized when credentials are wrong")
    void shouldReturn401UnauthorizedWhenCredentialsAreInvalid() throws Exception {
        when(loginUserUseCase.execute(any())).thenThrow(new InvalidCredentialsException());

        LoginRequest request = LoginRequest.builder()
                .email("jane@example.com")
                .password("WrongPassword123!")
                .build();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"))
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }
}
