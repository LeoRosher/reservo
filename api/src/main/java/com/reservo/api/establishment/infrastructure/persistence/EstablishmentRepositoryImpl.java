package com.reservo.api.establishment.infrastructure.persistence;

import com.reservo.api.establishment.domain.model.Address;
import com.reservo.api.establishment.domain.model.Establishment;
import com.reservo.api.establishment.domain.model.EstablishmentCategory;
import com.reservo.api.establishment.domain.port.EstablishmentRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class EstablishmentRepositoryImpl implements EstablishmentRepository {

    private final SpringDataEstablishmentRepository repository;

    public EstablishmentRepositoryImpl(SpringDataEstablishmentRepository repository) {
        this.repository = repository;
    }

    @Override
    public Establishment save(Establishment establishment) {
        if (establishment.getId() != null) {
            Optional<EstablishmentJpaEntity> existingOpt = repository.findById(establishment.getId());
            if (existingOpt.isPresent()) {
                EstablishmentJpaEntity existing = existingOpt.get();
                updateEntityFromDomain(existing, establishment);
                EstablishmentJpaEntity saved = repository.save(existing);
                return toDomain(saved);
            }
        }
        EstablishmentJpaEntity entity = toEntity(establishment);
        EstablishmentJpaEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    private void updateEntityFromDomain(EstablishmentJpaEntity entity, Establishment domain) {
        Instant updatedAtInstant = domain.getUpdatedAt() != null
                ? domain.getUpdatedAt().toInstant(ZoneOffset.UTC)
                : Instant.now();

        entity.setName(domain.getName());
        entity.setDescription(domain.getDescription());
        entity.setCategory(domain.getCategory());
        entity.setStreet(domain.getAddress() != null ? domain.getAddress().getStreet() : null);
        entity.setCity(domain.getAddress() != null ? domain.getAddress().getCity() : null);
        entity.setState(domain.getAddress() != null ? domain.getAddress().getState() : null);
        entity.setCountry(domain.getAddress() != null ? domain.getAddress().getCountry() : null);
        entity.setZipCode(domain.getAddress() != null ? domain.getAddress().getZipCode() : null);
        entity.setLatitude(domain.getAddress() != null ? domain.getAddress().getLatitude() : null);
        entity.setLongitude(domain.getAddress() != null ? domain.getAddress().getLongitude() : null);
        entity.setContactPhone(domain.getContactPhone());
        entity.setContactEmail(domain.getContactEmail());
        entity.setCapacity(domain.getCapacity());
        entity.setPricePerUnit(domain.getPricePerUnit());
        entity.setUnitType(domain.getUnitType());
        entity.setActive(domain.isActive());
        entity.setUpdatedAt(updatedAtInstant);

        entity.getAmenities().clear();
        if (domain.getAmenities() != null) {
            entity.getAmenities().addAll(domain.getAmenities());
        }

        entity.getRules().clear();
        if (domain.getRules() != null) {
            entity.getRules().addAll(domain.getRules());
        }

        entity.getImages().clear();
        if (domain.getImages() != null && !domain.getImages().isEmpty()) {
            for (int i = 0; i < domain.getImages().size(); i++) {
                entity.getImages().add(EstablishmentImageJpaEntity.builder()
                        .id(UUID.randomUUID())
                        .establishment(entity)
                        .imageUrl(domain.getImages().get(i))
                        .displayOrder(i)
                        .build());
            }
        }
    }

    @Override
    public Optional<Establishment> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Establishment> findByIdWithLock(UUID id) {
        return repository.findByIdWithLock(id).map(this::toDomain);
    }

    @Override
    public List<Establishment> findByHostId(UUID hostId) {
        return repository.findByHostId(hostId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Establishment> search(EstablishmentCategory category, String city, Boolean activeOnly) {
        return repository.search(category, city, activeOnly).stream().map(this::toDomain).toList();
    }

    private EstablishmentJpaEntity toEntity(Establishment domain) {
        Instant createdAtInstant = domain.getCreatedAt() != null
                ? domain.getCreatedAt().toInstant(ZoneOffset.UTC)
                : Instant.now();
        Instant updatedAtInstant = domain.getUpdatedAt() != null
                ? domain.getUpdatedAt().toInstant(ZoneOffset.UTC)
                : Instant.now();

        EstablishmentJpaEntity entity = EstablishmentJpaEntity.builder()
                .id(domain.getId())
                .hostId(domain.getHostId())
                .name(domain.getName())
                .description(domain.getDescription())
                .category(domain.getCategory())
                .street(domain.getAddress() != null ? domain.getAddress().getStreet() : null)
                .city(domain.getAddress() != null ? domain.getAddress().getCity() : null)
                .state(domain.getAddress() != null ? domain.getAddress().getState() : null)
                .country(domain.getAddress() != null ? domain.getAddress().getCountry() : null)
                .zipCode(domain.getAddress() != null ? domain.getAddress().getZipCode() : null)
                .latitude(domain.getAddress() != null ? domain.getAddress().getLatitude() : null)
                .longitude(domain.getAddress() != null ? domain.getAddress().getLongitude() : null)
                .contactPhone(domain.getContactPhone())
                .contactEmail(domain.getContactEmail())
                .capacity(domain.getCapacity())
                .pricePerUnit(domain.getPricePerUnit())
                .unitType(domain.getUnitType())
                .active(domain.isActive())
                .createdAt(createdAtInstant)
                .updatedAt(updatedAtInstant)
                .amenities(domain.getAmenities() != null ? new ArrayList<>(domain.getAmenities()) : new ArrayList<>())
                .rules(domain.getRules() != null ? new ArrayList<>(domain.getRules()) : new ArrayList<>())
                .build();

        if (domain.getImages() != null && !domain.getImages().isEmpty()) {
            List<EstablishmentImageJpaEntity> imageEntities = new ArrayList<>();
            for (int i = 0; i < domain.getImages().size(); i++) {
                imageEntities.add(EstablishmentImageJpaEntity.builder()
                        .id(UUID.randomUUID())
                        .establishment(entity)
                        .imageUrl(domain.getImages().get(i))
                        .displayOrder(i)
                        .build());
            }
            entity.setImages(imageEntities);
        }

        return entity;
    }

    private Establishment toDomain(EstablishmentJpaEntity entity) {
        Address address = Address.builder()
                .street(entity.getStreet())
                .city(entity.getCity())
                .state(entity.getState())
                .country(entity.getCountry())
                .zipCode(entity.getZipCode())
                .latitude(entity.getLatitude())
                .longitude(entity.getLongitude())
                .build();

        List<String> images = entity.getImages() != null
                ? entity.getImages().stream().map(EstablishmentImageJpaEntity::getImageUrl).toList()
                : new ArrayList<>();

        LocalDateTime createdAt = entity.getCreatedAt() != null
                ? LocalDateTime.ofInstant(entity.getCreatedAt(), ZoneOffset.UTC)
                : null;
        LocalDateTime updatedAt = entity.getUpdatedAt() != null
                ? LocalDateTime.ofInstant(entity.getUpdatedAt(), ZoneOffset.UTC)
                : null;

        return Establishment.builder()
                .id(entity.getId())
                .hostId(entity.getHostId())
                .name(entity.getName())
                .description(entity.getDescription())
                .category(entity.getCategory())
                .address(address)
                .contactPhone(entity.getContactPhone())
                .contactEmail(entity.getContactEmail())
                .capacity(entity.getCapacity())
                .pricePerUnit(entity.getPricePerUnit())
                .unitType(entity.getUnitType())
                .amenities(entity.getAmenities() != null ? new ArrayList<>(entity.getAmenities()) : new ArrayList<>())
                .rules(entity.getRules() != null ? new ArrayList<>(entity.getRules()) : new ArrayList<>())
                .images(images)
                .active(entity.isActive())
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }
}
