package com.reservo.api.establishment.usecase;

import com.reservo.api.establishment.domain.exception.EstablishmentNotFoundException;
import com.reservo.api.establishment.domain.exception.UnauthorizedHostException;
import com.reservo.api.establishment.domain.model.Establishment;
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

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteEstablishmentUseCaseTest {

    @Mock
    private EstablishmentRepository establishmentRepository;

    @Mock
    private UserRepository userRepository;

    private DeleteEstablishmentUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new DeleteEstablishmentUseCase(establishmentRepository, userRepository);
    }

    @Test
    @DisplayName("Should soft-delete establishment by setting active to false and saving")
    void shouldSoftDeleteEstablishmentSuccessfullyWhenHostIsOwner() {
        UUID hostId = UUID.randomUUID();
        UUID establishmentId = UUID.randomUUID();

        User host = User.builder().id(hostId).role(UserRole.HOST).build();
        Establishment establishment = Establishment.builder()
                .id(establishmentId)
                .hostId(hostId)
                .active(true)
                .build();

        when(userRepository.findById(hostId)).thenReturn(Optional.of(host));
        when(establishmentRepository.findById(establishmentId)).thenReturn(Optional.of(establishment));

        useCase.execute(new DeleteEstablishmentUseCase.Command(establishmentId, hostId));

        assertThat(establishment.isActive()).isFalse();
        assertThat(establishment.getUpdatedAt()).isNotNull();

        ArgumentCaptor<Establishment> captor = ArgumentCaptor.forClass(Establishment.class);
        verify(establishmentRepository).save(captor.capture());
        assertThat(captor.getValue().isActive()).isFalse();
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when host does not exist")
    void shouldThrowUserNotFoundExceptionWhenHostDoesNotExist() {
        UUID hostId = UUID.randomUUID();
        UUID establishmentId = UUID.randomUUID();

        when(userRepository.findById(hostId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new DeleteEstablishmentUseCase.Command(establishmentId, hostId)))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining(hostId.toString());

        verify(establishmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw UnauthorizedHostException when user is not a host")
    void shouldThrowUnauthorizedHostExceptionWhenUserIsNotHost() {
        UUID customerId = UUID.randomUUID();
        UUID establishmentId = UUID.randomUUID();

        User customer = User.builder().id(customerId).role(UserRole.CUSTOMER).build();
        when(userRepository.findById(customerId)).thenReturn(Optional.of(customer));

        assertThatThrownBy(() -> useCase.execute(new DeleteEstablishmentUseCase.Command(establishmentId, customerId)))
                .isInstanceOf(UnauthorizedHostException.class)
                .hasMessageContaining(customerId.toString());

        verify(establishmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw EstablishmentNotFoundException when establishment does not exist")
    void shouldThrowEstablishmentNotFoundExceptionWhenEstablishmentDoesNotExist() {
        UUID hostId = UUID.randomUUID();
        UUID establishmentId = UUID.randomUUID();

        User host = User.builder().id(hostId).role(UserRole.HOST).build();
        when(userRepository.findById(hostId)).thenReturn(Optional.of(host));
        when(establishmentRepository.findById(establishmentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new DeleteEstablishmentUseCase.Command(establishmentId, hostId)))
                .isInstanceOf(EstablishmentNotFoundException.class)
                .hasMessageContaining(establishmentId.toString());

        verify(establishmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw UnauthorizedHostException when host is not the owner")
    void shouldThrowUnauthorizedHostExceptionWhenHostIsNotOwner() {
        UUID hostId = UUID.randomUUID();
        UUID otherHostId = UUID.randomUUID();
        UUID establishmentId = UUID.randomUUID();

        User host = User.builder().id(hostId).role(UserRole.HOST).build();
        Establishment establishment = Establishment.builder()
                .id(establishmentId)
                .hostId(otherHostId)
                .active(true)
                .build();

        when(userRepository.findById(hostId)).thenReturn(Optional.of(host));
        when(establishmentRepository.findById(establishmentId)).thenReturn(Optional.of(establishment));

        assertThatThrownBy(() -> useCase.execute(new DeleteEstablishmentUseCase.Command(establishmentId, hostId)))
                .isInstanceOf(UnauthorizedHostException.class)
                .hasMessageContaining(hostId.toString())
                .hasMessageContaining(establishmentId.toString());

        verify(establishmentRepository, never()).save(any());
    }
}
