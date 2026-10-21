package com.reservo.api.reservation.domain.port;

import com.reservo.api.reservation.domain.model.Reservation;
import com.reservo.api.reservation.domain.model.ReservationStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReservationRepository {
    Reservation save(Reservation reservation);
    Optional<Reservation> findById(UUID id);
    List<Reservation> findByCustomerId(UUID customerId);
    List<Reservation> findByEstablishmentId(UUID establishmentId);
    List<Reservation> findOverlappingReservations(
            UUID establishmentId,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime,
            List<ReservationStatus> activeStatuses
    );
}
