package com.reservo.api.reservation.usecase;

import com.reservo.api.establishment.domain.exception.EstablishmentNotFoundException;
import com.reservo.api.establishment.domain.model.Establishment;
import com.reservo.api.establishment.domain.port.EstablishmentRepository;
import com.reservo.api.reservation.domain.exception.ReservationNotFoundException;
import com.reservo.api.reservation.domain.model.Reservation;
import com.reservo.api.reservation.domain.port.ReservationRepository;

import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public class ConfirmReservationUseCase {

    private final ReservationRepository reservationRepository;
    private final EstablishmentRepository establishmentRepository;

    public ConfirmReservationUseCase(
            ReservationRepository reservationRepository,
            EstablishmentRepository establishmentRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.establishmentRepository = establishmentRepository;
    }

    public record Command(
            UUID reservationId,
            UUID hostId
    ) {}

    @Transactional
    public Reservation execute(Command command) {
        Reservation reservation = reservationRepository.findById(command.reservationId())
                .orElseThrow(() -> new ReservationNotFoundException(command.reservationId()));

        Establishment establishment = establishmentRepository.findById(reservation.getEstablishmentId())
                .orElseThrow(() -> new EstablishmentNotFoundException(reservation.getEstablishmentId()));

        if (!establishment.belongsToHost(command.hostId())) {
            throw new IllegalArgumentException("Host is not the owner of the establishment for this reservation.");
        }

        reservation.confirm();
        return reservationRepository.save(reservation);
    }
}
