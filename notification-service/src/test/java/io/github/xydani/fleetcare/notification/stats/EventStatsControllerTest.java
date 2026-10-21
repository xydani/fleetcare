package io.github.xydani.fleetcare.notification.stats;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import io.github.xydani.fleetcare.notification.event.EventType;
import io.github.xydani.fleetcare.notification.stats.dto.DailyEventCount;

@WebMvcTest(EventStatsController.class)
@Import(EventStatsControllerTest.FixedClock.class)
class EventStatsControllerTest {

    private static final ZoneId ROME = ZoneId.of("Europe/Rome");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EventStatsQuery eventStatsQuery;

    @Test
    void shouldStartWindowAtLocalMidnight_includingToday() throws Exception {
        // 7 days ending Wednesday 21 October start on Thursday 15 at 00:00 in Rome, 22:00 UTC the day before
        when(eventStatsQuery.countPerDayAndType(Instant.parse("2026-10-14T22:00:00Z"), ROME)).thenReturn(List.of(
                new DailyEventCount(LocalDate.of(2026, 10, 19), EventType.BOOKING_CONFIRMED, 2)));

        mockMvc.perform(get("/api/events/stats").param("days", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].day").value("2026-10-19"))
                .andExpect(jsonPath("$[0].count").value(2));
    }

    @Test
    void shouldReturn400_whenWindowIsLongerThanRetention() throws Exception {
        mockMvc.perform(get("/api/events/stats").param("days", "91"))
                .andExpect(status().isBadRequest());
    }

    @TestConfiguration
    static class FixedClock {

        @Bean
        Clock clock() {
            return Clock.fixed(Instant.parse("2026-10-21T08:00:00Z"), ROME);
        }
    }
}
