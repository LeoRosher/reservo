package com.reservo.api.reservation.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body to confirm a pending reservation")
public class ConfirmReservationRequest {

    @NotNull(message = "Host ID is required")
    @Schema(description = "ID of the host confirming the reservation", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID hostId;
}
