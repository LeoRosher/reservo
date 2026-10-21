package com.reservo.api.reservation.domain.service;

import com.reservo.api.establishment.domain.model.Establishment;
import com.reservo.api.reservation.domain.exception.ExceededCapacityException;
import com.reservo.api.reservation.domain.exception.InvalidReservationDatesException;
import com.reservo.api.reservation.domain.exception.ReservationConflictException;
import com.reservo.api.reservation.domain.model.Reservation;
import com.reservo.api.reservation.domain.model.ReservationStatus;
import com.reservo.api.reservation.domain.port.ReservationRepository;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class ReservationDomainService {

    private final ReservationRepository reservationRepository;

    public ReservationDomainService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public void validateReservationDates(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        if (startDateTime == null || endDateTime == null) {
            throw new InvalidReservationDatesException("Reservation dates cannot be null.");
        }
        if (startDateTime.isBefore(LocalDateTime.now())) {
            throw new InvalidReservationDatesException("Start date time cannot be in the past.");
        }
        if (!endDateTime.isAfter(startDateTime)) {
            throw new InvalidReservationDatesException("End date time must be after start date time.");
        }
    }

    public void validateCapacity(Establishment establishment, int guestsCount) {
        if (guestsCount <= 0) {
            throw new IllegalArgumentException("Guest count must be greater than zero.");
        }
        if (guestsCount > establishment.getCapacity()) {
            throw new ExceededCapacityException(guestsCount, establishment.getCapacity());
        }
    }

    public void ensureNoOverlap(Establishment establishment, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        List<ReservationStatus> activeStatuses = List.of(ReservationStatus.PENDING, ReservationStatus.CONFIRMED);
        List<Reservation> overlapping = reservationRepository.findOverlappingReservations(
                establishment.getId(),
                startDateTime,
                endDateTime,
                activeStatuses
        );

        if (!overlapping.isEmpty()) {
            throw new ReservationConflictException(
                    "Establishment is not available for the selected dates due to an overlapping reservation."
            );
        }
    }

    public BigDecimal calculateTotalPrice(Establishment establishment, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        BigDecimal unitPrice = establishment.getPricePerUnit();
        return switch (establishment.getUnitType()) {
            case HOUR, SESSION, SLOT -> {
                long hours = Duration.between(startDateTime, endDateTime).toHours();
                if (hours == 0) hours = 1; // Minimum 1 hour
                yield unitPrice.multiply(BigDecimal.valueOf(hours));
            }
            case DAY, NIGHT -> {
                long days = ChronoUnit.DAYS.between(startDateTime.toLocalDate(), endDateTime.toLocalDate());
                if (days == 0) days = 1; // Minimum 1 day
                yield unitPrice.multiply(BigDecimal.valueOf(days));
            }
        };
    }
}
