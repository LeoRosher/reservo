package com.reservo.api.establishment.usecase;

import com.reservo.api.establishment.domain.exception.EstablishmentNotFoundException;
import com.reservo.api.establishment.domain.exception.UnauthorizedHostException;
import com.reservo.api.establishment.domain.model.Address;
import com.reservo.api.establishment.domain.model.Establishment;
import com.reservo.api.establishment.domain.model.EstablishmentCategory;
import com.reservo.api.establishment.domain.model.ReservationUnitType;
import com.reservo.api.establishment.domain.port.EstablishmentRepository;
import com.reservo.api.user.domain.exception.UserNotFoundException;
import com.reservo.api.user.domain.model.User;
import com.reservo.api.user.domain.model.UserRole;
import com.reservo.api.user.domain.port.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateEstablishmentUseCaseTest {

    @Mock
    private EstablishmentRepository establishmentRepository;

    @Mock
    private UserRepository userRepository;

    private UpdateEstablishmentUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new UpdateEstablishmentUseCase(establishmentRepository, userRepository);
    }

    @Test
    @DisplayName("Should successfully update establishment when host is valid owner")
    void shouldUpdateEstablishmentSuccessfullyWhenHostIsOwner() {
        UUID hostId = UUID.randomUUID();
        UUID establishmentId = UUID.randomUUID();

        User host = User.builder()
                .id(hostId)
                .name("Alice Host")
                .role(UserRole.HOST)
                .build();

        Establishment existing = Establishment.createNew(
                hostId,
                "Old Name",
                "Old Desc",
                EstablishmentCategory.HOTEL,
                Address.builder().city("Old City").country("Old Country").build(),
                null,
                null,
                2,
                BigDecimal.valueOf(100),
                ReservationUnitType.NIGHT,
                null,
                null,
                null
        );
        existing.setId(establishmentId);

        when(userRepository.findById(hostId)).thenReturn(Optional.of(host));
        when(establishmentRepository.findById(establishmentId)).thenReturn(Optional.of(existing));
        when(establishmentRepository.save(any(Establishment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Address newAddress = Address.builder().street("789 Pine St").city("Seattle").country("USA").build();

        UpdateEstablishmentUseCase.Command command = new UpdateEstablishmentUseCase.Command(
                establishmentId,
                hostId,
                "Updated Luxury Cabin",
                "Updated Description",
                EstablishmentCategory.CABIN,
                newAddress,
                "+1-222-333444",
                "updated@cabin.com",
                6,
                new BigDecimal("299.99"),
                ReservationUnitType.DAY,
                List.of("Hot Tub", "Sauna"),
                List.of("No smoking inside"),
                List.of("https://example.com/cabin.jpg")
        );

        Establishment result = useCase.execute(command);

        assertThat(result.getName()).isEqualTo("Updated Luxury Cabin");
        assertThat(result.getDescription()).isEqualTo("Updated Description");
        assertThat(result.getCategory()).isEqualTo(EstablishmentCategory.CABIN);
        assertThat(result.getAddress()).isEqualTo(newAddress);
        assertThat(result.getCapacity()).isEqualTo(6);
        assertThat(result.getPricePerUnit()).isEqualTo(new BigDecimal("299.99"));
        assertThat(result.getUnitType()).isEqualTo(ReservationUnitType.DAY);
        assertThat(result.getAmenities()).containsExactly("Hot Tub", "Sauna");
        assertThat(result.getRules()).containsExactly("No smoking inside");
        assertThat(result.getImages()).containsExactly("https://example.com/cabin.jpg");

        ArgumentCaptor<Establishment> captor = ArgumentCaptor.forClass(Establishment.class);
        verify(establishmentRepository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("Updated Luxury Cabin");
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when user does not exist")
    void shouldThrowUserNotFoundExceptionWhenUserDoesNotExist() {
        UUID nonExistentHostId = UUID.randomUUID();
        UUID establishmentId = UUID.randomUUID();

        when(userRepository.findById(nonExistentHostId)).thenReturn(Optional.empty());

        UpdateEstablishmentUseCase.Command command = new UpdateEstablishmentUseCase.Command(
                establishmentId,
                nonExistentHostId,
                "Name",
                "Desc",
                EstablishmentCategory.HOTEL,
                Address.builder().city("City").country("Country").build(),
                null,
                null,
                2,
                BigDecimal.TEN,
                ReservationUnitType.NIGHT,
                null,
                null,
                null
        );

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining(nonExistentHostId.toString());

        verify(establishmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw UnauthorizedHostException when user is not a HOST")
    void shouldThrowUnauthorizedHostExceptionWhenUserIsNotHost() {
        UUID customerId = UUID.randomUUID();
        UUID establishmentId = UUID.randomUUID();

        User customer = User.builder()
                .id(customerId)
                .role(UserRole.CUSTOMER)
                .build();

        when(userRepository.findById(customerId)).thenReturn(Optional.of(customer));

        UpdateEstablishmentUseCase.Command command = new UpdateEstablishmentUseCase.Command(
                establishmentId,
                customerId,
                "Name",
                "Desc",
                EstablishmentCategory.HOTEL,
                Address.builder().city("City").country("Country").build(),
                null,
                null,
                2,
                BigDecimal.TEN,
                ReservationUnitType.NIGHT,
                null,
                null,
                null
        );

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(UnauthorizedHostException.class)
                .hasMessageContaining(customerId.toString());

        verify(establishmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw EstablishmentNotFoundException when establishment does not exist")
    void shouldThrowEstablishmentNotFoundExceptionWhenEstablishmentDoesNotExist() {
        UUID hostId = UUID.randomUUID();
        UUID nonExistentEstablishmentId = UUID.randomUUID();

        User host = User.builder().id(hostId).role(UserRole.HOST).build();
        when(userRepository.findById(hostId)).thenReturn(Optional.of(host));
        when(establishmentRepository.findById(nonExistentEstablishmentId)).thenReturn(Optional.empty());

        UpdateEstablishmentUseCase.Command command = new UpdateEstablishmentUseCase.Command(
                nonExistentEstablishmentId,
                hostId,
                "Name",
                "Desc",
                EstablishmentCategory.HOTEL,
                Address.builder().city("City").country("Country").build(),
                null,
                null,
                2,
                BigDecimal.TEN,
                ReservationUnitType.NIGHT,
                null,
                null,
                null
        );

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(EstablishmentNotFoundException.class)
                .hasMessageContaining(nonExistentEstablishmentId.toString());

        verify(establishmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw UnauthorizedHostException when host is not the owner")
    void shouldThrowUnauthorizedHostExceptionWhenHostIsNotOwner() {
        UUID hostId = UUID.randomUUID();
        UUID ownerHostId = UUID.randomUUID();
        UUID establishmentId = UUID.randomUUID();

        User host = User.builder().id(hostId).role(UserRole.HOST).build();
        Establishment existing = Establishment.builder()
                .id(establishmentId)
                .hostId(ownerHostId)
                .active(true)
                .build();

        when(userRepository.findById(hostId)).thenReturn(Optional.of(host));
        when(establishmentRepository.findById(establishmentId)).thenReturn(Optional.of(existing));

        UpdateEstablishmentUseCase.Command command = new UpdateEstablishmentUseCase.Command(
                establishmentId,
                hostId,
                "Name",
                "Desc",
                EstablishmentCategory.HOTEL,
                Address.builder().city("City").country("Country").build(),
                null,
                null,
                2,
                BigDecimal.TEN,
                ReservationUnitType.NIGHT,
                null,
                null,
                null
        );

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(UnauthorizedHostException.class)
                .hasMessageContaining(hostId.toString())
                .hasMessageContaining(establishmentId.toString());

        verify(establishmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw IllegalStateException when establishment is inactive")
    void shouldThrowIllegalStateExceptionWhenEstablishmentIsInactive() {
        UUID hostId = UUID.randomUUID();
        UUID establishmentId = UUID.randomUUID();

        User host = User.builder().id(hostId).role(UserRole.HOST).build();
        Establishment inactive = Establishment.builder()
                .id(establishmentId)
                .hostId(hostId)
                .active(false)
                .build();

        when(userRepository.findById(hostId)).thenReturn(Optional.of(host));
        when(establishmentRepository.findById(establishmentId)).thenReturn(Optional.of(inactive));

        UpdateEstablishmentUseCase.Command command = new UpdateEstablishmentUseCase.Command(
                establishmentId,
                hostId,
                "Name",
                "Desc",
                EstablishmentCategory.HOTEL,
                Address.builder().city("City").country("Country").build(),
                null,
                null,
                2,
                BigDecimal.TEN,
                ReservationUnitType.NIGHT,
                null,
                null,
                null
        );

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot update an inactive or deleted establishment.");

        verify(establishmentRepository, never()).save(any());
    }
}
