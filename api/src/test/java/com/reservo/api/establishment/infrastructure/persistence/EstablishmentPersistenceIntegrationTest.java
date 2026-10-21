package com.reservo.api.establishment.infrastructure.persistence;

import com.reservo.api.establishment.domain.model.Address;
import com.reservo.api.establishment.domain.model.Establishment;
import com.reservo.api.establishment.domain.model.EstablishmentCategory;
import com.reservo.api.establishment.domain.model.ReservationUnitType;
import com.reservo.api.establishment.domain.port.EstablishmentRepository;
import com.reservo.api.establishment.usecase.CreateEstablishmentUseCase;
import com.reservo.api.establishment.usecase.DeleteEstablishmentUseCase;
import com.reservo.api.establishment.usecase.UpdateEstablishmentUseCase;
import com.reservo.api.user.domain.model.User;
import com.reservo.api.user.domain.model.UserRole;
import com.reservo.api.user.domain.port.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class EstablishmentPersistenceIntegrationTest {

    @Autowired
    private EstablishmentRepository establishmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CreateEstablishmentUseCase createEstablishmentUseCase;

    @Autowired
    private UpdateEstablishmentUseCase updateEstablishmentUseCase;

    @Autowired
    private DeleteEstablishmentUseCase deleteEstablishmentUseCase;

    @Autowired
    private jakarta.persistence.EntityManager entityManager;

    private User savedHost;

    @BeforeEach
    void setUp() {
        User host = User.createNew(
                "Carlos Host",
                "carlos_" + UUID.randomUUID() + "@example.com",
                "hashedpassword",
                "+1234567890",
                UserRole.HOST
        );
        savedHost = userRepository.save(host);
    }

    @Test
    @DisplayName("Should persist and retrieve establishment with address, amenities, rules, and images in PostgreSQL")
    void shouldPersistAndRetrieveEstablishmentInDatabase() {
        // Arrange
        Address address = Address.builder()
                .street("Av. Corrientes 1234")
                .city("Buenos Aires")
                .state("CABA")
                .country("Argentina")
                .zipCode("C1043")
                .latitude(new BigDecimal("-34.6037000"))
                .longitude(new BigDecimal("-58.3816000"))
                .build();

        Establishment newEstablishment = Establishment.createNew(
                savedHost.getId(),
                "Palermo Studio Hub",
                "Modern studio in Palermo for work and events",
                EstablishmentCategory.STUDIO,
                address,
                "+54-11-4567-8901",
                "palermo@hub.com",
                8,
                new BigDecimal("45.50"),
                ReservationUnitType.HOUR,
                List.of("High Speed WiFi", "Projector", "Whiteboard"),
                List.of("No smoking inside", "Quiet hours after 10 PM"),
                List.of("https://res.cloudinary.com/demo/image1.jpg", "https://res.cloudinary.com/demo/image2.jpg")
        );

        // Act - Save into PostgreSQL database
        Establishment saved = establishmentRepository.save(newEstablishment);

        // Assert - Retrieve from PostgreSQL database
        Optional<Establishment> retrieved = establishmentRepository.findById(saved.getId());

        assertThat(retrieved).isPresent();
        Establishment found = retrieved.get();
        assertThat(found.getId()).isEqualTo(saved.getId());
        assertThat(found.getHostId()).isEqualTo(savedHost.getId());
        assertThat(found.getName()).isEqualTo("Palermo Studio Hub");
        assertThat(found.getDescription()).isEqualTo("Modern studio in Palermo for work and events");
        assertThat(found.getCategory()).isEqualTo(EstablishmentCategory.STUDIO);
        assertThat(found.getCapacity()).isEqualTo(8);
        assertThat(found.getPricePerUnit()).isEqualByComparingTo("45.50");
        assertThat(found.getUnitType()).isEqualTo(ReservationUnitType.HOUR);
        assertThat(found.isActive()).isTrue();

        // Address validation in DB
        assertThat(found.getAddress()).isNotNull();
        assertThat(found.getAddress().getStreet()).isEqualTo("Av. Corrientes 1234");
        assertThat(found.getAddress().getCity()).isEqualTo("Buenos Aires");
        assertThat(found.getAddress().getState()).isEqualTo("CABA");
        assertThat(found.getAddress().getCountry()).isEqualTo("Argentina");
        assertThat(found.getAddress().getZipCode()).isEqualTo("C1043");
        assertThat(found.getAddress().getLatitude()).isEqualByComparingTo("-34.6037000");
        assertThat(found.getAddress().getLongitude()).isEqualByComparingTo("-58.3816000");

        // Associated collections validation in DB
        assertThat(found.getAmenities()).containsExactlyInAnyOrder("High Speed WiFi", "Projector", "Whiteboard");
        assertThat(found.getRules()).containsExactlyInAnyOrder("No smoking inside", "Quiet hours after 10 PM");
        assertThat(found.getImages()).containsExactly(
                "https://res.cloudinary.com/demo/image1.jpg",
                "https://res.cloudinary.com/demo/image2.jpg"
        );
    }

    @Test
    @DisplayName("Should execute CreateEstablishmentUseCase end-to-end and persist to database")
    void shouldCreateEstablishmentViaUseCaseAndValidateInDatabase() {
        // Arrange
        CreateEstablishmentUseCase.Command command = new CreateEstablishmentUseCase.Command(
                savedHost.getId(),
                "Cordoba Mountain Lodge",
                "Relaxing lodge in the hills",
                EstablishmentCategory.CABIN,
                Address.builder().city("Villa General Belgrano").country("Argentina").build(),
                "+54-351-123456",
                "info@lodge.com",
                6,
                new BigDecimal("220.00"),
                ReservationUnitType.NIGHT,
                List.of("Fireplace", "Pool"),
                List.of("Pets allowed"),
                List.of("https://example.com/lodge1.jpg")
        );

        // Act
        Establishment created = createEstablishmentUseCase.execute(command);

        // Assert - Verify database state
        Optional<Establishment> inDb = establishmentRepository.findById(created.getId());
        assertThat(inDb).isPresent();
        assertThat(inDb.get().getName()).isEqualTo("Cordoba Mountain Lodge");
        assertThat(inDb.get().getHostId()).isEqualTo(savedHost.getId());
        assertThat(inDb.get().getCategory()).isEqualTo(EstablishmentCategory.CABIN);

        // Query by host
        List<Establishment> byHost = establishmentRepository.findByHostId(savedHost.getId());
        assertThat(byHost).extracting(Establishment::getName).contains("Cordoba Mountain Lodge");

        // Search query
        List<Establishment> searchResults = establishmentRepository.search(
                EstablishmentCategory.CABIN,
                "Belgrano",
                true
        );
        assertThat(searchResults).extracting(Establishment::getName).contains("Cordoba Mountain Lodge");
    }

    @Test
    @DisplayName("Should reject persisting establishment with invalid foreign key hostId at database constraint level")
    void shouldFailWhenHostIdDoesNotExistInUsersTableDueToForeignKeyConstraint() {
        // Arrange - random non-existent host UUID
        UUID nonExistentHostId = UUID.randomUUID();

        Establishment invalidEstablishment = Establishment.createNew(
                nonExistentHostId,
                "Orphan Establishment",
                "Description",
                EstablishmentCategory.OTHER,
                Address.builder().city("Unknown").country("Unknown").build(),
                null,
                null,
                1,
                new BigDecimal("10.00"),
                ReservationUnitType.DAY,
                null,
                null,
                null
        );

        // Act & Assert - database foreign key constraint violation
        assertThatThrownBy(() -> {
            establishmentRepository.save(invalidEstablishment);
            entityManager.flush();
        }).satisfies(thrown -> {
            boolean isConstraintError = thrown instanceof DataIntegrityViolationException
                    || thrown instanceof org.hibernate.exception.ConstraintViolationException
                    || thrown instanceof jakarta.persistence.PersistenceException;
            assertThat(isConstraintError).isTrue();
        });
    }

    @Test
    @DisplayName("Should execute UpdateEstablishmentUseCase and verify updated fields and collections in PostgreSQL")
    void shouldUpdateEstablishmentDetailsAndPersistInPostgres() {
        // Arrange - create initial establishment
        Establishment initial = establishmentRepository.save(Establishment.createNew(
                savedHost.getId(),
                "Original Loft",
                "Original Description",
                EstablishmentCategory.APARTMENT,
                Address.builder().city("Rosario").country("Argentina").build(),
                "+54-341-000000",
                "old@loft.com",
                2,
                new BigDecimal("50.00"),
                ReservationUnitType.NIGHT,
                List.of("WiFi"),
                List.of("No smoking"),
                List.of("https://example.com/initial.jpg")
        ));
        entityManager.flush();
        entityManager.clear();

        // Act - perform update via use case
        Address updatedAddress = Address.builder()
                .street("Boulevard Oroño 500")
                .city("Rosario")
                .state("Santa Fe")
                .country("Argentina")
                .zipCode("S2000")
                .build();

        UpdateEstablishmentUseCase.Command updateCommand = new UpdateEstablishmentUseCase.Command(
                initial.getId(),
                savedHost.getId(),
                "Luxury Rosario Penthouse",
                "Modern penthouse with river view",
                EstablishmentCategory.APARTMENT,
                updatedAddress,
                "+54-341-999999",
                "luxury@loft.com",
                6,
                new BigDecimal("150.00"),
                ReservationUnitType.DAY,
                List.of("WiFi", "River View", "Balcony"),
                List.of("No smoking", "No pets allowed"),
                List.of("https://example.com/penthouse1.jpg", "https://example.com/penthouse2.jpg")
        );

        Establishment updated = updateEstablishmentUseCase.execute(updateCommand);
        entityManager.flush();
        entityManager.clear();

        // Assert - verify database state
        Optional<Establishment> inDb = establishmentRepository.findById(updated.getId());
        assertThat(inDb).isPresent();
        Establishment found = inDb.get();

        assertThat(found.getName()).isEqualTo("Luxury Rosario Penthouse");
        assertThat(found.getDescription()).isEqualTo("Modern penthouse with river view");
        assertThat(found.getCapacity()).isEqualTo(6);
        assertThat(found.getPricePerUnit()).isEqualByComparingTo("150.00");
        assertThat(found.getUnitType()).isEqualTo(ReservationUnitType.DAY);
        assertThat(found.getContactPhone()).isEqualTo("+54-341-999999");
        assertThat(found.getContactEmail()).isEqualTo("luxury@loft.com");

        assertThat(found.getAddress().getStreet()).isEqualTo("Boulevard Oroño 500");
        assertThat(found.getAddress().getCity()).isEqualTo("Rosario");
        assertThat(found.getAddress().getState()).isEqualTo("Santa Fe");

        assertThat(found.getAmenities()).containsExactlyInAnyOrder("WiFi", "River View", "Balcony");
        assertThat(found.getRules()).containsExactlyInAnyOrder("No smoking", "No pets allowed");
        assertThat(found.getImages()).containsExactly(
                "https://example.com/penthouse1.jpg",
                "https://example.com/penthouse2.jpg"
        );
    }

    @Test
    @DisplayName("Should execute DeleteEstablishmentUseCase and verify soft delete in PostgreSQL")
    void shouldSoftDeleteEstablishmentAndFilterFromActiveSearchesInPostgres() {
        // Arrange - create initial establishment
        Establishment initial = establishmentRepository.save(Establishment.createNew(
                savedHost.getId(),
                "Place to be deleted",
                "Will be soft deleted",
                EstablishmentCategory.HOTEL,
                Address.builder().city("Mendoza").country("Argentina").build(),
                null,
                null,
                4,
                new BigDecimal("80.00"),
                ReservationUnitType.NIGHT,
                null,
                null,
                null
        ));
        entityManager.flush();
        entityManager.clear();

        // Act - soft delete via use case
        deleteEstablishmentUseCase.execute(new DeleteEstablishmentUseCase.Command(initial.getId(), savedHost.getId()));
        entityManager.flush();
        entityManager.clear();

        // Assert - record still exists in DB but active is false
        Optional<Establishment> inDb = establishmentRepository.findById(initial.getId());
        assertThat(inDb).isPresent();
        assertThat(inDb.get().isActive()).isFalse();

        // Assert - activeOnly search does NOT return it
        List<Establishment> activeSearchResults = establishmentRepository.search(
                EstablishmentCategory.HOTEL,
                "Mendoza",
                true
        );
        assertThat(activeSearchResults).extracting(Establishment::getId).doesNotContain(initial.getId());

        // Assert - search including inactive returns it
        List<Establishment> allSearchResults = establishmentRepository.search(
                EstablishmentCategory.HOTEL,
                "Mendoza",
                false
        );
        assertThat(allSearchResults).extracting(Establishment::getId).contains(initial.getId());
    }
}
