package com.reservo.api.common.presentation;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Standardized error response payload")
public class ErrorResponse {

    @Schema(description = "Machine-readable error code", example = "VALIDATION_FAILED")
    private String code;

    @Schema(description = "Human-readable error description", example = "Request validation failed")
    private String message;

    @Schema(description = "HTTP status code", example = "400")
    private int status;

    @Schema(description = "Error timestamp", example = "2026-10-19T22:15:00Z")
    private Instant timestamp;

    @Schema(description = "Field-level validation error details when applicable")
    private Map<String, String> details;

    public static ErrorResponse of(String code, String message, int status) {
        return ErrorResponse.builder()
                .code(code)
                .message(message)
                .status(status)
                .timestamp(Instant.now())
                .build();
    }

    public static ErrorResponse of(String code, String message, int status, Map<String, String> details) {
        return ErrorResponse.builder()
                .code(code)
                .message(message)
                .status(status)
                .timestamp(Instant.now())
                .details(details)
                .build();
    }
}
