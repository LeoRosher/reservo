package com.reservo.api.reservation.presentation.dto;

import com.reservo.api.reservation.domain.model.Reservation;
import com.reservo.api.reservation.domain.model.ReservationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Reservation response details")
public class ReservationResponse {

    @Schema(description = "Unique identifier of the reservation", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "ID of the customer who made the reservation", example = "550e8400-e29b-41d4-a716-446655440001")
    private UUID customerId;

    @Schema(description = "ID of the reserved establishment", example = "550e8400-e29b-41d4-a716-446655440002")
    private UUID establishmentId;

    @Schema(description = "Reservation start date and time", example = "2026-11-01T10:00:00")
    private LocalDateTime startDateTime;

    @Schema(description = "Reservation end date and time", example = "2026-11-01T14:00:00")
    private LocalDateTime endDateTime;

    @Schema(description = "Number of guests", example = "2")
    private int guestsCount;

    @Schema(description = "Total calculated price", example = "150.00")
    private BigDecimal totalPrice;

    @Schema(description = "Current reservation status", example = "PENDING")
    private ReservationStatus status;

    @Schema(description = "Cancellation reason if applicable", example = "Host unavailable")
    private String cancellationReason;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;

    public static ReservationResponse from(Reservation reservation) {
        if (reservation == null) {
            return null;
        }
        return ReservationResponse.builder()
                .id(reservation.getId())
                .customerId(reservation.getCustomerId())
                .establishmentId(reservation.getEstablishmentId())
                .startDateTime(reservation.getStartDateTime())
                .endDateTime(reservation.getEndDateTime())
                .guestsCount(reservation.getGuestsCount())
                .totalPrice(reservation.getTotalPrice())
                .status(reservation.getStatus())
                .cancellationReason(reservation.getCancellationReason())
                .createdAt(reservation.getCreatedAt())
                .updatedAt(reservation.getUpdatedAt())
                .build();
    }
}
