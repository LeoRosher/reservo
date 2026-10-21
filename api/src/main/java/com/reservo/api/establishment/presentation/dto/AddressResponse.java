package com.reservo.api.establishment.presentation.dto;

import com.reservo.api.establishment.domain.model.Address;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Address response details")
public class AddressResponse {

    @Schema(description = "Street name and number", example = "123 Ocean Drive")
    private String street;

    @Schema(description = "City", example = "Miami")
    private String city;

    @Schema(description = "State or Province", example = "FL")
    private String state;

    @Schema(description = "Country", example = "USA")
    private String country;

    @Schema(description = "Zip or Postal Code", example = "33139")
    private String zipCode;

    @Schema(description = "Geographic latitude", example = "25.7617000")
    private BigDecimal latitude;

    @Schema(description = "Geographic longitude", example = "-80.1918000")
    private BigDecimal longitude;

    public static AddressResponse from(Address address) {
        if (address == null) {
            return null;
        }
        return AddressResponse.builder()
                .street(address.getStreet())
                .city(address.getCity())
                .state(address.getState())
                .country(address.getCountry())
                .zipCode(address.getZipCode())
                .latitude(address.getLatitude())
                .longitude(address.getLongitude())
                .build();
    }
}
