package com.reservo.api.reservation.usecase;

import com.reservo.api.establishment.domain.exception.EstablishmentNotFoundException;
import com.reservo.api.establishment.domain.model.Establishment;
import com.reservo.api.establishment.domain.port.EstablishmentRepository;
import com.reservo.api.reservation.domain.model.Reservation;
import com.reservo.api.reservation.domain.port.ReservationRepository;
import com.reservo.api.reservation.domain.service.ReservationDomainService;
import com.reservo.api.user.domain.exception.UserNotFoundException;
import com.reservo.api.user.domain.model.User;
import com.reservo.api.user.domain.port.UserRepository;

import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class CreateReservationUseCase {

    private final ReservationRepository reservationRepository;
    private final EstablishmentRepository establishmentRepository;
    private final UserRepository userRepository;
    private final ReservationDomainService reservationDomainService;

    public CreateReservationUseCase(
            ReservationRepository reservationRepository,
            EstablishmentRepository establishmentRepository,
            UserRepository userRepository,
            ReservationDomainService reservationDomainService
    ) {
        this.reservationRepository = reservationRepository;
        this.establishmentRepository = establishmentRepository;
        this.userRepository = userRepository;
        this.reservationDomainService = reservationDomainService;
    }

    public record Command(
            UUID customerId,
            UUID establishmentId,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime,
            int guestsCount
    ) {}

    @Transactional
    public Reservation execute(Command command) {
        // 1. Validate customer exists
        User customer = userRepository.findById(command.customerId())
                .orElseThrow(() -> new UserNotFoundException(command.customerId()));

        if (!customer.isCustomer()) {
            throw new IllegalArgumentException("Only users with CUSTOMER role can place reservations.");
        }

        // 2. Validate establishment exists and is active (acquire pessimistic lock to prevent concurrent double-booking)
        Establishment establishment = establishmentRepository.findByIdWithLock(command.establishmentId())
                .orElseThrow(() -> new EstablishmentNotFoundException(command.establishmentId()));

        if (!establishment.isActive()) {
            throw new IllegalStateException("Establishment is currently inactive for reservations.");
        }

        // 3. Reglas de negocio del dominio
        reservationDomainService.validateReservationDates(command.startDateTime(), command.endDateTime());
        reservationDomainService.validateCapacity(establishment, command.guestsCount());
        reservationDomainService.ensureNoOverlap(establishment, command.startDateTime(), command.endDateTime());

        // 4. Calcular precio total
        BigDecimal totalPrice = reservationDomainService.calculateTotalPrice(
                establishment,
                command.startDateTime(),
                command.endDateTime()
        );

        // 5. Crear la reserva en estado PENDING y persistir
        Reservation reservation = Reservation.createNew(
                command.customerId(),
                command.establishmentId(),
                command.startDateTime(),
                command.endDateTime(),
                command.guestsCount(),
                totalPrice
        );

        return reservationRepository.save(reservation);
    }
}
