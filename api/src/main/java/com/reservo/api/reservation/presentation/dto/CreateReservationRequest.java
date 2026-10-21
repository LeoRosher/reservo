package com.reservo.api.reservation.presentation.dto;

import com.reservo.api.reservation.usecase.CreateReservationUseCase;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body to create a new reservation")
public class CreateReservationRequest {

    @NotNull(message = "Customer ID is required")
    @Schema(description = "ID of the customer making the reservation", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID customerId;

    @NotNull(message = "Establishment ID is required")
    @Schema(description = "ID of the establishment being reserved", example = "550e8400-e29b-41d4-a716-446655440001")
    private UUID establishmentId;

    @NotNull(message = "Start date time is required")
    @FutureOrPresent(message = "Start date time cannot be in the past")
    @Schema(description = "Reservation start date and time", example = "2026-11-01T10:00:00")
    private LocalDateTime startDateTime;

    @NotNull(message = "End date time is required")
    @Schema(description = "Reservation end date and time", example = "2026-11-01T14:00:00")
    private LocalDateTime endDateTime;

    @Min(value = 1, message = "Guests count must be at least 1")
    @Schema(description = "Number of guests attending", example = "2")
    private int guestsCount;

    public CreateReservationUseCase.Command toCommand() {
        return new CreateReservationUseCase.Command(
                this.customerId,
                this.establishmentId,
                this.startDateTime,
                this.endDateTime,
                this.guestsCount
        );
    }
}
