package io.github.xydani.fleetcare.vehicle.vehicle;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import io.github.xydani.fleetcare.vehicle.common.web.PageResponse;
import io.github.xydani.fleetcare.vehicle.vehicle.dto.OwnerSummary;
import io.github.xydani.fleetcare.vehicle.vehicle.dto.VehicleDetailResponse;
import io.github.xydani.fleetcare.vehicle.vehicle.dto.VehicleRequest;
import io.github.xydani.fleetcare.vehicle.vehicle.dto.VehicleResponse;

@WebMvcTest(VehicleController.class)
class VehicleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VehicleService vehicleService;

    @MockitoBean
    private VehicleDetailService vehicleDetailService;

    @Test
    void shouldReturnFilteredPage_whenListingVehicles() throws Exception {
        when(vehicleService.findAll(eq("fiat"), eq(FuelType.PETROL), eq(null), any(Pageable.class)))
                .thenReturn(new PageResponse<>(List.of(response(1L)), 0, 20, 1, 1));

        mockMvc.perform(get("/api/vehicles").param("brand", "fiat").param("fuelType", "PETROL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].plate").value("FG482KL"))
                .andExpect(jsonPath("$.content[0].owner.fullName").value("Giulia Bianchi"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void shouldReturn201WithLocation_whenVehicleIsCreated() throws Exception {
        when(vehicleService.create(any(VehicleRequest.class))).thenReturn(response(3L));

        mockMvc.perform(post("/api/vehicles").contentType(MediaType.APPLICATION_JSON).content(body("FG482KL")))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/vehicles/3"));
        verify(vehicleService).create(any(VehicleRequest.class));
    }

    @ParameterizedTest
    @ValueSource(strings = {"fg482kl", "FG48KL", "FG4821KL", "IO123AB", "AB123QU"})
    void shouldReturn400_whenPlateIsNotAValidItalianPlate(String plate) throws Exception {
        mockMvc.perform(post("/api/vehicles").contentType(MediaType.APPLICATION_JSON).content(body(plate)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.plate").exists());
    }

    @Test
    void shouldReturn409_whenPlateIsAlreadyRegistered() throws Exception {
        when(vehicleService.create(any(VehicleRequest.class))).thenThrow(new DuplicatePlateException("FG482KL"));

        mockMvc.perform(post("/api/vehicles").contentType(MediaType.APPLICATION_JSON).content(body("FG482KL")))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldReturnDetailWithInspectionStatus_whenVehicleExists() throws Exception {
        when(vehicleDetailService.getDetail(1L)).thenReturn(VehicleDetailResponse.builder()
                .id(1L)
                .plate("FG482KL")
                .owner(new OwnerSummary(2L, "Giulia Bianchi"))
                .mileage(98_400, LocalDate.of(2026, 10, 1))
                .nextInspection(LocalDate.of(2026, 11, 5), InspectionStatus.DUE_SOON)
                .build());

        mockMvc.perform(get("/api/vehicles/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentKm").value(98400))
                .andExpect(jsonPath("$.inspectionStatus").value("DUE_SOON"))
                .andExpect(jsonPath("$.nextInspectionDue").value("2026-11-05"));
    }

    @Test
    void shouldReturn404_whenVehicleDoesNotExist() throws Exception {
        when(vehicleDetailService.getDetail(42L)).thenThrow(new VehicleNotFoundException(42L));

        mockMvc.perform(get("/api/vehicles/42"))
                .andExpect(status().isNotFound());
    }

    private static String body(String plate) {
        return """
                {"plate": "%s", "brand": "Fiat", "model": "Panda", "productionYear": 2017,
                 "fuelType": "PETROL", "registrationDate": "2017-04-12", "ownerId": 1}
                """.formatted(plate);
    }

    private static VehicleResponse response(Long id) {
        return new VehicleResponse(id, "FG482KL", "Fiat", "Panda", 2017, FuelType.PETROL,
                LocalDate.of(2017, 4, 12), new OwnerSummary(1L, "Giulia Bianchi"),
                Instant.parse("2026-10-19T08:00:00Z"));
    }
}
