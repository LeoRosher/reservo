package com.reservo.api.establishment.usecase;

import com.reservo.api.establishment.domain.exception.EstablishmentNotFoundException;
import com.reservo.api.establishment.domain.model.Establishment;
import com.reservo.api.establishment.domain.port.EstablishmentRepository;

import java.util.UUID;

public class GetEstablishmentByIdUseCase {

    private final EstablishmentRepository establishmentRepository;

    public GetEstablishmentByIdUseCase(EstablishmentRepository establishmentRepository) {
        this.establishmentRepository = establishmentRepository;
    }

    public Establishment execute(UUID id) {
        return establishmentRepository.findById(id)
                .orElseThrow(() -> new EstablishmentNotFoundException(id));
    }
}
