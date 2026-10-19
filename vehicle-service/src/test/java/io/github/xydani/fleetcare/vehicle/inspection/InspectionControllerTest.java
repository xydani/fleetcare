package io.github.xydani.fleetcare.vehicle.inspection;

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

import io.github.xydani.fleetcare.vehicle.inspection.dto.InspectionDue;
import io.github.xydani.fleetcare.vehicle.inspection.dto.InspectionRequest;
import io.github.xydani.fleetcare.vehicle.inspection.dto.InspectionResponse;

@WebMvcTest(InspectionController.class)
class InspectionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InspectionService inspectionService;

    @Test
    void shouldReturn201WithExpiry_whenInspectionIsRecorded() throws Exception {
        when(inspectionService.record(eq(1L), any(InspectionRequest.class))).thenReturn(new InspectionResponse(
                5L, 1L, LocalDate.parse("2026-09-01"), LocalDate.parse("2028-09-01"), InspectionOutcome.PASSED, null));

        mockMvc.perform(post("/api/vehicles/1/inspections")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"inspectionDate\": \"2026-09-01\", \"outcome\": \"PASSED\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.expiryDate").value("2028-09-01"));
    }

    @Test
    void shouldReturn400_whenOutcomeIsMissing() throws Exception {
        mockMvc.perform(post("/api/vehicles/1/inspections")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"inspectionDate\": \"2026-09-01\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.outcome").exists());
    }

    @Test
    void shouldReturnDueVehicles_withOverdueFlag() throws Exception {
        when(inspectionService.findDueWithin(30)).thenReturn(List.of(new InspectionDue(1L, "AB123CD", "Fiat",
                "Panda", 2L, "Giulia Bianchi", "giulia@example.it", LocalDate.parse("2026-10-10"), -9, true)));

        mockMvc.perform(get("/api/vehicles/inspections/expiring"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].plate").value("AB123CD"))
                .andExpect(jsonPath("$[0].overdue").value(true))
                .andExpect(jsonPath("$[0].ownerEmail").value("giulia@example.it"));
    }

    @Test
    void shouldReturn400_whenDaysIsOutOfRange() throws Exception {
        mockMvc.perform(get("/api/vehicles/inspections/expiring").param("days", "1000"))
                .andExpect(status().isBadRequest());
    }
}
