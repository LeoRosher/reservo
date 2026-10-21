package com.reservo.api.establishment.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Establishment {

    private UUID id;
    private UUID hostId;
    private String name;
    private String description;
    private EstablishmentCategory category;
    private Address address;
    private String contactPhone;
    private String contactEmail;
    private int capacity;
    private BigDecimal pricePerUnit;
    private ReservationUnitType unitType;
    @Builder.Default
    private List<String> amenities = new ArrayList<>();
    @Builder.Default
    private List<String> rules = new ArrayList<>();
    @Builder.Default
    private List<String> images = new ArrayList<>();
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public boolean belongsToHost(UUID hostId) {
        return this.hostId != null && this.hostId.equals(hostId);
    }

    public void activate() {
        this.active = true;
        this.updatedAt = LocalDateTime.now();
    }

    public void deactivate() {
        this.active = false;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateDetails(
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
    ) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Establishment name cannot be blank.");
        }
        if (category == null) {
            throw new IllegalArgumentException("Establishment category cannot be null.");
        }
        if (address == null) {
            throw new IllegalArgumentException("Establishment address cannot be null.");
        }
        if (capacity <= 0) {
            throw new IllegalArgumentException("Establishment capacity must be greater than zero.");
        }
        if (pricePerUnit == null || pricePerUnit.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price per unit must be non-negative.");
        }
        if (unitType == null) {
            throw new IllegalArgumentException("Reservation unit type cannot be null.");
        }

        this.name = name;
        this.description = description;
        this.category = category;
        this.address = address;
        this.contactPhone = contactPhone;
        this.contactEmail = contactEmail;
        this.capacity = capacity;
        this.pricePerUnit = pricePerUnit;
        this.unitType = unitType;
        this.amenities = amenities != null ? new ArrayList<>(amenities) : new ArrayList<>();
        this.rules = rules != null ? new ArrayList<>(rules) : new ArrayList<>();
        this.images = images != null ? new ArrayList<>(images) : new ArrayList<>();
        this.updatedAt = LocalDateTime.now();
    }

    public static Establishment createNew(
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
    ) {
        LocalDateTime now = LocalDateTime.now();
        return Establishment.builder()
                .id(UUID.randomUUID())
                .hostId(hostId)
                .name(name)
                .description(description)
                .category(category)
                .address(address)
                .contactPhone(contactPhone)
                .contactEmail(contactEmail)
                .capacity(capacity)
                .pricePerUnit(pricePerUnit)
                .unitType(unitType)
                .amenities(amenities != null ? amenities : new ArrayList<>())
                .rules(rules != null ? rules : new ArrayList<>())
                .images(images != null ? images : new ArrayList<>())
                .active(true)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }
}
