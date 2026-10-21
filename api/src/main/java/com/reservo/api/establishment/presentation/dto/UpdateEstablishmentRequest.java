package com.reservo.api.establishment.presentation.dto;

import com.reservo.api.establishment.domain.model.EstablishmentCategory;
import com.reservo.api.establishment.domain.model.ReservationUnitType;
import com.reservo.api.establishment.usecase.UpdateEstablishmentUseCase;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body to update an existing establishment")
public class UpdateEstablishmentRequest {

    @NotNull(message = "Host ID is required")
    @Schema(description = "ID of the host user attempting to update the establishment", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID hostId;

    @NotBlank(message = "Establishment name is required")
    @Size(max = 200, message = "Establishment name must not exceed 200 characters")
    @Schema(description = "Name of the establishment", example = "Sunset Beach Villa")
    private String name;

    @Schema(description = "Detailed description of the property", example = "Luxurious villa located right in front of the beach")
    private String description;

    @NotNull(message = "Category is required")
    @Schema(description = "Category of the place", example = "APARTMENT")
    private EstablishmentCategory category;

    @NotNull(message = "Address is required")
    @Valid
    @Schema(description = "Physical address of the establishment")
    private AddressRequest address;

    @Schema(description = "Contact phone number", example = "+1-555-9876543")
    private String contactPhone;

    @Email(message = "Contact email must be a valid email address")
    @Schema(description = "Contact email address", example = "sunset@villa.com")
    private String contactEmail;

    @Min(value = 1, message = "Capacity must be at least 1")
    @Schema(description = "Maximum guest capacity", example = "4")
    private int capacity;

    @NotNull(message = "Price per unit is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Price per unit must be greater than or equal to 0")
    @Schema(description = "Price per reservation unit", example = "150.00")
    private BigDecimal pricePerUnit;

    @NotNull(message = "Reservation unit type is required")
    @Schema(description = "Unit type for reservation pricing (HOUR, DAY, NIGHT, etc.)", example = "NIGHT")
    private ReservationUnitType unitType;

    @Builder.Default
    @Schema(description = "List of amenities provided", example = "[\"WiFi\", \"Ocean View\", \"Pool\"]")
    private List<String> amenities = new ArrayList<>();

    @Builder.Default
    @Schema(description = "List of house rules", example = "[\"No smoking\", \"No pets\"]")
    private List<String> rules = new ArrayList<>();

    @Builder.Default
    @Schema(description = "List of image URLs", example = "[\"https://images.example.com/villa1.jpg\"]")
    private List<String> images = new ArrayList<>();

    public UpdateEstablishmentUseCase.Command toCommand(UUID establishmentId) {
        return toCommand(establishmentId, this.hostId);
    }

    public UpdateEstablishmentUseCase.Command toCommand(UUID establishmentId, UUID effectiveHostId) {
        return new UpdateEstablishmentUseCase.Command(
                establishmentId,
                effectiveHostId != null ? effectiveHostId : this.hostId,
                this.name,
                this.description,
                this.category,
                this.address != null ? this.address.toDomain() : null,
                this.contactPhone,
                this.contactEmail,
                this.capacity,
                this.pricePerUnit,
                this.unitType,
                this.amenities,
                this.rules,
                this.images
        );
    }
}
