package com.reservo.api.establishment.presentation.dto;

import com.reservo.api.establishment.domain.model.Establishment;
import com.reservo.api.establishment.domain.model.EstablishmentCategory;
import com.reservo.api.establishment.domain.model.ReservationUnitType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Establishment response details")
public class EstablishmentResponse {

    @Schema(description = "Unique identifier of the establishment", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "Identifier of the host owner", example = "550e8400-e29b-41d4-a716-446655440001")
    private UUID hostId;

    @Schema(description = "Establishment name", example = "Sunset Beach Villa")
    private String name;

    @Schema(description = "Detailed description", example = "Luxurious villa located right in front of the beach")
    private String description;

    @Schema(description = "Category of the place", example = "APARTMENT")
    private EstablishmentCategory category;

    @Schema(description = "Physical address details")
    private AddressResponse address;

    @Schema(description = "Contact phone number", example = "+1-555-9876543")
    private String contactPhone;

    @Schema(description = "Contact email address", example = "sunset@villa.com")
    private String contactEmail;

    @Schema(description = "Maximum capacity", example = "4")
    private int capacity;

    @Schema(description = "Price per reservation unit", example = "150.00")
    private BigDecimal pricePerUnit;

    @Schema(description = "Reservation unit type", example = "NIGHT")
    private ReservationUnitType unitType;

    @Schema(description = "List of amenities", example = "[\"WiFi\", \"Ocean View\", \"Pool\"]")
    private List<String> amenities;

    @Schema(description = "List of rules", example = "[\"No smoking\", \"No pets\"]")
    private List<String> rules;

    @Schema(description = "List of image URLs", example = "[\"https://images.example.com/villa1.jpg\"]")
    private List<String> images;

    @Schema(description = "Whether the establishment is active for bookings", example = "true")
    private boolean active;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;

    public static EstablishmentResponse from(Establishment establishment) {
        if (establishment == null) {
            return null;
        }
        return EstablishmentResponse.builder()
                .id(establishment.getId())
                .hostId(establishment.getHostId())
                .name(establishment.getName())
                .description(establishment.getDescription())
                .category(establishment.getCategory())
                .address(AddressResponse.from(establishment.getAddress()))
                .contactPhone(establishment.getContactPhone())
                .contactEmail(establishment.getContactEmail())
                .capacity(establishment.getCapacity())
                .pricePerUnit(establishment.getPricePerUnit())
                .unitType(establishment.getUnitType())
                .amenities(establishment.getAmenities())
                .rules(establishment.getRules())
                .images(establishment.getImages())
                .active(establishment.isActive())
                .createdAt(establishment.getCreatedAt())
                .updatedAt(establishment.getUpdatedAt())
                .build();
    }
}
