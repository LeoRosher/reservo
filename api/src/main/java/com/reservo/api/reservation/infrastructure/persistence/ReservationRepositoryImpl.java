package com.reservo.api.reservation.infrastructure.persistence;

import com.reservo.api.reservation.domain.model.Reservation;
import com.reservo.api.reservation.domain.model.ReservationStatus;
import com.reservo.api.reservation.domain.port.ReservationRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class ReservationRepositoryImpl implements ReservationRepository {

    private final SpringDataReservationRepository repository;

    public ReservationRepositoryImpl(SpringDataReservationRepository repository) {
        this.repository = repository;
    }

    @Override
    public Reservation save(Reservation reservation) {
        if (reservation.getId() != null) {
            Optional<ReservationJpaEntity> existingOpt = repository.findById(reservation.getId());
            if (existingOpt.isPresent()) {
                ReservationJpaEntity existing = existingOpt.get();
                updateEntityFromDomain(existing, reservation);
                ReservationJpaEntity saved = repository.save(existing);
                return toDomain(saved);
            }
        }

        ReservationJpaEntity entity = toEntity(reservation);
        ReservationJpaEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Reservation> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Reservation> findByCustomerId(UUID customerId) {
        return repository.findByCustomerId(customerId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Reservation> findByEstablishmentId(UUID establishmentId) {
        return repository.findByEstablishmentId(establishmentId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Reservation> findOverlappingReservations(
            UUID establishmentId,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime,
            List<ReservationStatus> activeStatuses
    ) {
        Instant startInstant = startDateTime.toInstant(ZoneOffset.UTC);
        Instant endInstant = endDateTime.toInstant(ZoneOffset.UTC);

        return repository.findOverlappingReservations(
                establishmentId,
                startInstant,
                endInstant,
                activeStatuses
        ).stream().map(this::toDomain).toList();
    }

    private void updateEntityFromDomain(ReservationJpaEntity entity, Reservation domain) {
        Instant updatedAtInstant = domain.getUpdatedAt() != null
                ? domain.getUpdatedAt().toInstant(ZoneOffset.UTC)
                : Instant.now();

        entity.setStatus(domain.getStatus());
        entity.setCancellationReason(domain.getCancellationReason());
        entity.setGuestsCount(domain.getGuestsCount());
        entity.setTotalPrice(domain.getTotalPrice());
        entity.setUpdatedAt(updatedAtInstant);
    }

    private ReservationJpaEntity toEntity(Reservation domain) {
        Instant createdAtInstant = domain.getCreatedAt() != null
                ? domain.getCreatedAt().toInstant(ZoneOffset.UTC)
                : Instant.now();
        Instant updatedAtInstant = domain.getUpdatedAt() != null
                ? domain.getUpdatedAt().toInstant(ZoneOffset.UTC)
                : Instant.now();
        Instant startInstant = domain.getStartDateTime() != null
                ? domain.getStartDateTime().toInstant(ZoneOffset.UTC)
                : null;
        Instant endInstant = domain.getEndDateTime() != null
                ? domain.getEndDateTime().toInstant(ZoneOffset.UTC)
                : null;

        return ReservationJpaEntity.builder()
                .id(domain.getId())
                .customerId(domain.getCustomerId())
                .establishmentId(domain.getEstablishmentId())
                .startDateTime(startInstant)
                .endDateTime(endInstant)
                .guestsCount(domain.getGuestsCount())
                .totalPrice(domain.getTotalPrice())
                .status(domain.getStatus())
                .cancellationReason(domain.getCancellationReason())
                .createdAt(createdAtInstant)
                .updatedAt(updatedAtInstant)
                .build();
    }

    private Reservation toDomain(ReservationJpaEntity entity) {
        LocalDateTime createdAt = entity.getCreatedAt() != null
                ? LocalDateTime.ofInstant(entity.getCreatedAt(), ZoneOffset.UTC)
                : null;
        LocalDateTime updatedAt = entity.getUpdatedAt() != null
                ? LocalDateTime.ofInstant(entity.getUpdatedAt(), ZoneOffset.UTC)
                : null;
        LocalDateTime startDateTime = entity.getStartDateTime() != null
                ? LocalDateTime.ofInstant(entity.getStartDateTime(), ZoneOffset.UTC)
                : null;
        LocalDateTime endDateTime = entity.getEndDateTime() != null
                ? LocalDateTime.ofInstant(entity.getEndDateTime(), ZoneOffset.UTC)
                : null;

        return Reservation.builder()
                .id(entity.getId())
                .customerId(entity.getCustomerId())
                .establishmentId(entity.getEstablishmentId())
                .startDateTime(startDateTime)
                .endDateTime(endDateTime)
                .guestsCount(entity.getGuestsCount())
                .totalPrice(entity.getTotalPrice())
                .status(entity.getStatus())
                .cancellationReason(entity.getCancellationReason())
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }
}
