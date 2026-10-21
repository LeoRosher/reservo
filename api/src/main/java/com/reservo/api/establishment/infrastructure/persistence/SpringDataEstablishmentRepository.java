package com.reservo.api.establishment.infrastructure.persistence;

import com.reservo.api.establishment.domain.model.EstablishmentCategory;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataEstablishmentRepository extends JpaRepository<EstablishmentJpaEntity, UUID> {

    List<EstablishmentJpaEntity> findByHostId(UUID hostId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM EstablishmentJpaEntity e WHERE e.id = :id")
    Optional<EstablishmentJpaEntity> findByIdWithLock(@Param("id") UUID id);

    @Query("SELECT e FROM EstablishmentJpaEntity e WHERE " +
           "(:category IS NULL OR e.category = :category) AND " +
           "(:city IS NULL OR LOWER(e.city) LIKE LOWER(CONCAT('%', :city, '%'))) AND " +
           "(:activeOnly IS NULL OR e.active = :activeOnly)")
    List<EstablishmentJpaEntity> search(
            @Param("category") EstablishmentCategory category,
            @Param("city") String city,
            @Param("activeOnly") Boolean activeOnly
    );
}
