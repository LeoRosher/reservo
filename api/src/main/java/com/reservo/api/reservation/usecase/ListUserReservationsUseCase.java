package com.reservo.api.reservation.usecase;

import com.reservo.api.reservation.domain.model.Reservation;
import com.reservo.api.reservation.domain.port.ReservationRepository;

import java.util.List;
import java.util.UUID;

public class ListUserReservationsUseCase {

    private final ReservationRepository reservationRepository;

    public ListUserReservationsUseCase(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public List<Reservation> executeByCustomer(UUID customerId) {
        return reservationRepository.findByCustomerId(customerId);
    }

    public List<Reservation> executeByEstablishment(UUID establishmentId) {
        return reservationRepository.findByEstablishmentId(establishmentId);
    }
}
