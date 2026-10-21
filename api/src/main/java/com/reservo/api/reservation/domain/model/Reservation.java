package com.reservo.api.reservation.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Reservation {

    private UUID id;
    private UUID customerId;
    private UUID establishmentId;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private int guestsCount;
    private BigDecimal totalPrice;
    private ReservationStatus status;
    private String cancellationReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public boolean overlapsWith(LocalDateTime start, LocalDateTime end) {
        if (this.status == ReservationStatus.CANCELLED || this.status == ReservationStatus.REJECTED) {
            return false;
        }
        return this.startDateTime.isBefore(end) && start.isBefore(this.endDateTime);
    }

    public void confirm() {
        if (this.status != ReservationStatus.PENDING) {
            throw new IllegalStateException("Only reservations in PENDING status can be confirmed.");
        }
        this.status = ReservationStatus.CONFIRMED;
        this.updatedAt = LocalDateTime.now();
    }

    public void cancel(String reason) {
        if (this.status == ReservationStatus.CANCELLED || this.status == ReservationStatus.COMPLETED) {
            throw new IllegalStateException("Cannot cancel a reservation that is already " + this.status);
        }
        this.status = ReservationStatus.CANCELLED;
        this.cancellationReason = reason;
        this.updatedAt = LocalDateTime.now();
    }

    public void reject(String reason) {
        if (this.status != ReservationStatus.PENDING) {
            throw new IllegalStateException("Only reservations in PENDING status can be rejected.");
        }
        this.status = ReservationStatus.REJECTED;
        this.cancellationReason = reason;
        this.updatedAt = LocalDateTime.now();
    }

    public static Reservation createNew(
            UUID customerId,
            UUID establishmentId,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime,
            int guestsCount,
            BigDecimal totalPrice
    ) {
        LocalDateTime now = LocalDateTime.now();
        return Reservation.builder()
                .id(UUID.randomUUID())
                .customerId(customerId)
                .establishmentId(establishmentId)
                .startDateTime(startDateTime)
                .endDateTime(endDateTime)
                .guestsCount(guestsCount)
                .totalPrice(totalPrice)
                .status(ReservationStatus.PENDING)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }
}
