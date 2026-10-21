package com.reservo.api.reservation.usecase;

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

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetReservationByIdUseCaseTest {

    @Mock
    private ReservationRepository reservationRepository;

    private GetReservationByIdUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetReservationByIdUseCase(reservationRepository);
    }

    @Test
    @DisplayName("Should return reservation when found by ID")
    void shouldReturnReservationWhenExists() {
        UUID reservationId = UUID.randomUUID();
        Reservation reservation = Reservation.builder()
                .id(reservationId)
                .customerId(UUID.randomUUID())
                .establishmentId(UUID.randomUUID())
                .status(ReservationStatus.PENDING)
                .totalPrice(new BigDecimal("150.00"))
                .build();

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));

        Reservation result = useCase.execute(reservationId);

        assertThat(result.getId()).isEqualTo(reservationId);
        assertThat(result.getTotalPrice()).isEqualByComparingTo("150.00");
    }

    @Test
    @DisplayName("Should throw ReservationNotFoundException when reservation does not exist")
    void shouldThrowWhenReservationNotFound() {
        UUID reservationId = UUID.randomUUID();
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(reservationId))
                .isInstanceOf(ReservationNotFoundException.class)
                .hasMessageContaining(reservationId.toString());
    }
}
