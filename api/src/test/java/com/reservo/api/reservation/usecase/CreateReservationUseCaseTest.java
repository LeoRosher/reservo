package com.reservo.api.reservation.usecase;

import com.reservo.api.establishment.domain.exception.EstablishmentNotFoundException;
import com.reservo.api.establishment.domain.model.Establishment;
import com.reservo.api.establishment.domain.port.EstablishmentRepository;
import com.reservo.api.reservation.domain.model.Reservation;
import com.reservo.api.reservation.domain.model.ReservationStatus;
import com.reservo.api.reservation.domain.port.ReservationRepository;
import com.reservo.api.reservation.domain.service.ReservationDomainService;
import com.reservo.api.user.domain.exception.UserNotFoundException;
import com.reservo.api.user.domain.model.User;
import com.reservo.api.user.domain.model.UserRole;
import com.reservo.api.user.domain.port.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateReservationUseCaseTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private EstablishmentRepository establishmentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReservationDomainService reservationDomainService;

    private CreateReservationUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateReservationUseCase(
                reservationRepository,
                establishmentRepository,
                userRepository,
                reservationDomainService
        );
    }

    @Test
    @DisplayName("Should create reservation successfully when customer and establishment are valid")
    void shouldCreateReservationSuccessfully() {
        UUID customerId = UUID.randomUUID();
        UUID establishmentId = UUID.randomUUID();

        User customer = User.builder().id(customerId).role(UserRole.CUSTOMER).build();
        Establishment establishment = Establishment.builder()
                .id(establishmentId)
                .capacity(4)
                .active(true)
                .build();

        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.plusHours(2);

        when(userRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(establishmentRepository.findByIdWithLock(establishmentId)).thenReturn(Optional.of(establishment));
        when(reservationDomainService.calculateTotalPrice(establishment, start, end)).thenReturn(new BigDecimal("100.00"));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateReservationUseCase.Command command = new CreateReservationUseCase.Command(
                customerId,
                establishmentId,
                start,
                end,
                2
        );

        Reservation result = useCase.execute(command);

        assertThat(result).isNotNull();
        assertThat(result.getCustomerId()).isEqualTo(customerId);
        assertThat(result.getEstablishmentId()).isEqualTo(establishmentId);
        assertThat(result.getGuestsCount()).isEqualTo(2);
        assertThat(result.getTotalPrice()).isEqualByComparingTo("100.00");
        assertThat(result.getStatus()).isEqualTo(ReservationStatus.PENDING);

        verify(reservationDomainService).validateReservationDates(start, end);
        verify(reservationDomainService).validateCapacity(establishment, 2);
        verify(reservationDomainService).ensureNoOverlap(establishment, start, end);
        verify(establishmentRepository).findByIdWithLock(establishmentId);
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when customer does not exist")
    void shouldThrowUserNotFoundExceptionWhenCustomerDoesNotExist() {
        UUID customerId = UUID.randomUUID();
        UUID establishmentId = UUID.randomUUID();

        when(userRepository.findById(customerId)).thenReturn(Optional.empty());

        CreateReservationUseCase.Command command = new CreateReservationUseCase.Command(
                customerId,
                establishmentId,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                2
        );

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when user is not a CUSTOMER")
    void shouldThrowWhenUserIsNotCustomer() {
        UUID hostId = UUID.randomUUID();
        UUID establishmentId = UUID.randomUUID();

        User hostUser = User.builder().id(hostId).role(UserRole.HOST).build();
        when(userRepository.findById(hostId)).thenReturn(Optional.of(hostUser));

        CreateReservationUseCase.Command command = new CreateReservationUseCase.Command(
                hostId,
                establishmentId,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                2
        );

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Only users with CUSTOMER role");
    }

    @Test
    @DisplayName("Should throw EstablishmentNotFoundException when establishment does not exist")
    void shouldThrowWhenEstablishmentDoesNotExist() {
        UUID customerId = UUID.randomUUID();
        UUID establishmentId = UUID.randomUUID();

        User customer = User.builder().id(customerId).role(UserRole.CUSTOMER).build();
        when(userRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(establishmentRepository.findByIdWithLock(establishmentId)).thenReturn(Optional.empty());

        CreateReservationUseCase.Command command = new CreateReservationUseCase.Command(
                customerId,
                establishmentId,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                2
        );

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(EstablishmentNotFoundException.class);
    }

    @Test
    @DisplayName("Should throw IllegalStateException when establishment is not active")
    void shouldThrowWhenEstablishmentIsInactive() {
        UUID customerId = UUID.randomUUID();
        UUID establishmentId = UUID.randomUUID();

        User customer = User.builder().id(customerId).role(UserRole.CUSTOMER).build();
        Establishment inactiveEstablishment = Establishment.builder()
                .id(establishmentId)
                .active(false)
                .build();

        when(userRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(establishmentRepository.findByIdWithLock(establishmentId)).thenReturn(Optional.of(inactiveEstablishment));

        CreateReservationUseCase.Command command = new CreateReservationUseCase.Command(
                customerId,
                establishmentId,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                2
        );

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Establishment is currently inactive");
    }
}
