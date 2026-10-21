package com.reservo.api.establishment.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EstablishmentTest {

    @Test
    @DisplayName("Should successfully update establishment details and refresh updatedAt timestamp")
    void shouldUpdateDetailsSuccessfully() {
        UUID hostId = UUID.randomUUID();
        Address initialAddress = Address.builder().city("Boston").country("USA").build();
        Establishment establishment = Establishment.createNew(
                hostId,
                "Old Name",
                "Old Description",
                EstablishmentCategory.HOTEL,
                initialAddress,
                "+1-111-222333",
                "old@hotel.com",
                2,
                new BigDecimal("100.00"),
                ReservationUnitType.NIGHT,
                List.of("WiFi"),
                List.of("No smoking"),
                List.of("https://example.com/img1.jpg")
        );

        Address newAddress = Address.builder().street("100 New St").city("New York").country("USA").build();

        establishment.updateDetails(
                "New Name",
                "New Description",
                EstablishmentCategory.APARTMENT,
                newAddress,
                "+1-444-555666",
                "new@hotel.com",
                5,
                new BigDecimal("250.00"),
                ReservationUnitType.DAY,
                List.of("WiFi", "Gym"),
                List.of("No parties"),
                List.of("https://example.com/new1.jpg", "https://example.com/new2.jpg")
        );

        assertThat(establishment.getName()).isEqualTo("New Name");
        assertThat(establishment.getDescription()).isEqualTo("New Description");
        assertThat(establishment.getCategory()).isEqualTo(EstablishmentCategory.APARTMENT);
        assertThat(establishment.getAddress()).isEqualTo(newAddress);
        assertThat(establishment.getContactPhone()).isEqualTo("+1-444-555666");
        assertThat(establishment.getContactEmail()).isEqualTo("new@hotel.com");
        assertThat(establishment.getCapacity()).isEqualTo(5);
        assertThat(establishment.getPricePerUnit()).isEqualTo(new BigDecimal("250.00"));
        assertThat(establishment.getUnitType()).isEqualTo(ReservationUnitType.DAY);
        assertThat(establishment.getAmenities()).containsExactly("WiFi", "Gym");
        assertThat(establishment.getRules()).containsExactly("No parties");
        assertThat(establishment.getImages()).containsExactly("https://example.com/new1.jpg", "https://example.com/new2.jpg");
        assertThat(establishment.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when updating with blank name")
    void shouldThrowWhenUpdatingWithBlankName() {
        Establishment establishment = createSampleEstablishment();
        Address address = Address.builder().city("City").country("Country").build();

        assertThatThrownBy(() -> establishment.updateDetails(
                "   ",
                "Desc",
                EstablishmentCategory.HOTEL,
                address,
                null,
                null,
                2,
                BigDecimal.TEN,
                ReservationUnitType.NIGHT,
                null,
                null,
                null
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Establishment name cannot be blank.");
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when updating with null category")
    void shouldThrowWhenUpdatingWithNullCategory() {
        Establishment establishment = createSampleEstablishment();
        Address address = Address.builder().city("City").country("Country").build();

        assertThatThrownBy(() -> establishment.updateDetails(
                "Valid Name",
                "Desc",
                null,
                address,
                null,
                null,
                2,
                BigDecimal.TEN,
                ReservationUnitType.NIGHT,
                null,
                null,
                null
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Establishment category cannot be null.");
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when updating with null address")
    void shouldThrowWhenUpdatingWithNullAddress() {
        Establishment establishment = createSampleEstablishment();

        assertThatThrownBy(() -> establishment.updateDetails(
                "Valid Name",
                "Desc",
                EstablishmentCategory.HOTEL,
                null,
                null,
                null,
                2,
                BigDecimal.TEN,
                ReservationUnitType.NIGHT,
                null,
                null,
                null
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Establishment address cannot be null.");
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when updating with capacity <= 0")
    void shouldThrowWhenUpdatingWithInvalidCapacity() {
        Establishment establishment = createSampleEstablishment();
        Address address = Address.builder().city("City").country("Country").build();

        assertThatThrownBy(() -> establishment.updateDetails(
                "Valid Name",
                "Desc",
                EstablishmentCategory.HOTEL,
                address,
                null,
                null,
                0,
                BigDecimal.TEN,
                ReservationUnitType.NIGHT,
                null,
                null,
                null
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Establishment capacity must be greater than zero.");
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when updating with negative price")
    void shouldThrowWhenUpdatingWithNegativePrice() {
        Establishment establishment = createSampleEstablishment();
        Address address = Address.builder().city("City").country("Country").build();

        assertThatThrownBy(() -> establishment.updateDetails(
                "Valid Name",
                "Desc",
                EstablishmentCategory.HOTEL,
                address,
                null,
                null,
                2,
                new BigDecimal("-5.00"),
                ReservationUnitType.NIGHT,
                null,
                null,
                null
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Price per unit must be non-negative.");
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when updating with null unitType")
    void shouldThrowWhenUpdatingWithNullUnitType() {
        Establishment establishment = createSampleEstablishment();
        Address address = Address.builder().city("City").country("Country").build();

        assertThatThrownBy(() -> establishment.updateDetails(
                "Valid Name",
                "Desc",
                EstablishmentCategory.HOTEL,
                address,
                null,
                null,
                2,
                BigDecimal.TEN,
                null,
                null,
                null,
                null
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Reservation unit type cannot be null.");
    }

    @Test
    @DisplayName("Should deactivate and activate establishment properly")
    void shouldDeactivateAndActivate() {
        Establishment establishment = createSampleEstablishment();
        assertThat(establishment.isActive()).isTrue();

        establishment.deactivate();
        assertThat(establishment.isActive()).isFalse();

        establishment.activate();
        assertThat(establishment.isActive()).isTrue();
    }

    @Test
    @DisplayName("Should verify whether establishment belongs to host")
    void shouldVerifyBelongsToHost() {
        UUID hostId = UUID.randomUUID();
        UUID otherId = UUID.randomUUID();
        Establishment establishment = Establishment.builder().hostId(hostId).build();

        assertThat(establishment.belongsToHost(hostId)).isTrue();
        assertThat(establishment.belongsToHost(otherId)).isFalse();
        assertThat(establishment.belongsToHost(null)).isFalse();
    }

    private Establishment createSampleEstablishment() {
        return Establishment.createNew(
                UUID.randomUUID(),
                "Sample Name",
                "Sample Desc",
                EstablishmentCategory.HOTEL,
                Address.builder().city("City").country("Country").build(),
                null,
                null,
                2,
                BigDecimal.TEN,
                ReservationUnitType.NIGHT,
                null,
                null,
                null
        );
    }
}
