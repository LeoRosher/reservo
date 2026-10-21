package com.reservo.api.establishment.presentation.dto;

import com.reservo.api.establishment.domain.model.Address;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Address details for an establishment")
public class AddressRequest {

    @Schema(description = "Street name and number", example = "123 Ocean Drive")
    private String street;

    @NotBlank(message = "City is required")
    @Schema(description = "City", example = "Miami")
    private String city;

    @Schema(description = "State or Province", example = "FL")
    private String state;

    @NotBlank(message = "Country is required")
    @Schema(description = "Country", example = "USA")
    private String country;

    @Schema(description = "Zip or Postal Code", example = "33139")
    private String zipCode;

    @Schema(description = "Geographic latitude", example = "25.7617000")
    private BigDecimal latitude;

    @Schema(description = "Geographic longitude", example = "-80.1918000")
    private BigDecimal longitude;

    public Address toDomain() {
        return Address.builder()
                .street(this.street)
                .city(this.city)
                .state(this.state)
                .country(this.country)
                .zipCode(this.zipCode)
                .latitude(this.latitude)
                .longitude(this.longitude)
                .build();
    }
}
