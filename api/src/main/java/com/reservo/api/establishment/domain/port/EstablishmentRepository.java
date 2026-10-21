package com.reservo.api.establishment.domain.port;

import com.reservo.api.establishment.domain.model.Establishment;
import com.reservo.api.establishment.domain.model.EstablishmentCategory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EstablishmentRepository {
    Establishment save(Establishment establishment);
    Optional<Establishment> findById(UUID id);
    Optional<Establishment> findByIdWithLock(UUID id);
    List<Establishment> findByHostId(UUID hostId);
    List<Establishment> search(EstablishmentCategory category, String city, Boolean activeOnly);
}
