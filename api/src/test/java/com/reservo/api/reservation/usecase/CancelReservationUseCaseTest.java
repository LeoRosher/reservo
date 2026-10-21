package com.reservo.api.reservation.usecase;

import com.reservo.api.establishment.domain.model.Establishment;
import com.reservo.api.establishment.domain.port.EstablishmentRepository;
import com.reservo.api.reservation.domain.model.Reservation;
import com.reservo.api.reservation.domain.model.ReservationStatus;
import com.reservo.api.reservation.domain.port.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CancelReservationUseCaseTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private EstablishmentRepository establishmentRepository;

    private CancelReservationUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CancelReservationUseCase(reservationRepository, establishmentRepository);
    }

    @Test
    @DisplayName("Should cancel reservation successfully when requested by customer")
    void shouldCancelByCustomer() {
        UUID customerId = UUID.randomUUID();
        UUID hostId = UUID.randomUUID();
        UUID establishmentId = UUID.randomUUID();
        UUID reservationId = UUID.randomUUID();

        Reservation reservation = Reservation.builder()
                .id(reservationId)
                .customerId(customerId)
                .establishmentId(establishmentId)
                .status(ReservationStatus.PENDING)
                .build();

        Establishment establishment = Establishment.builder()
                .id(establishmentId)
                .hostId(hostId)
                .build();

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(establishmentRepository.findById(establishmentId)).thenReturn(Optional.of(establishment));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Reservation result = useCase.execute(new CancelReservationUseCase.Command(reservationId, customerId, "Plans changed"));

        assertThat(result.getStatus()).isEqualTo(ReservationStatus.CANCELLED);
        assertThat(result.getCancellationReason()).isEqualTo("Plans changed");
    }

    @Test
    @DisplayName("Should cancel reservation successfully when requested by host")
    void shouldCancelByHost() {
        UUID customerId = UUID.randomUUID();
        UUID hostId = UUID.randomUUID();
        UUID establishmentId = UUID.randomUUID();
        UUID reservationId = UUID.randomUUID();

        Reservation reservation = Reservation.builder()
                .id(reservationId)
                .customerId(customerId)
                .establishmentId(establishmentId)
                .status(ReservationStatus.CONFIRMED)
                .build();

        Establishment establishment = Establishment.builder()
                .id(establishmentId)
                .hostId(hostId)
                .build();

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(establishmentRepository.findById(establishmentId)).thenReturn(Optional.of(establishment));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Reservation result = useCase.execute(new CancelReservationUseCase.Command(reservationId, hostId, "Property maintenance"));

        assertThat(result.getStatus()).isEqualTo(ReservationStatus.CANCELLED);
        assertThat(result.getCancellationReason()).isEqualTo("Property maintenance");
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when user is neither customer nor host")
    void shouldThrowWhenUserUnauthorized() {
        UUID customerId = UUID.randomUUID();
        UUID hostId = UUID.randomUUID();
        UUID strangerId = UUID.randomUUID();
        UUID establishmentId = UUID.randomUUID();
        UUID reservationId = UUID.randomUUID();

        Reservation reservation = Reservation.builder()
                .id(reservationId)
                .customerId(customerId)
                .establishmentId(establishmentId)
                .status(ReservationStatus.CONFIRMED)
                .build();

        Establishment establishment = Establishment.builder()
                .id(establishmentId)
                .hostId(hostId)
                .build();

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(establishmentRepository.findById(establishmentId)).thenReturn(Optional.of(establishment));

        assertThatThrownBy(() -> useCase.execute(new CancelReservationUseCase.Command(reservationId, strangerId, "Unauthorized")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User is not authorized to cancel");
    }
}
