package com.reservo.api.reservation.infrastructure.persistence;

import com.reservo.api.establishment.domain.model.Address;
import com.reservo.api.establishment.domain.model.Establishment;
import com.reservo.api.establishment.domain.model.EstablishmentCategory;
import com.reservo.api.establishment.domain.model.ReservationUnitType;
import com.reservo.api.establishment.domain.port.EstablishmentRepository;
import com.reservo.api.reservation.domain.model.Reservation;
import com.reservo.api.reservation.domain.model.ReservationStatus;
import com.reservo.api.reservation.domain.port.ReservationRepository;
import com.reservo.api.user.domain.model.User;
import com.reservo.api.user.domain.model.UserRole;
import com.reservo.api.user.domain.port.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ReservationPersistenceIntegrationTest {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private EstablishmentRepository establishmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private jakarta.persistence.EntityManager entityManager;

    private User savedHost;
    private User savedCustomer;
    private Establishment savedEstablishment;

    @BeforeEach
    void setUp() {
        savedHost = userRepository.save(User.createNew(
                "Host User",
                "host_" + UUID.randomUUID() + "@example.com",
                "hash",
                "+1111",
                UserRole.HOST
        ));

        savedCustomer = userRepository.save(User.createNew(
                "Customer User",
                "customer_" + UUID.randomUUID() + "@example.com",
                "hash",
                "+2222",
                UserRole.CUSTOMER
        ));

        savedEstablishment = establishmentRepository.save(Establishment.createNew(
                savedHost.getId(),
                "Central Apartment",
                "Nice apartment",
                EstablishmentCategory.APARTMENT,
                Address.builder().city("Madrid").country("Spain").build(),
                "+34-91-000000",
                "central@madrid.com",
                4,
                new BigDecimal("80.00"),
                ReservationUnitType.NIGHT,
                List.of("WiFi"),
                List.of("No smoking"),
                List.of("https://example.com/img.jpg")
        ));

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("Should persist and retrieve reservation in PostgreSQL")
    void shouldPersistAndRetrieveReservationInPostgres() {
        LocalDateTime start = LocalDateTime.now().plusDays(2).withNano(0);
        LocalDateTime end = start.plusDays(3).withNano(0);

        Reservation newReservation = Reservation.createNew(
                savedCustomer.getId(),
                savedEstablishment.getId(),
                start,
                end,
                2,
                new BigDecimal("240.00")
        );

        Reservation saved = reservationRepository.save(newReservation);
        entityManager.flush();
        entityManager.clear();

        Optional<Reservation> foundOpt = reservationRepository.findById(saved.getId());
        assertThat(foundOpt).isPresent();
        Reservation found = foundOpt.get();

        assertThat(found.getCustomerId()).isEqualTo(savedCustomer.getId());
        assertThat(found.getEstablishmentId()).isEqualTo(savedEstablishment.getId());
        assertThat(found.getGuestsCount()).isEqualTo(2);
        assertThat(found.getTotalPrice()).isEqualByComparingTo("240.00");
        assertThat(found.getStatus()).isEqualTo(ReservationStatus.PENDING);
        assertThat(found.getStartDateTime()).isEqualTo(start);
        assertThat(found.getEndDateTime()).isEqualTo(end);
    }

    @Test
    @DisplayName("Should detect overlapping reservations accurately in PostgreSQL")
    void shouldDetectOverlappingReservations() {
        LocalDateTime start = LocalDateTime.now().plusDays(5).withNano(0);
        LocalDateTime end = start.plusDays(2).withNano(0);

        Reservation existing = reservationRepository.save(Reservation.createNew(
                savedCustomer.getId(),
                savedEstablishment.getId(),
                start,
                end,
                2,
                new BigDecimal("160.00")
        ));
        entityManager.flush();
        entityManager.clear();

        // Overlapping query: dates strictly overlap
        List<Reservation> overlapping = reservationRepository.findOverlappingReservations(
                savedEstablishment.getId(),
                start.plusHours(1),
                end.plusHours(1),
                List.of(ReservationStatus.PENDING, ReservationStatus.CONFIRMED)
        );
        assertThat(overlapping).extracting(Reservation::getId).contains(existing.getId());

        // Non-overlapping query: starts exactly when the previous ends
        List<Reservation> nonOverlapping = reservationRepository.findOverlappingReservations(
                savedEstablishment.getId(),
                end,
                end.plusDays(2),
                List.of(ReservationStatus.PENDING, ReservationStatus.CONFIRMED)
        );
        assertThat(nonOverlapping).isEmpty();
    }

    @Test
    @DisplayName("Should query reservations by customer and by establishment")
    void shouldQueryReservationsByCustomerAndEstablishment() {
        LocalDateTime start = LocalDateTime.now().plusDays(10).withNano(0);
        LocalDateTime end = start.plusDays(1).withNano(0);

        Reservation r = reservationRepository.save(Reservation.createNew(
                savedCustomer.getId(),
                savedEstablishment.getId(),
                start,
                end,
                1,
                new BigDecimal("80.00")
        ));
        entityManager.flush();
        entityManager.clear();

        List<Reservation> byCustomer = reservationRepository.findByCustomerId(savedCustomer.getId());
        assertThat(byCustomer).extracting(Reservation::getId).contains(r.getId());

        List<Reservation> byEstablishment = reservationRepository.findByEstablishmentId(savedEstablishment.getId());
        assertThat(byEstablishment).extracting(Reservation::getId).contains(r.getId());
    }
}
