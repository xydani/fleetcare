package io.github.xydani.fleetcare.vehicle.mileage;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import io.github.xydani.fleetcare.vehicle.common.error.BusinessRuleException;
import io.github.xydani.fleetcare.vehicle.mileage.dto.MileageHistoryEntry;
import io.github.xydani.fleetcare.vehicle.mileage.dto.MileageReadingRequest;
import io.github.xydani.fleetcare.vehicle.mileage.dto.MileageReadingResponse;

@WebMvcTest(MileageController.class)
class MileageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MileageService mileageService;

    @Test
    void shouldReturnHistory_whenVehicleHasReadings() throws Exception {
        when(mileageService.getHistory(1L)).thenReturn(List.of(
                new MileageHistoryEntry(LocalDate.parse("2026-01-01"), 10_000, null, null, null),
                new MileageHistoryEntry(LocalDate.parse("2026-02-01"), 11_000, 1_000, 31, 1_000)));

        mockMvc.perform(get("/api/vehicles/1/mileage"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].readingDate").value("2026-01-01"))
                .andExpect(jsonPath("$[1].kmSincePrevious").value(1000));
    }

    @Test
    void shouldReturn201_whenReadingIsAdded() throws Exception {
        when(mileageService.addReading(eq(1L), any(MileageReadingRequest.class)))
                .thenReturn(new MileageReadingResponse(10L, 1L, LocalDate.parse("2026-10-01"), 52_300));

        mockMvc.perform(post("/api/vehicles/1/mileage")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"readingDate\": \"2026-10-01\", \"km\": 52300}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.km").value(52300));
    }

    @Test
    void shouldReturn400_whenKmIsNegative() throws Exception {
        mockMvc.perform(post("/api/vehicles/1/mileage")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"readingDate\": \"2026-10-01\", \"km\": -5}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.km").exists());
    }

    @Test
    void shouldReturn422_whenMileageDecreases() throws Exception {
        when(mileageService.addReading(eq(1L), any(MileageReadingRequest.class)))
                .thenThrow(new BusinessRuleException("Mileage cannot decrease"));

        mockMvc.perform(post("/api/vehicles/1/mileage")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"readingDate\": \"2026-10-01\", \"km\": 100}"))
                .andExpect(status().isUnprocessableContent());
    }
}
