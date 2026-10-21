package com.reservo.api.establishment.usecase;

import com.reservo.api.establishment.domain.exception.EstablishmentNotFoundException;
import com.reservo.api.establishment.domain.exception.UnauthorizedHostException;
import com.reservo.api.establishment.domain.model.Establishment;
import com.reservo.api.establishment.domain.port.EstablishmentRepository;
import com.reservo.api.user.domain.exception.UserNotFoundException;
import com.reservo.api.user.domain.model.User;
import com.reservo.api.user.domain.port.UserRepository;

import java.util.UUID;

public class DeleteEstablishmentUseCase {

    private final EstablishmentRepository establishmentRepository;
    private final UserRepository userRepository;

    public DeleteEstablishmentUseCase(EstablishmentRepository establishmentRepository, UserRepository userRepository) {
        this.establishmentRepository = establishmentRepository;
        this.userRepository = userRepository;
    }

    public record Command(
            UUID establishmentId,
            UUID hostId
    ) {}

    public void execute(Command command) {
        User host = userRepository.findById(command.hostId())
                .orElseThrow(() -> new UserNotFoundException(command.hostId()));

        if (!host.isHost()) {
            throw new UnauthorizedHostException(command.hostId());
        }

        Establishment establishment = establishmentRepository.findById(command.establishmentId())
                .orElseThrow(() -> new EstablishmentNotFoundException(command.establishmentId()));

        if (!establishment.belongsToHost(command.hostId())) {
            throw new UnauthorizedHostException(command.hostId(), command.establishmentId());
        }

        establishment.deactivate();
        establishmentRepository.save(establishment);
    }
}
