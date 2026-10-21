package com.reservo.api.establishment.presentation;

import tools.jackson.databind.ObjectMapper;
import com.reservo.api.common.presentation.GlobalExceptionHandler;
import com.reservo.api.establishment.domain.exception.EstablishmentNotFoundException;
import com.reservo.api.establishment.domain.exception.UnauthorizedHostException;
import com.reservo.api.establishment.domain.model.Address;
import com.reservo.api.establishment.domain.model.Establishment;
import com.reservo.api.establishment.domain.model.EstablishmentCategory;
import com.reservo.api.establishment.domain.model.ReservationUnitType;
import com.reservo.api.establishment.presentation.dto.AddressRequest;
import com.reservo.api.establishment.presentation.dto.CreateEstablishmentRequest;
import com.reservo.api.establishment.presentation.dto.UpdateEstablishmentRequest;
import com.reservo.api.establishment.usecase.CreateEstablishmentUseCase;
import com.reservo.api.establishment.usecase.DeleteEstablishmentUseCase;
import com.reservo.api.establishment.usecase.GetEstablishmentByIdUseCase;
import com.reservo.api.establishment.usecase.ListEstablishmentsUseCase;
import com.reservo.api.establishment.usecase.UpdateEstablishmentUseCase;
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
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EstablishmentController.class)
@Import(GlobalExceptionHandler.class)
class EstablishmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CreateEstablishmentUseCase createEstablishmentUseCase;

    @MockitoBean
    private GetEstablishmentByIdUseCase getEstablishmentByIdUseCase;

    @MockitoBean
    private ListEstablishmentsUseCase listEstablishmentsUseCase;

    @MockitoBean
    private UpdateEstablishmentUseCase updateEstablishmentUseCase;

    @MockitoBean
    private DeleteEstablishmentUseCase deleteEstablishmentUseCase;

    @Test
    @DisplayName("POST /api/v1/establishments should return 201 Created and Location header when request is valid")
    void shouldReturn201CreatedWhenCreateEstablishmentPayloadIsValid() throws Exception {
        UUID establishmentId = UUID.randomUUID();
        UUID hostId = UUID.randomUUID();

        Address address = Address.builder()
                .street("456 Elm St")
                .city("Chicago")
                .country("USA")
                .build();

        Establishment establishment = Establishment.builder()
                .id(establishmentId)
                .hostId(hostId)
                .name("Modern Downtown Loft")
                .description("Cozy loft in downtown Chicago")
                .category(EstablishmentCategory.APARTMENT)
                .address(address)
                .capacity(2)
                .pricePerUnit(new BigDecimal("150.00"))
                .unitType(ReservationUnitType.NIGHT)
                .amenities(List.of("WiFi", "Elevator"))
                .rules(List.of("No smoking"))
                .images(List.of("https://example.com/loft.jpg"))
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(createEstablishmentUseCase.execute(any())).thenReturn(establishment);

        CreateEstablishmentRequest request = CreateEstablishmentRequest.builder()
                .hostId(hostId)
                .name("Modern Downtown Loft")
                .description("Cozy loft in downtown Chicago")
                .category(EstablishmentCategory.APARTMENT)
                .address(AddressRequest.builder()
                        .street("456 Elm St")
                        .city("Chicago")
                        .country("USA")
                        .build())
                .capacity(2)
                .pricePerUnit(new BigDecimal("150.00"))
                .unitType(ReservationUnitType.NIGHT)
                .amenities(List.of("WiFi", "Elevator"))
                .rules(List.of("No smoking"))
                .images(List.of("https://example.com/loft.jpg"))
                .build();

        mockMvc.perform(post("/api/v1/establishments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/v1/establishments/" + establishmentId)))
                .andExpect(jsonPath("$.id").value(establishmentId.toString()))
                .andExpect(jsonPath("$.hostId").value(hostId.toString()))
                .andExpect(jsonPath("$.name").value("Modern Downtown Loft"))
                .andExpect(jsonPath("$.category").value("APARTMENT"))
                .andExpect(jsonPath("$.address.city").value("Chicago"))
                .andExpect(jsonPath("$.capacity").value(2))
                .andExpect(jsonPath("$.pricePerUnit").value(150.00))
                .andExpect(jsonPath("$.unitType").value("NIGHT"))
                .andExpect(jsonPath("$.amenities", hasItem("WiFi")))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    @DisplayName("POST /api/v1/establishments should return 400 Bad Request when required fields are missing")
    void shouldReturn400BadRequestWhenRequiredFieldsAreMissing() throws Exception {
        CreateEstablishmentRequest invalidRequest = CreateEstablishmentRequest.builder()
                .hostId(null)
                .name("")
                .category(null)
                .capacity(0)
                .pricePerUnit(new BigDecimal("-10.00"))
                .unitType(null)
                .address(null)
                .build();

        mockMvc.perform(post("/api/v1/establishments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.details.hostId").exists())
                .andExpect(jsonPath("$.details.name").exists())
                .andExpect(jsonPath("$.details.category").exists())
                .andExpect(jsonPath("$.details.capacity").exists())
                .andExpect(jsonPath("$.details.pricePerUnit").exists())
                .andExpect(jsonPath("$.details.address").exists());
    }

    @Test
    @DisplayName("POST /api/v1/establishments should return 403 Forbidden when host is unauthorized")
    void shouldReturn403ForbiddenWhenHostIsUnauthorized() throws Exception {
        UUID customerId = UUID.randomUUID();

        when(createEstablishmentUseCase.execute(any()))
                .thenThrow(new UnauthorizedHostException(customerId));

        CreateEstablishmentRequest request = CreateEstablishmentRequest.builder()
                .hostId(customerId)
                .name("Beach House")
                .category(EstablishmentCategory.CABIN)
                .address(AddressRequest.builder().city("Miami").country("USA").build())
                .capacity(5)
                .pricePerUnit(new BigDecimal("200.00"))
                .unitType(ReservationUnitType.DAY)
                .build();

        mockMvc.perform(post("/api/v1/establishments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED_HOST"))
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value(containsString(customerId.toString())));
    }

    @Test
    @DisplayName("POST /api/v1/establishments should return 404 Not Found when host does not exist")
    void shouldReturn404NotFoundWhenHostDoesNotExist() throws Exception {
        UUID nonExistentHostId = UUID.randomUUID();

        when(createEstablishmentUseCase.execute(any()))
                .thenThrow(new UserNotFoundException(nonExistentHostId));

        CreateEstablishmentRequest request = CreateEstablishmentRequest.builder()
                .hostId(nonExistentHostId)
                .name("Beach House")
                .category(EstablishmentCategory.CABIN)
                .address(AddressRequest.builder().city("Miami").country("USA").build())
                .capacity(5)
                .pricePerUnit(new BigDecimal("200.00"))
                .unitType(ReservationUnitType.DAY)
                .build();

        mockMvc.perform(post("/api/v1/establishments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("GET /api/v1/establishments/{id} should return 200 OK when establishment exists")
    void shouldReturn200OkWhenGetEstablishmentByIdExists() throws Exception {
        UUID establishmentId = UUID.randomUUID();
        Establishment establishment = Establishment.builder()
                .id(establishmentId)
                .hostId(UUID.randomUUID())
                .name("Mountain View Cabin")
                .category(EstablishmentCategory.CABIN)
                .address(Address.builder().city("Denver").country("USA").build())
                .capacity(6)
                .pricePerUnit(new BigDecimal("300.00"))
                .unitType(ReservationUnitType.NIGHT)
                .active(true)
                .build();

        when(getEstablishmentByIdUseCase.execute(establishmentId)).thenReturn(establishment);

        mockMvc.perform(get("/api/v1/establishments/{id}", establishmentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(establishmentId.toString()))
                .andExpect(jsonPath("$.name").value("Mountain View Cabin"));
    }

    @Test
    @DisplayName("GET /api/v1/establishments/{id} should return 404 Not Found when establishment does not exist")
    void shouldReturn404NotFoundWhenGetEstablishmentDoesNotExist() throws Exception {
        UUID establishmentId = UUID.randomUUID();
        when(getEstablishmentByIdUseCase.execute(establishmentId))
                .thenThrow(new EstablishmentNotFoundException(establishmentId));

        mockMvc.perform(get("/api/v1/establishments/{id}", establishmentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ESTABLISHMENT_NOT_FOUND"))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("GET /api/v1/establishments should return 200 OK and list of establishments")
    void shouldReturn200OkWhenListingEstablishments() throws Exception {
        UUID establishmentId = UUID.randomUUID();
        Establishment establishment = Establishment.builder()
                .id(establishmentId)
                .hostId(UUID.randomUUID())
                .name("Sunny Villa")
                .category(EstablishmentCategory.HOTEL)
                .address(Address.builder().city("San Diego").country("USA").build())
                .capacity(4)
                .pricePerUnit(new BigDecimal("180.00"))
                .unitType(ReservationUnitType.NIGHT)
                .active(true)
                .build();

        when(listEstablishmentsUseCase.execute(any())).thenReturn(List.of(establishment));

        mockMvc.perform(get("/api/v1/establishments")
                        .param("category", "HOTEL")
                        .param("city", "San Diego")
                        .param("activeOnly", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(establishmentId.toString()))
                .andExpect(jsonPath("$[0].name").value("Sunny Villa"));
    }

    @Test
    @DisplayName("PUT /api/v1/establishments/{id} should return 200 OK when request is valid and user is owner")
    void shouldReturn200OkWhenUpdatingEstablishmentSuccessfully() throws Exception {
        UUID establishmentId = UUID.randomUUID();
        UUID hostId = UUID.randomUUID();

        Address updatedAddress = Address.builder().street("789 Pine St").city("Seattle").country("USA").build();
        Establishment updated = Establishment.builder()
                .id(establishmentId)
                .hostId(hostId)
                .name("Updated Luxury Cabin")
                .description("Updated Description")
                .category(EstablishmentCategory.CABIN)
                .address(updatedAddress)
                .capacity(6)
                .pricePerUnit(new BigDecimal("299.99"))
                .unitType(ReservationUnitType.DAY)
                .amenities(List.of("Hot Tub"))
                .rules(List.of("No smoking inside"))
                .images(List.of("https://example.com/cabin.jpg"))
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(updateEstablishmentUseCase.execute(any())).thenReturn(updated);

        UpdateEstablishmentRequest request = UpdateEstablishmentRequest.builder()
                .hostId(hostId)
                .name("Updated Luxury Cabin")
                .description("Updated Description")
                .category(EstablishmentCategory.CABIN)
                .address(AddressRequest.builder().street("789 Pine St").city("Seattle").country("USA").build())
                .capacity(6)
                .pricePerUnit(new BigDecimal("299.99"))
                .unitType(ReservationUnitType.DAY)
                .amenities(List.of("Hot Tub"))
                .rules(List.of("No smoking inside"))
                .images(List.of("https://example.com/cabin.jpg"))
                .build();

        mockMvc.perform(put("/api/v1/establishments/{id}", establishmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(establishmentId.toString()))
                .andExpect(jsonPath("$.name").value("Updated Luxury Cabin"))
                .andExpect(jsonPath("$.capacity").value(6))
                .andExpect(jsonPath("$.pricePerUnit").value(299.99));
    }

    @Test
    @DisplayName("PUT /api/v1/establishments/{id} should return 400 Bad Request when validation fails")
    void shouldReturn400BadRequestWhenUpdatingWithInvalidPayload() throws Exception {
        UUID establishmentId = UUID.randomUUID();

        UpdateEstablishmentRequest invalidRequest = UpdateEstablishmentRequest.builder()
                .hostId(null)
                .name("")
                .category(null)
                .capacity(-1)
                .pricePerUnit(new BigDecimal("-1.00"))
                .unitType(null)
                .address(null)
                .build();

        mockMvc.perform(put("/api/v1/establishments/{id}", establishmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("PUT /api/v1/establishments/{id} should return 403 Forbidden when host is unauthorized or not owner")
    void shouldReturn403ForbiddenWhenUpdatingUnauthorizedHost() throws Exception {
        UUID establishmentId = UUID.randomUUID();
        UUID unauthorizedHostId = UUID.randomUUID();

        when(updateEstablishmentUseCase.execute(any()))
                .thenThrow(new UnauthorizedHostException(unauthorizedHostId, establishmentId));

        UpdateEstablishmentRequest request = UpdateEstablishmentRequest.builder()
                .hostId(unauthorizedHostId)
                .name("Beach House")
                .category(EstablishmentCategory.CABIN)
                .address(AddressRequest.builder().city("Miami").country("USA").build())
                .capacity(5)
                .pricePerUnit(new BigDecimal("200.00"))
                .unitType(ReservationUnitType.DAY)
                .build();

        mockMvc.perform(put("/api/v1/establishments/{id}", establishmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED_HOST"))
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("PUT /api/v1/establishments/{id} should return 404 Not Found when establishment does not exist")
    void shouldReturn404NotFoundWhenUpdatingNonExistentEstablishment() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        UUID hostId = UUID.randomUUID();

        when(updateEstablishmentUseCase.execute(any()))
                .thenThrow(new EstablishmentNotFoundException(nonExistentId));

        UpdateEstablishmentRequest request = UpdateEstablishmentRequest.builder()
                .hostId(hostId)
                .name("Beach House")
                .category(EstablishmentCategory.CABIN)
                .address(AddressRequest.builder().city("Miami").country("USA").build())
                .capacity(5)
                .pricePerUnit(new BigDecimal("200.00"))
                .unitType(ReservationUnitType.DAY)
                .build();

        mockMvc.perform(put("/api/v1/establishments/{id}", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ESTABLISHMENT_NOT_FOUND"))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("PUT /api/v1/establishments/{id} should return 409 Conflict when establishment is inactive")
    void shouldReturn409ConflictWhenUpdatingInactiveEstablishment() throws Exception {
        UUID establishmentId = UUID.randomUUID();
        UUID hostId = UUID.randomUUID();

        when(updateEstablishmentUseCase.execute(any()))
                .thenThrow(new IllegalStateException("Cannot update an inactive or deleted establishment."));

        UpdateEstablishmentRequest request = UpdateEstablishmentRequest.builder()
                .hostId(hostId)
                .name("Beach House")
                .category(EstablishmentCategory.CABIN)
                .address(AddressRequest.builder().city("Miami").country("USA").build())
                .capacity(5)
                .pricePerUnit(new BigDecimal("200.00"))
                .unitType(ReservationUnitType.DAY)
                .build();

        mockMvc.perform(put("/api/v1/establishments/{id}", establishmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ILLEGAL_STATE"))
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("DELETE /api/v1/establishments/{id} should return 204 No Content when query param hostId is provided")
    void shouldReturn204NoContentWhenDeletingWithQueryParam() throws Exception {
        UUID establishmentId = UUID.randomUUID();
        UUID hostId = UUID.randomUUID();

        doNothing().when(deleteEstablishmentUseCase).execute(any());

        mockMvc.perform(delete("/api/v1/establishments/{id}", establishmentId)
                        .param("hostId", hostId.toString()))
                .andExpect(status().isNoContent());

        verify(deleteEstablishmentUseCase).execute(new DeleteEstablishmentUseCase.Command(establishmentId, hostId));
    }

    @Test
    @DisplayName("DELETE /api/v1/establishments/{id} should return 204 No Content when header X-Host-Id is provided")
    void shouldReturn204NoContentWhenDeletingWithHeader() throws Exception {
        UUID establishmentId = UUID.randomUUID();
        UUID hostId = UUID.randomUUID();

        doNothing().when(deleteEstablishmentUseCase).execute(any());

        mockMvc.perform(delete("/api/v1/establishments/{id}", establishmentId)
                        .header("X-Host-Id", hostId.toString()))
                .andExpect(status().isNoContent());

        verify(deleteEstablishmentUseCase).execute(new DeleteEstablishmentUseCase.Command(establishmentId, hostId));
    }

    @Test
    @DisplayName("DELETE /api/v1/establishments/{id} should return 400 Bad Request when hostId is missing")
    void shouldReturn400BadRequestWhenDeletingWithoutHostId() throws Exception {
        UUID establishmentId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/establishments/{id}", establishmentId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_ARGUMENT"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("DELETE /api/v1/establishments/{id} should return 403 Forbidden when host is unauthorized or not owner")
    void shouldReturn403ForbiddenWhenDeletingUnauthorizedHost() throws Exception {
        UUID establishmentId = UUID.randomUUID();
        UUID unauthorizedHostId = UUID.randomUUID();

        doThrow(new UnauthorizedHostException(unauthorizedHostId, establishmentId))
                .when(deleteEstablishmentUseCase).execute(any());

        mockMvc.perform(delete("/api/v1/establishments/{id}", establishmentId)
                        .param("hostId", unauthorizedHostId.toString()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED_HOST"))
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("DELETE /api/v1/establishments/{id} should return 404 Not Found when establishment does not exist")
    void shouldReturn404NotFoundWhenDeletingNonExistentEstablishment() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        UUID hostId = UUID.randomUUID();

        doThrow(new EstablishmentNotFoundException(nonExistentId))
                .when(deleteEstablishmentUseCase).execute(any());

        mockMvc.perform(delete("/api/v1/establishments/{id}", nonExistentId)
                        .param("hostId", hostId.toString()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ESTABLISHMENT_NOT_FOUND"))
                .andExpect(jsonPath("$.status").value(404));
    }
}
