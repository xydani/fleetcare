package io.github.xydani.fleetcare.maintenance.report;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import io.github.xydani.fleetcare.maintenance.report.dto.BrandAverageCost;
import io.github.xydani.fleetcare.maintenance.report.dto.MonthlySpending;
import io.github.xydani.fleetcare.maintenance.report.dto.WeekdayLoad;

@WebMvcTest(ReportController.class)
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReportService reportService;

    @Test
    void shouldUseDefaultThreshold_whenListingAverageCostByBrand() throws Exception {
        when(reportService.averageCostByBrand(3)).thenReturn(List.of(
                new BrandAverageCost("Fiat", 3, new BigDecimal("233.33"), new BigDecimal("700.00"))));

        mockMvc.perform(get("/api/reports/brands/average-cost"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].brand").value("Fiat"))
                .andExpect(jsonPath("$[0].averageCost").value(233.33));
    }

    @Test
    void shouldReturn400_whenLimitIsOutOfRange() throws Exception {
        mockMvc.perform(get("/api/reports/brands/top-interventions").param("limit", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnMonthlySpending_forTheRequestedYear() throws Exception {
        when(reportService.monthlySpending(2025)).thenReturn(List.of(
                new MonthlySpending(LocalDate.of(2025, 1, 1), 2, new BigDecimal("400.00"), new BigDecimal("400.00"))));

        mockMvc.perform(get("/api/reports/monthly-spending").param("year", "2025"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].month").value("2025-01-01"))
                .andExpect(jsonPath("$[0].cumulativeSpent").value(400.00));
    }

    @Test
    void shouldReturnWorkshopLoad_forTheGivenRange() throws Exception {
        when(reportService.workshopLoad(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 31))).thenReturn(List.of(
                new WeekdayLoad(DayOfWeek.MONDAY, 3, new BigDecimal("7.00"), new BigDecimal("87.5"))));

        mockMvc.perform(get("/api/reports/workshop-load").param("from", "2026-01-01").param("to", "2026-03-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].dayOfWeek").value("MONDAY"));
    }
}
