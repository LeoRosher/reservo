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
import com.reservo.api.user.domain.port.UserRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class UpdateEstablishmentUseCase {

    private final EstablishmentRepository establishmentRepository;
    private final UserRepository userRepository;

    public UpdateEstablishmentUseCase(EstablishmentRepository establishmentRepository, UserRepository userRepository) {
        this.establishmentRepository = establishmentRepository;
        this.userRepository = userRepository;
    }

    public record Command(
            UUID establishmentId,
            UUID hostId,
            String name,
            String description,
            EstablishmentCategory category,
            Address address,
            String contactPhone,
            String contactEmail,
            int capacity,
            BigDecimal pricePerUnit,
            ReservationUnitType unitType,
            List<String> amenities,
            List<String> rules,
            List<String> images
    ) {}

    public Establishment execute(Command command) {
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

        if (!establishment.isActive()) {
            throw new IllegalStateException("Cannot update an inactive or deleted establishment.");
        }

        establishment.updateDetails(
                command.name(),
                command.description(),
                command.category(),
                command.address(),
                command.contactPhone(),
                command.contactEmail(),
                command.capacity(),
                command.pricePerUnit(),
                command.unitType(),
                command.amenities(),
                command.rules(),
                command.images()
        );

        return establishmentRepository.save(establishment);
    }
}
