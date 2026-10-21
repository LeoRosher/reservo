package com.reservo.api.reservation.infrastructure.persistence;

import com.reservo.api.reservation.domain.model.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataReservationRepository extends JpaRepository<ReservationJpaEntity, UUID> {

    List<ReservationJpaEntity> findByCustomerId(UUID customerId);

    List<ReservationJpaEntity> findByEstablishmentId(UUID establishmentId);

    @Query("SELECT r FROM ReservationJpaEntity r WHERE " +
           "r.establishmentId = :establishmentId AND " +
           "r.status IN :activeStatuses AND " +
           "r.startDateTime < :endDateTime AND " +
           "r.endDateTime > :startDateTime")
    List<ReservationJpaEntity> findOverlappingReservations(
            @Param("establishmentId") UUID establishmentId,
            @Param("startDateTime") Instant startDateTime,
            @Param("endDateTime") Instant endDateTime,
            @Param("activeStatuses") List<ReservationStatus> activeStatuses
    );
}
