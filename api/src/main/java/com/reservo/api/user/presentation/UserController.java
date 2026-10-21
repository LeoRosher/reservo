package com.reservo.api.user.presentation;

import com.reservo.api.common.presentation.ErrorResponse;
import com.reservo.api.user.domain.model.User;
import com.reservo.api.user.presentation.dto.UserResponse;
import com.reservo.api.user.usecase.GetUserByIdUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users", description = "Endpoints for user profile retrieval")
public class UserController {

    private final GetUserByIdUseCase getUserByIdUseCase;

    public UserController(GetUserByIdUseCase getUserByIdUseCase) {
        this.getUserByIdUseCase = getUserByIdUseCase;
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get user by ID",
            description = "Retrieves public profile information of a user by ID."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID id) {
        User user = getUserByIdUseCase.execute(id);
        return ResponseEntity.ok(UserResponse.from(user));
    }
}
