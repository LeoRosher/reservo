package com.reservo.api.establishment.presentation;

import com.reservo.api.common.presentation.ErrorResponse;
import com.reservo.api.establishment.domain.model.Establishment;
import com.reservo.api.establishment.domain.model.EstablishmentCategory;
import com.reservo.api.establishment.presentation.dto.CreateEstablishmentRequest;
import com.reservo.api.establishment.presentation.dto.EstablishmentResponse;
import com.reservo.api.establishment.presentation.dto.UpdateEstablishmentRequest;
import com.reservo.api.establishment.usecase.CreateEstablishmentUseCase;
import com.reservo.api.establishment.usecase.DeleteEstablishmentUseCase;
import com.reservo.api.establishment.usecase.GetEstablishmentByIdUseCase;
import com.reservo.api.establishment.usecase.ListEstablishmentsUseCase;
import com.reservo.api.establishment.usecase.UpdateEstablishmentUseCase;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/establishments")
@Tag(name = "Establishments", description = "Endpoints for managing establishments and rental places")
public class EstablishmentController {

    private final CreateEstablishmentUseCase createEstablishmentUseCase;
    private final GetEstablishmentByIdUseCase getEstablishmentByIdUseCase;
    private final ListEstablishmentsUseCase listEstablishmentsUseCase;
    private final UpdateEstablishmentUseCase updateEstablishmentUseCase;
    private final DeleteEstablishmentUseCase deleteEstablishmentUseCase;

    public EstablishmentController(
            CreateEstablishmentUseCase createEstablishmentUseCase,
            GetEstablishmentByIdUseCase getEstablishmentByIdUseCase,
            ListEstablishmentsUseCase listEstablishmentsUseCase,
            UpdateEstablishmentUseCase updateEstablishmentUseCase,
            DeleteEstablishmentUseCase deleteEstablishmentUseCase
    ) {
        this.createEstablishmentUseCase = createEstablishmentUseCase;
        this.getEstablishmentByIdUseCase = getEstablishmentByIdUseCase;
        this.listEstablishmentsUseCase = listEstablishmentsUseCase;
        this.updateEstablishmentUseCase = updateEstablishmentUseCase;
        this.deleteEstablishmentUseCase = deleteEstablishmentUseCase;
    }

    @PostMapping
    @Operation(
            summary = "Create a new establishment",
            description = "Creates a new establishment for an authorized host user and persists it into the database."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Establishment created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = EstablishmentResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation error or malformed request payload",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "User is not authorized as a host",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Host user not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<EstablishmentResponse> createEstablishment(
            @Valid @RequestBody CreateEstablishmentRequest request
    ) {
        Establishment created = createEstablishmentUseCase.execute(request.toCommand());

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();

        return ResponseEntity.created(location).body(EstablishmentResponse.from(created));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get establishment by ID",
            description = "Retrieves an establishment and its full details by its unique identifier."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Establishment retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = EstablishmentResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Establishment not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<EstablishmentResponse> getEstablishmentById(@PathVariable UUID id) {
        Establishment establishment = getEstablishmentByIdUseCase.execute(id);
        return ResponseEntity.ok(EstablishmentResponse.from(establishment));
    }

    @GetMapping
    @Operation(
            summary = "Search or list establishments",
            description = "Retrieves a list of establishments matching optional filters (category, city, active status)."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Establishments retrieved successfully",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = EstablishmentResponse.class)))
            )
    })
    public ResponseEntity<List<EstablishmentResponse>> listEstablishments(
            @Parameter(description = "Filter by establishment category")
            @RequestParam(required = false) EstablishmentCategory category,
            @Parameter(description = "Filter by city name (case-insensitive partial match)")
            @RequestParam(required = false) String city,
            @Parameter(description = "Filter by active status (defaults to true)")
            @RequestParam(required = false, defaultValue = "true") Boolean activeOnly
    ) {
        List<Establishment> establishments = listEstablishmentsUseCase.execute(
                new ListEstablishmentsUseCase.Query(category, city, activeOnly)
        );
        List<EstablishmentResponse> responseList = establishments.stream()
                .map(EstablishmentResponse::from)
                .toList();
        return ResponseEntity.ok(responseList);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update establishment",
            description = "Updates the establishment details. The caller host must be the owner of the establishment."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Establishment updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = EstablishmentResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation error or invalid request parameters",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "User is not authorized as a host or is not the owner",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Establishment or user not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Establishment is inactive or deleted and cannot be updated",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<EstablishmentResponse> updateEstablishment(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateEstablishmentRequest request,
            @RequestHeader(name = "X-Host-Id", required = false) UUID hostIdHeader
    ) {
        Establishment updated = updateEstablishmentUseCase.execute(request.toCommand(id, hostIdHeader));
        return ResponseEntity.ok(EstablishmentResponse.from(updated));
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete an establishment (soft delete)",
            description = "Soft-deletes an establishment by setting active to false. Requires the owning host ID via query param or X-Host-Id header."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Establishment soft-deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Missing hostId parameter or invalid request",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "User is not authorized as a host or is not the owner",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Establishment or user not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<Void> deleteEstablishment(
            @PathVariable UUID id,
            @Parameter(description = "Host ID of the owner")
            @RequestParam(name = "hostId", required = false) UUID hostIdParam,
            @Parameter(description = "Host ID of the owner in header")
            @RequestHeader(name = "X-Host-Id", required = false) UUID hostIdHeader
    ) {
        UUID effectiveHostId = hostIdParam != null ? hostIdParam : hostIdHeader;
        if (effectiveHostId == null) {
            throw new IllegalArgumentException("Host ID is required to delete an establishment.");
        }

        deleteEstablishmentUseCase.execute(new DeleteEstablishmentUseCase.Command(id, effectiveHostId));
        return ResponseEntity.noContent().build();
    }
}
