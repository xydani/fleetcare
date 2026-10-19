package io.github.xydani.fleetcare.vehicle.stats;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import io.github.xydani.fleetcare.vehicle.stats.dto.FleetStatsResponse;
import io.github.xydani.fleetcare.vehicle.vehicle.FuelType;

@WebMvcTest(FleetStatsController.class)
class FleetStatsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FleetStatsService statsService;

    @Test
    void shouldReturnFleetStats() throws Exception {
        when(statsService.getStats()).thenReturn(new FleetStatsResponse(4,
                List.of(new FleetStatsResponse.FuelTypeShare(FuelType.PETROL, 2, new BigDecimal("50.0"))),
                List.of(new FleetStatsResponse.AgeBand("0-3 years", 3, 21_000)),
                List.of(new FleetStatsResponse.BrandCount("Fiat", 2))));

        mockMvc.perform(get("/api/vehicles/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalVehicles").value(4))
                .andExpect(jsonPath("$.byFuelType[0].percentage").value(50.0))
                .andExpect(jsonPath("$.byAge[0].ageBand").value("0-3 years"))
                .andExpect(jsonPath("$.topBrands[0].brand").value("Fiat"));
    }
}
