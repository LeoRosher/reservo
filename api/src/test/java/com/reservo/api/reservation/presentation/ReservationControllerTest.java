package com.reservo.api.reservation.presentation;

import tools.jackson.databind.ObjectMapper;
import com.reservo.api.common.presentation.GlobalExceptionHandler;
import com.reservo.api.establishment.domain.exception.EstablishmentNotFoundException;
import com.reservo.api.reservation.domain.exception.ReservationConflictException;
import com.reservo.api.reservation.domain.exception.ReservationNotFoundException;
import com.reservo.api.reservation.domain.model.Reservation;
import com.reservo.api.reservation.domain.model.ReservationStatus;
import com.reservo.api.reservation.presentation.dto.CancelReservationRequest;
import com.reservo.api.reservation.presentation.dto.ConfirmReservationRequest;
import com.reservo.api.reservation.presentation.dto.CreateReservationRequest;
import com.reservo.api.reservation.usecase.CancelReservationUseCase;
import com.reservo.api.reservation.usecase.ConfirmReservationUseCase;
import com.reservo.api.reservation.usecase.CreateReservationUseCase;
import com.reservo.api.reservation.usecase.GetReservationByIdUseCase;
import com.reservo.api.reservation.usecase.ListUserReservationsUseCase;
import com.reservo.api.user.domain.exception.UserNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReservationController.class)
@Import(GlobalExceptionHandler.class)
class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CreateReservationUseCase createReservationUseCase;

    @MockitoBean
    private GetReservationByIdUseCase getReservationByIdUseCase;

    @MockitoBean
    private ListUserReservationsUseCase listUserReservationsUseCase;

    @MockitoBean
    private ConfirmReservationUseCase confirmReservationUseCase;

    @MockitoBean
    private CancelReservationUseCase cancelReservationUseCase;

    @Test
    @DisplayName("POST /api/v1/reservations should return 201 Created when request is valid")
    void shouldReturn201CreatedWhenReservationIsValid() throws Exception {
        UUID reservationId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID establishmentId = UUID.randomUUID();

        LocalDateTime start = LocalDateTime.now().plusDays(2);
        LocalDateTime end = start.plusHours(3);

        Reservation reservation = Reservation.builder()
                .id(reservationId)
                .customerId(customerId)
                .establishmentId(establishmentId)
                .startDateTime(start)
                .endDateTime(end)
                .guestsCount(2)
                .totalPrice(new BigDecimal("150.00"))
                .status(ReservationStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(createReservationUseCase.execute(any())).thenReturn(reservation);

        CreateReservationRequest request = CreateReservationRequest.builder()
                .customerId(customerId)
                .establishmentId(establishmentId)
                .startDateTime(start)
                .endDateTime(end)
                .guestsCount(2)
                .build();

        mockMvc.perform(post("/api/v1/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/v1/reservations/" + reservationId)))
                .andExpect(jsonPath("$.id").value(reservationId.toString()))
                .andExpect(jsonPath("$.customerId").value(customerId.toString()))
                .andExpect(jsonPath("$.establishmentId").value(establishmentId.toString()))
                .andExpect(jsonPath("$.totalPrice").value(150.00))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @DisplayName("POST /api/v1/reservations should return 400 Bad Request when required fields are missing")
    void shouldReturn400BadRequestWhenPayloadIsInvalid() throws Exception {
        CreateReservationRequest invalidRequest = CreateReservationRequest.builder()
                .customerId(null)
                .establishmentId(null)
                .startDateTime(null)
                .endDateTime(null)
                .guestsCount(0)
                .build();

        mockMvc.perform(post("/api/v1/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("POST /api/v1/reservations should return 404 Not Found when customer does not exist")
    void shouldReturn404NotFoundWhenCustomerDoesNotExist() throws Exception {
        UUID customerId = UUID.randomUUID();
        when(createReservationUseCase.execute(any()))
                .thenThrow(new UserNotFoundException(customerId));

        CreateReservationRequest request = CreateReservationRequest.builder()
                .customerId(customerId)
                .establishmentId(UUID.randomUUID())
                .startDateTime(LocalDateTime.now().plusDays(1))
                .endDateTime(LocalDateTime.now().plusDays(1).plusHours(2))
                .guestsCount(1)
                .build();

        mockMvc.perform(post("/api/v1/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("POST /api/v1/reservations should return 404 Not Found when establishment does not exist")
    void shouldReturn404NotFoundWhenEstablishmentDoesNotExist() throws Exception {
        UUID establishmentId = UUID.randomUUID();
        when(createReservationUseCase.execute(any()))
                .thenThrow(new EstablishmentNotFoundException(establishmentId));

        CreateReservationRequest request = CreateReservationRequest.builder()
                .customerId(UUID.randomUUID())
                .establishmentId(establishmentId)
                .startDateTime(LocalDateTime.now().plusDays(1))
                .endDateTime(LocalDateTime.now().plusDays(1).plusHours(2))
                .guestsCount(1)
                .build();

        mockMvc.perform(post("/api/v1/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ESTABLISHMENT_NOT_FOUND"))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("POST /api/v1/reservations should return 409 Conflict when there is a booking conflict")
    void shouldReturn409ConflictWhenThereIsBookingConflict() throws Exception {
        when(createReservationUseCase.execute(any()))
                .thenThrow(new ReservationConflictException("Establishment is not available for the selected dates."));

        CreateReservationRequest request = CreateReservationRequest.builder()
                .customerId(UUID.randomUUID())
                .establishmentId(UUID.randomUUID())
                .startDateTime(LocalDateTime.now().plusDays(1))
                .endDateTime(LocalDateTime.now().plusDays(1).plusHours(2))
                .guestsCount(1)
                .build();

        mockMvc.perform(post("/api/v1/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("RESERVATION_CONFLICT"))
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("GET /api/v1/reservations/{id} should return 200 OK when reservation exists")
    void shouldReturn200OkWhenReservationExists() throws Exception {
        UUID reservationId = UUID.randomUUID();
        Reservation reservation = Reservation.builder()
                .id(reservationId)
                .customerId(UUID.randomUUID())
                .establishmentId(UUID.randomUUID())
                .startDateTime(LocalDateTime.now().plusDays(1))
                .endDateTime(LocalDateTime.now().plusDays(1).plusHours(2))
                .guestsCount(2)
                .totalPrice(new BigDecimal("99.00"))
                .status(ReservationStatus.PENDING)
                .build();

        when(getReservationByIdUseCase.execute(reservationId)).thenReturn(reservation);

        mockMvc.perform(get("/api/v1/reservations/{id}", reservationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reservationId.toString()))
                .andExpect(jsonPath("$.totalPrice").value(99.00));
    }

    @Test
    @DisplayName("GET /api/v1/reservations/{id} should return 404 Not Found when reservation does not exist")
    void shouldReturn404NotFoundWhenReservationDoesNotExist() throws Exception {
        UUID reservationId = UUID.randomUUID();
        when(getReservationByIdUseCase.execute(reservationId))
                .thenThrow(new ReservationNotFoundException(reservationId));

        mockMvc.perform(get("/api/v1/reservations/{id}", reservationId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESERVATION_NOT_FOUND"))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("GET /api/v1/reservations should return 200 OK with list filtered by customerId")
    void shouldReturnReservationsListByCustomer() throws Exception {
        UUID customerId = UUID.randomUUID();
        Reservation r = Reservation.builder()
                .id(UUID.randomUUID())
                .customerId(customerId)
                .establishmentId(UUID.randomUUID())
                .status(ReservationStatus.CONFIRMED)
                .build();

        when(listUserReservationsUseCase.executeByCustomer(customerId)).thenReturn(List.of(r));

        mockMvc.perform(get("/api/v1/reservations").param("customerId", customerId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].customerId").value(customerId.toString()));
    }

    @Test
    @DisplayName("GET /api/v1/reservations should return 400 Bad Request when neither customerId nor establishmentId is provided")
    void shouldReturn400BadRequestWhenNoFilterProvided() throws Exception {
        mockMvc.perform(get("/api/v1/reservations"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_ARGUMENT"));
    }

    @Test
    @DisplayName("POST /api/v1/reservations/{id}/confirm should return 200 OK on successful confirmation")
    void shouldConfirmReservationSuccessfully() throws Exception {
        UUID reservationId = UUID.randomUUID();
        UUID hostId = UUID.randomUUID();

        Reservation confirmed = Reservation.builder()
                .id(reservationId)
                .status(ReservationStatus.CONFIRMED)
                .build();

        when(confirmReservationUseCase.execute(any())).thenReturn(confirmed);

        ConfirmReservationRequest request = ConfirmReservationRequest.builder().hostId(hostId).build();

        mockMvc.perform(post("/api/v1/reservations/{id}/confirm", reservationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reservationId.toString()))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    @DisplayName("POST /api/v1/reservations/{id}/cancel should return 200 OK on successful cancellation")
    void shouldCancelReservationSuccessfully() throws Exception {
        UUID reservationId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Reservation cancelled = Reservation.builder()
                .id(reservationId)
                .status(ReservationStatus.CANCELLED)
                .cancellationReason("Change of plans")
                .build();

        when(cancelReservationUseCase.execute(any())).thenReturn(cancelled);

        CancelReservationRequest request = CancelReservationRequest.builder()
                .userId(userId)
                .reason("Change of plans")
                .build();

        mockMvc.perform(post("/api/v1/reservations/{id}/cancel", reservationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reservationId.toString()))
                .andExpect(jsonPath("$.status").value("CANCELLED"))
                .andExpect(jsonPath("$.cancellationReason").value("Change of plans"));
    }
}
