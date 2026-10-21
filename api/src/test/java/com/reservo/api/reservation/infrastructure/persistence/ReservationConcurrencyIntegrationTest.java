package com.reservo.api.reservation.infrastructure.persistence;

import com.reservo.api.establishment.domain.model.Address;
import com.reservo.api.establishment.domain.model.Establishment;
import com.reservo.api.establishment.domain.model.EstablishmentCategory;
import com.reservo.api.establishment.domain.model.ReservationUnitType;
import com.reservo.api.establishment.domain.port.EstablishmentRepository;
import com.reservo.api.reservation.domain.exception.ReservationConflictException;
import com.reservo.api.reservation.domain.model.Reservation;
import com.reservo.api.reservation.domain.model.ReservationStatus;
import com.reservo.api.reservation.domain.port.ReservationRepository;
import com.reservo.api.reservation.usecase.CreateReservationUseCase;
import com.reservo.api.user.domain.model.User;
import com.reservo.api.user.domain.model.UserRole;
import com.reservo.api.user.domain.port.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ReservationConcurrencyIntegrationTest {

    @Autowired
    private CreateReservationUseCase createReservationUseCase;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private EstablishmentRepository establishmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SpringDataReservationRepository springDataReservationRepository;

    private User host;
    private User customer1;
    private User customer2;
    private Establishment establishment;

    @BeforeEach
    void setUp() {
        host = userRepository.save(User.createNew(
                "Host Concurrency",
                "host_conc_" + UUID.randomUUID() + "@example.com",
                "passwordHash",
                "+1000",
                UserRole.HOST
        ));

        customer1 = userRepository.save(User.createNew(
                "Customer Concurrency 1",
                "customer1_conc_" + UUID.randomUUID() + "@example.com",
                "passwordHash",
                "+2000",
                UserRole.CUSTOMER
        ));

        customer2 = userRepository.save(User.createNew(
                "Customer Concurrency 2",
                "customer2_conc_" + UUID.randomUUID() + "@example.com",
                "passwordHash",
                "+3000",
                UserRole.CUSTOMER
        ));

        establishment = establishmentRepository.save(Establishment.createNew(
                host.getId(),
                "Exclusive Penthouse",
                "High demand venue",
                EstablishmentCategory.APARTMENT,
                Address.builder().city("New York").country("USA").build(),
                "+1-212-000000",
                "penthouse@ny.com",
                6,
                new BigDecimal("500.00"),
                ReservationUnitType.NIGHT,
                List.of("WiFi", "Terrace"),
                List.of("No smoking"),
                List.of("https://example.com/ny.jpg")
        ));
    }

    @AfterEach
    void tearDown() {
        springDataReservationRepository.deleteAll();
    }

    @Test
    @DisplayName("Should prevent double booking when two concurrent requests try to reserve the same overlapping time interval")
    void shouldPreventDoubleBookingUnderConcurrentRequests() throws Exception {
        LocalDateTime start = LocalDateTime.now().plusDays(20).withNano(0);
        LocalDateTime end = start.plusDays(3).withNano(0);

        int threads = 2;
        ExecutorService executorService = Executors.newFixedThreadPool(threads);
        CountDownLatch readyLatch = new CountDownLatch(threads);
        CountDownLatch startLatch = new CountDownLatch(1);

        List<Throwable> errors = Collections.synchronizedList(new ArrayList<>());
        List<Reservation> successfulReservations = Collections.synchronizedList(new ArrayList<>());

        CreateReservationUseCase.Command command1 = new CreateReservationUseCase.Command(
                customer1.getId(),
                establishment.getId(),
                start,
                end,
                2
        );

        CreateReservationUseCase.Command command2 = new CreateReservationUseCase.Command(
                customer2.getId(),
                establishment.getId(),
                start,
                end,
                3
        );

        Future<?> f1 = executorService.submit(() -> {
            try {
                readyLatch.countDown();
                startLatch.await();
                Reservation r = createReservationUseCase.execute(command1);
                successfulReservations.add(r);
            } catch (Throwable t) {
                errors.add(t);
            }
        });

        Future<?> f2 = executorService.submit(() -> {
            try {
                readyLatch.countDown();
                startLatch.await();
                Reservation r = createReservationUseCase.execute(command2);
                successfulReservations.add(r);
            } catch (Throwable t) {
                errors.add(t);
            }
        });

        // Wait for both threads to be ready at the gate
        assertThat(readyLatch.await(5, TimeUnit.SECONDS)).isTrue();
        // Release the gate: both execute concurrently!
        startLatch.countDown();

        f1.get(10, TimeUnit.SECONDS);
        f2.get(10, TimeUnit.SECONDS);
        executorService.shutdown();

        // 1. Exactly one request must succeed
        assertThat(successfulReservations)
                .as("Exactly one concurrent reservation must succeed")
                .hasSize(1);

        // 2. Exactly one request must fail with a conflict error
        assertThat(errors)
                .as("Exactly one concurrent reservation must fail with a conflict")
                .hasSize(1);

        Throwable error = errors.get(0);
        boolean isConflict = error instanceof ReservationConflictException
                || error.getCause() instanceof ReservationConflictException
                || error instanceof org.springframework.dao.DataAccessException;
        assertThat(isConflict)
                .as("The failure must be due to booking conflict")
                .isTrue();

        // 3. Verify in PostgreSQL that there is ONLY 1 reservation saved in DB
        List<Reservation> persistedReservations = reservationRepository.findOverlappingReservations(
                establishment.getId(),
                start,
                end,
                List.of(ReservationStatus.PENDING, ReservationStatus.CONFIRMED)
        );

        assertThat(persistedReservations)
                .as("PostgreSQL database must contain exactly one reservation for the requested time range")
                .hasSize(1);
    }
}
