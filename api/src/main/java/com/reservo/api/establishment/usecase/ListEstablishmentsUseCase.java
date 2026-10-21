package com.reservo.api.establishment.usecase;

import com.reservo.api.establishment.domain.model.Establishment;
import com.reservo.api.establishment.domain.model.EstablishmentCategory;
import com.reservo.api.establishment.domain.port.EstablishmentRepository;

import java.util.List;

public class ListEstablishmentsUseCase {

    private final EstablishmentRepository establishmentRepository;

    public ListEstablishmentsUseCase(EstablishmentRepository establishmentRepository) {
        this.establishmentRepository = establishmentRepository;
    }

    public record Query(
            EstablishmentCategory category,
            String city,
            Boolean activeOnly
    ) {}

    public List<Establishment> execute(Query query) {
        Boolean activeOnly = query.activeOnly() != null ? query.activeOnly() : true;
        return establishmentRepository.search(query.category(), query.city(), activeOnly);
    }
}
