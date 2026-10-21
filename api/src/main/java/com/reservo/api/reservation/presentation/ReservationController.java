package com.reservo.api.reservation.presentation;

import com.reservo.api.common.presentation.ErrorResponse;
import com.reservo.api.reservation.domain.model.Reservation;
import com.reservo.api.reservation.presentation.dto.CancelReservationRequest;
import com.reservo.api.reservation.presentation.dto.ConfirmReservationRequest;
import com.reservo.api.reservation.presentation.dto.CreateReservationRequest;
import com.reservo.api.reservation.presentation.dto.ReservationResponse;
import com.reservo.api.reservation.usecase.CancelReservationUseCase;
import com.reservo.api.reservation.usecase.ConfirmReservationUseCase;
import com.reservo.api.reservation.usecase.CreateReservationUseCase;
import com.reservo.api.reservation.usecase.GetReservationByIdUseCase;
import com.reservo.api.reservation.usecase.ListUserReservationsUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reservations")
@Tag(name = "Reservations", description = "Endpoints for managing bookings and reservations")
public class ReservationController {

    private final CreateReservationUseCase createReservationUseCase;
    private final GetReservationByIdUseCase getReservationByIdUseCase;
    private final ListUserReservationsUseCase listUserReservationsUseCase;
    private final ConfirmReservationUseCase confirmReservationUseCase;
    private final CancelReservationUseCase cancelReservationUseCase;

    public ReservationController(
            CreateReservationUseCase createReservationUseCase,
            GetReservationByIdUseCase getReservationByIdUseCase,
            ListUserReservationsUseCase listUserReservationsUseCase,
            ConfirmReservationUseCase confirmReservationUseCase,
            CancelReservationUseCase cancelReservationUseCase
    ) {
        this.createReservationUseCase = createReservationUseCase;
        this.getReservationByIdUseCase = getReservationByIdUseCase;
        this.listUserReservationsUseCase = listUserReservationsUseCase;
        this.confirmReservationUseCase = confirmReservationUseCase;
        this.cancelReservationUseCase = cancelReservationUseCase;
    }

    @PostMapping
    @Operation(
            summary = "Create a new reservation",
            description = "Creates a new reservation for an establishment with concurrency checks to avoid overlapping bookings."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Reservation created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ReservationResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation failed, invalid reservation dates, or capacity exceeded",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Customer user or establishment not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Booking conflict: dates already reserved or establishment inactive",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<ReservationResponse> createReservation(
            @Valid @RequestBody CreateReservationRequest request
    ) {
        Reservation created = createReservationUseCase.execute(request.toCommand());

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();

        return ResponseEntity.created(location).body(ReservationResponse.from(created));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get reservation by ID",
            description = "Retrieves the reservation details by its unique identifier."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Reservation retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ReservationResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Reservation not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<ReservationResponse> getReservationById(@PathVariable UUID id) {
        Reservation reservation = getReservationByIdUseCase.execute(id);
        return ResponseEntity.ok(ReservationResponse.from(reservation));
    }

    @GetMapping
    @Operation(
            summary = "List reservations by customer or establishment",
            description = "Retrieves all reservations for a given customer ID or establishment ID."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Reservations list retrieved successfully",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ReservationResponse.class)))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Neither customerId nor establishmentId was provided",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<List<ReservationResponse>> listReservations(
            @Parameter(description = "Customer ID to filter reservations")
            @RequestParam(required = false) UUID customerId,
            @Parameter(description = "Establishment ID to filter reservations")
            @RequestParam(required = false) UUID establishmentId
    ) {
        List<Reservation> reservations;
        if (customerId != null) {
            reservations = listUserReservationsUseCase.executeByCustomer(customerId);
        } else if (establishmentId != null) {
            reservations = listUserReservationsUseCase.executeByEstablishment(establishmentId);
        } else {
            throw new IllegalArgumentException("Either customerId or establishmentId query parameter must be provided.");
        }

        List<ReservationResponse> responses = reservations.stream()
                .map(ReservationResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/{id}/confirm")
    @Operation(
            summary = "Confirm reservation",
            description = "Allows an authorized host to confirm a pending reservation."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Reservation confirmed successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ReservationResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Host is not authorized or invalid argument",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Reservation or establishment not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Reservation is not in PENDING state",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<ReservationResponse> confirmReservation(
            @PathVariable UUID id,
            @Valid @RequestBody ConfirmReservationRequest request
    ) {
        Reservation confirmed = confirmReservationUseCase.execute(
                new ConfirmReservationUseCase.Command(id, request.getHostId())
        );
        return ResponseEntity.ok(ReservationResponse.from(confirmed));
    }

    @PostMapping("/{id}/cancel")
    @Operation(
            summary = "Cancel reservation",
            description = "Allows a customer or host to cancel a reservation."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Reservation cancelled successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ReservationResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "User not authorized to cancel or invalid request",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Reservation or establishment not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Reservation is already cancelled or completed",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<ReservationResponse> cancelReservation(
            @PathVariable UUID id,
            @Valid @RequestBody CancelReservationRequest request
    ) {
        Reservation cancelled = cancelReservationUseCase.execute(
                new CancelReservationUseCase.Command(id, request.getUserId(), request.getReason())
        );
        return ResponseEntity.ok(ReservationResponse.from(cancelled));
    }
}
