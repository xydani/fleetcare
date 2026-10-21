package io.github.xydani.fleetcare.notification.stats;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import io.github.xydani.fleetcare.notification.stats.dto.DailyEventCount;

@RestController
@Tag(name = "Events")
public class EventStatsController {

    private final EventStatsQuery eventStatsQuery;
    private final Clock clock;

    public EventStatsController(EventStatsQuery eventStatsQuery, Clock clock) {
        this.eventStatsQuery = eventStatsQuery;
        this.clock = clock;
    }

    @Operation(summary = "Events per day and type",
            description = "Over the last `days` days, today included. Events are kept for 90 days.")
    @GetMapping("/api/events/stats")
    public List<DailyEventCount> stats(@RequestParam(defaultValue = "30") @Min(1) @Max(90) int days) {
        LocalDate firstDay = LocalDate.now(clock).minusDays(days - 1L);
        return eventStatsQuery.countPerDayAndType(firstDay.atStartOfDay(clock.getZone()).toInstant(), clock.getZone());
    }
}
