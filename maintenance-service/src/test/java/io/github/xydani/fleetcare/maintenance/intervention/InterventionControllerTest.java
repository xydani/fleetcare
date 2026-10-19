package io.github.xydani.fleetcare.maintenance.intervention;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import io.github.xydani.fleetcare.maintenance.booking.ServiceType;
import io.github.xydani.fleetcare.maintenance.common.web.PageResponse;
import io.github.xydani.fleetcare.maintenance.intervention.dto.CompletionRequest;
import io.github.xydani.fleetcare.maintenance.intervention.dto.InterventionResponse;
import io.github.xydani.fleetcare.maintenance.pricing.PricingPlan;

@WebMvcTest(InterventionController.class)
class InterventionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InterventionService interventionService;

    @Test
    void shouldReturn201WithLocation_whenBookingIsCompleted() throws Exception {
        when(interventionService.complete(eq(5L), any(CompletionRequest.class))).thenReturn(response());

        mockMvc.perform(post("/api/bookings/5/complete").contentType(MediaType.APPLICATION_JSON).content("""
                        {"description": "Front pads", "partsCost": 120.00, "labourHours": 1.5, "pricingPlan": "FLEET"}
                        """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/interventions/11"))
                .andExpect(jsonPath("$.totalCost").value(163.20));
    }

    @Test
    void shouldReturn400_whenCostsAreNegativeOrPlanIsMissing() throws Exception {
        mockMvc.perform(post("/api/bookings/5/complete").contentType(MediaType.APPLICATION_JSON).content("""
                        {"description": "", "partsCost": -1, "labourHours": 100}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.description").exists())
                .andExpect(jsonPath("$.errors.partsCost").exists())
                .andExpect(jsonPath("$.errors.labourHours").exists())
                .andExpect(jsonPath("$.errors.pricingPlan").exists());
    }

    @Test
    void shouldReturnVehicleHistory_whenFilteringByVehicle() throws Exception {
        when(interventionService.findAll(eq(7L), eq(null), any(Pageable.class)))
                .thenReturn(new PageResponse<>(List.of(response()), 0, 20, 1, 1));

        mockMvc.perform(get("/api/interventions").param("vehicleId", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].plate").value("FG482KL"))
                .andExpect(jsonPath("$.content[0].serviceType").value("BRAKES"));
    }

    @Test
    void shouldReturnIntervention_whenItExists() throws Exception {
        when(interventionService.findById(11L)).thenReturn(response());

        mockMvc.perform(get("/api/interventions/11"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.discount").value(28.80));
    }

    private static InterventionResponse response() {
        return new InterventionResponse(11L, 5L, 7L, "FG482KL", "Fiat", "Panda", ServiceType.BRAKES, "Front pads",
                PricingPlan.FLEET, new BigDecimal("1.5"), new BigDecimal("120.00"), new BigDecimal("72.00"),
                new BigDecimal("28.80"), new BigDecimal("163.20"), LocalDateTime.of(2026, 10, 26, 11, 0));
    }
}
