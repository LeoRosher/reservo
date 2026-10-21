package com.reservo.api.reservation.infrastructure.config;

import com.reservo.api.establishment.domain.port.EstablishmentRepository;
import com.reservo.api.reservation.domain.port.ReservationRepository;
import com.reservo.api.reservation.domain.service.ReservationDomainService;
import com.reservo.api.reservation.usecase.CancelReservationUseCase;
import com.reservo.api.reservation.usecase.ConfirmReservationUseCase;
import com.reservo.api.reservation.usecase.CreateReservationUseCase;
import com.reservo.api.reservation.usecase.GetReservationByIdUseCase;
import com.reservo.api.reservation.usecase.ListUserReservationsUseCase;
import com.reservo.api.user.domain.port.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ReservationConfig {

    @Bean
    public ReservationDomainService reservationDomainService(ReservationRepository reservationRepository) {
        return new ReservationDomainService(reservationRepository);
    }

    @Bean
    public CreateReservationUseCase createReservationUseCase(
            ReservationRepository reservationRepository,
            EstablishmentRepository establishmentRepository,
            UserRepository userRepository,
            ReservationDomainService reservationDomainService
    ) {
        return new CreateReservationUseCase(
                reservationRepository,
                establishmentRepository,
                userRepository,
                reservationDomainService
        );
    }

    @Bean
    public ConfirmReservationUseCase confirmReservationUseCase(
            ReservationRepository reservationRepository,
            EstablishmentRepository establishmentRepository
    ) {
        return new ConfirmReservationUseCase(reservationRepository, establishmentRepository);
    }

    @Bean
    public CancelReservationUseCase cancelReservationUseCase(
            ReservationRepository reservationRepository,
            EstablishmentRepository establishmentRepository
    ) {
        return new CancelReservationUseCase(reservationRepository, establishmentRepository);
    }

    @Bean
    public ListUserReservationsUseCase listUserReservationsUseCase(ReservationRepository reservationRepository) {
        return new ListUserReservationsUseCase(reservationRepository);
    }

    @Bean
    public GetReservationByIdUseCase getReservationByIdUseCase(ReservationRepository reservationRepository) {
        return new GetReservationByIdUseCase(reservationRepository);
    }
}
