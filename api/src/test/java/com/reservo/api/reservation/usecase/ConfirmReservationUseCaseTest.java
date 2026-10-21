package com.reservo.api.reservation.usecase;

import com.reservo.api.establishment.domain.model.Establishment;
import com.reservo.api.establishment.domain.port.EstablishmentRepository;
import com.reservo.api.reservation.domain.exception.ReservationNotFoundException;
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
class ConfirmReservationUseCaseTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private EstablishmentRepository establishmentRepository;

    private ConfirmReservationUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ConfirmReservationUseCase(reservationRepository, establishmentRepository);
    }

    @Test
    @DisplayName("Should confirm reservation when host is owner and reservation is pending")
    void shouldConfirmReservationSuccessfully() {
        UUID hostId = UUID.randomUUID();
        UUID establishmentId = UUID.randomUUID();
        UUID reservationId = UUID.randomUUID();

        Reservation reservation = Reservation.builder()
                .id(reservationId)
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

        Reservation result = useCase.execute(new ConfirmReservationUseCase.Command(reservationId, hostId));

        assertThat(result.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
    }

    @Test
    @DisplayName("Should throw ReservationNotFoundException when reservation does not exist")
    void shouldThrowWhenReservationDoesNotExist() {
        UUID reservationId = UUID.randomUUID();
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new ConfirmReservationUseCase.Command(reservationId, UUID.randomUUID())))
                .isInstanceOf(ReservationNotFoundException.class);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when host is not the establishment owner")
    void shouldThrowWhenHostIsNotOwner() {
        UUID hostId = UUID.randomUUID();
        UUID otherHostId = UUID.randomUUID();
        UUID establishmentId = UUID.randomUUID();
        UUID reservationId = UUID.randomUUID();

        Reservation reservation = Reservation.builder()
                .id(reservationId)
                .establishmentId(establishmentId)
                .status(ReservationStatus.PENDING)
                .build();

        Establishment establishment = Establishment.builder()
                .id(establishmentId)
                .hostId(otherHostId)
                .build();

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(establishmentRepository.findById(establishmentId)).thenReturn(Optional.of(establishment));

        assertThatThrownBy(() -> useCase.execute(new ConfirmReservationUseCase.Command(reservationId, hostId)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Host is not the owner");
    }
}
