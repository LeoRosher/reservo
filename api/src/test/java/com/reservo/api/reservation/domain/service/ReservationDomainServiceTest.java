package com.reservo.api.reservation.domain.service;

import com.reservo.api.establishment.domain.model.Establishment;
import com.reservo.api.establishment.domain.model.ReservationUnitType;
import com.reservo.api.reservation.domain.exception.ExceededCapacityException;
import com.reservo.api.reservation.domain.exception.InvalidReservationDatesException;
import com.reservo.api.reservation.domain.exception.ReservationConflictException;
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
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationDomainServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    private ReservationDomainService domainService;

    @BeforeEach
    void setUp() {
        domainService = new ReservationDomainService(reservationRepository);
    }

    @Test
    @DisplayName("Should pass date validation when dates are in future and end is after start")
    void shouldPassValidDates() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.plusHours(4);

        domainService.validateReservationDates(start, end);
    }

    @Test
    @DisplayName("Should throw InvalidReservationDatesException when dates are null")
    void shouldThrowWhenDatesAreNull() {
        assertThatThrownBy(() -> domainService.validateReservationDates(null, LocalDateTime.now().plusDays(1)))
                .isInstanceOf(InvalidReservationDatesException.class)
                .hasMessageContaining("cannot be null");

        assertThatThrownBy(() -> domainService.validateReservationDates(LocalDateTime.now().plusDays(1), null))
                .isInstanceOf(InvalidReservationDatesException.class)
                .hasMessageContaining("cannot be null");
    }

    @Test
    @DisplayName("Should throw InvalidReservationDatesException when start date is in past")
    void shouldThrowWhenStartDateIsInPast() {
        LocalDateTime past = LocalDateTime.now().minusHours(1);
        LocalDateTime future = LocalDateTime.now().plusHours(2);

        assertThatThrownBy(() -> domainService.validateReservationDates(past, future))
                .isInstanceOf(InvalidReservationDatesException.class)
                .hasMessageContaining("cannot be in the past");
    }

    @Test
    @DisplayName("Should throw InvalidReservationDatesException when end date is before or equal to start date")
    void shouldThrowWhenEndDateIsBeforeOrEqualToStart() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.minusHours(1);

        assertThatThrownBy(() -> domainService.validateReservationDates(start, end))
                .isInstanceOf(InvalidReservationDatesException.class)
                .hasMessageContaining("must be after start date time");

        assertThatThrownBy(() -> domainService.validateReservationDates(start, start))
                .isInstanceOf(InvalidReservationDatesException.class)
                .hasMessageContaining("must be after start date time");
    }

    @Test
    @DisplayName("Should pass capacity validation when guestsCount <= capacity")
    void shouldPassCapacityValidation() {
        Establishment establishment = Establishment.builder().capacity(4).build();
        domainService.validateCapacity(establishment, 2);
        domainService.validateCapacity(establishment, 4);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when guestsCount <= 0")
    void shouldThrowWhenGuestsCountIsInvalid() {
        Establishment establishment = Establishment.builder().capacity(4).build();

        assertThatThrownBy(() -> domainService.validateCapacity(establishment, 0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Should throw ExceededCapacityException when guestsCount > capacity")
    void shouldThrowWhenGuestsCountExceedsCapacity() {
        Establishment establishment = Establishment.builder().capacity(2).build();

        assertThatThrownBy(() -> domainService.validateCapacity(establishment, 3))
                .isInstanceOf(ExceededCapacityException.class)
                .hasMessageContaining("exceeds maximum establishment capacity");
    }

    @Test
    @DisplayName("Should ensure no overlap when repository returns empty overlapping list")
    void shouldPassEnsureNoOverlapWhenNoConflicts() {
        UUID establishmentId = UUID.randomUUID();
        Establishment establishment = Establishment.builder().id(establishmentId).build();
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.plusHours(2);

        when(reservationRepository.findOverlappingReservations(eq(establishmentId), eq(start), eq(end), any()))
                .thenReturn(Collections.emptyList());

        domainService.ensureNoOverlap(establishment, start, end);
    }

    @Test
    @DisplayName("Should throw ReservationConflictException when repository returns overlapping reservations")
    void shouldThrowReservationConflictExceptionWhenOverlapsExist() {
        UUID establishmentId = UUID.randomUUID();
        Establishment establishment = Establishment.builder().id(establishmentId).build();
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.plusHours(2);

        Reservation existingOverlap = Reservation.builder()
                .id(UUID.randomUUID())
                .establishmentId(establishmentId)
                .startDateTime(start)
                .endDateTime(end)
                .status(ReservationStatus.CONFIRMED)
                .build();

        when(reservationRepository.findOverlappingReservations(eq(establishmentId), eq(start), eq(end), any()))
                .thenReturn(List.of(existingOverlap));

        assertThatThrownBy(() -> domainService.ensureNoOverlap(establishment, start, end))
                .isInstanceOf(ReservationConflictException.class)
                .hasMessageContaining("overlapping reservation");
    }

    @Test
    @DisplayName("Should calculate total price correctly for hourly establishments")
    void shouldCalculateHourlyPrice() {
        Establishment establishment = Establishment.builder()
                .pricePerUnit(new BigDecimal("50.00"))
                .unitType(ReservationUnitType.HOUR)
                .build();

        LocalDateTime start = LocalDateTime.of(2026, 11, 1, 10, 0);
        LocalDateTime end = LocalDateTime.of(2026, 11, 1, 13, 0);

        BigDecimal price = domainService.calculateTotalPrice(establishment, start, end);
        assertThat(price).isEqualByComparingTo("150.00");
    }

    @Test
    @DisplayName("Should calculate total price correctly for daily establishments")
    void shouldCalculateDailyPrice() {
        Establishment establishment = Establishment.builder()
                .pricePerUnit(new BigDecimal("100.00"))
                .unitType(ReservationUnitType.DAY)
                .build();

        LocalDateTime start = LocalDateTime.of(2026, 11, 1, 14, 0);
        LocalDateTime end = LocalDateTime.of(2026, 11, 4, 11, 0);

        BigDecimal price = domainService.calculateTotalPrice(establishment, start, end);
        assertThat(price).isEqualByComparingTo("300.00");
    }
}
