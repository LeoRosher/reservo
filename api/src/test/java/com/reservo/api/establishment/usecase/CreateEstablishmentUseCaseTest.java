package com.reservo.api.establishment.usecase;

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
class CreateEstablishmentUseCaseTest {

    @Mock
    private EstablishmentRepository establishmentRepository;

    @Mock
    private UserRepository userRepository;

    private CreateEstablishmentUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateEstablishmentUseCase(establishmentRepository, userRepository);
    }

    @Test
    @DisplayName("Should successfully create establishment when host exists and has HOST role")
    void shouldCreateEstablishmentSuccessfullyWhenHostIsValid() {
        // Arrange
        UUID hostId = UUID.randomUUID();
        User host = User.builder()
                .id(hostId)
                .name("Jane Host")
                .email("jane@host.com")
                .role(UserRole.HOST)
                .build();

        when(userRepository.findById(hostId)).thenReturn(Optional.of(host));
        when(establishmentRepository.save(any(Establishment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Address address = Address.builder()
                .street("123 Main St")
                .city("Springfield")
                .state("IL")
                .country("USA")
                .zipCode("62701")
                .latitude(new BigDecimal("39.7817"))
                .longitude(new BigDecimal("-89.6501"))
                .build();

        CreateEstablishmentUseCase.Command command = new CreateEstablishmentUseCase.Command(
                hostId,
                "Cozy Downtown Apartment",
                "Spacious apartment in the heart of Springfield",
                EstablishmentCategory.APARTMENT,
                address,
                "+1-555-0199",
                "info@cozyapt.com",
                4,
                new BigDecimal("120.00"),
                ReservationUnitType.NIGHT,
                List.of("WiFi", "Kitchen", "Air Conditioning"),
                List.of("No smoking", "No pets"),
                List.of("https://example.com/img1.jpg", "https://example.com/img2.jpg")
        );

        // Act
        Establishment result = useCase.execute(command);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getHostId()).isEqualTo(hostId);
        assertThat(result.getName()).isEqualTo("Cozy Downtown Apartment");
        assertThat(result.getCategory()).isEqualTo(EstablishmentCategory.APARTMENT);
        assertThat(result.getAddress()).isEqualTo(address);
        assertThat(result.getCapacity()).isEqualTo(4);
        assertThat(result.getPricePerUnit()).isEqualByComparingTo("120.00");
        assertThat(result.getUnitType()).isEqualTo(ReservationUnitType.NIGHT);
        assertThat(result.getAmenities()).containsExactly("WiFi", "Kitchen", "Air Conditioning");
        assertThat(result.getRules()).containsExactly("No smoking", "No pets");
        assertThat(result.getImages()).containsExactly("https://example.com/img1.jpg", "https://example.com/img2.jpg");
        assertThat(result.isActive()).isTrue();
        assertThat(result.getCreatedAt()).isNotNull();
        assertThat(result.getUpdatedAt()).isNotNull();

        ArgumentCaptor<Establishment> captor = ArgumentCaptor.forClass(Establishment.class);
        verify(establishmentRepository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("Cozy Downtown Apartment");
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when host does not exist")
    void shouldThrowUserNotFoundExceptionWhenHostDoesNotExist() {
        // Arrange
        UUID nonExistentHostId = UUID.randomUUID();
        when(userRepository.findById(nonExistentHostId)).thenReturn(Optional.empty());

        CreateEstablishmentUseCase.Command command = new CreateEstablishmentUseCase.Command(
                nonExistentHostId,
                "Cozy Apartment",
                "Description",
                EstablishmentCategory.APARTMENT,
                null,
                null,
                null,
                2,
                new BigDecimal("50.00"),
                ReservationUnitType.NIGHT,
                null,
                null,
                null
        );

        // Act & Assert
        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining(nonExistentHostId.toString());

        verify(establishmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw UnauthorizedHostException when user exists but role is not HOST")
    void shouldThrowUnauthorizedHostExceptionWhenUserIsNotHost() {
        // Arrange
        UUID customerId = UUID.randomUUID();
        User customer = User.builder()
                .id(customerId)
                .name("John Customer")
                .email("john@customer.com")
                .role(UserRole.CUSTOMER)
                .build();

        when(userRepository.findById(customerId)).thenReturn(Optional.of(customer));

        CreateEstablishmentUseCase.Command command = new CreateEstablishmentUseCase.Command(
                customerId,
                "Fake Hotel",
                "Description",
                EstablishmentCategory.HOTEL,
                null,
                null,
                null,
                2,
                new BigDecimal("100.00"),
                ReservationUnitType.NIGHT,
                null,
                null,
                null
        );

        // Act & Assert
        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(UnauthorizedHostException.class)
                .hasMessageContaining(customerId.toString());

        verify(establishmentRepository, never()).save(any());
    }
}
