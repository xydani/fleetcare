package io.github.xydani.fleetcare.notification.stats;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import io.github.xydani.fleetcare.notification.event.EventType;
import io.github.xydani.fleetcare.notification.stats.dto.DailyEventCount;

@WebMvcTest(EventStatsController.class)
class EventStatsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EventStatsService eventStatsService;

    @Test
    void shouldReturnDailyCounts_forTheRequestedDays() throws Exception {
        when(eventStatsService.lastDays(7)).thenReturn(List.of(
                new DailyEventCount(LocalDate.of(2026, 10, 19), EventType.BOOKING_CONFIRMED, 2)));

        mockMvc.perform(get("/api/events/stats").param("days", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].day").value("2026-10-19"))
                .andExpect(jsonPath("$[0].count").value(2));
    }

    @Test
    void shouldUse30Days_byDefault() throws Exception {
        when(eventStatsService.lastDays(30)).thenReturn(List.of());

        mockMvc.perform(get("/api/events/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void shouldReturn400_whenWindowIsLongerThanRetention() throws Exception {
        mockMvc.perform(get("/api/events/stats").param("days", "91"))
                .andExpect(status().isBadRequest());
    }
}
