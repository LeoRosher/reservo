package com.reservo.api.reservation.usecase;

import com.reservo.api.reservation.domain.exception.ReservationNotFoundException;
import com.reservo.api.reservation.domain.model.Reservation;
import com.reservo.api.reservation.domain.port.ReservationRepository;

import java.util.UUID;

public class GetReservationByIdUseCase {

    private final ReservationRepository reservationRepository;

    public GetReservationByIdUseCase(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public Reservation execute(UUID id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new ReservationNotFoundException(id));
    }
}
