package io.github.xydani.fleetcare.notification.stats;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import io.github.xydani.fleetcare.notification.stats.dto.DailyEventCount;

@Service
public class EventStatsService {

    private final EventStatsQuery eventStatsQuery;
    private final Clock clock;

    public EventStatsService(EventStatsQuery eventStatsQuery, Clock clock) {
        this.eventStatsQuery = eventStatsQuery;
        this.clock = clock;
    }

    /**
     * Events per day and type over the last {@code days} days, today included. Days start at local midnight
     * in the service's time zone.
     */
    public List<DailyEventCount> lastDays(int days) {
        LocalDate firstDay = LocalDate.now(clock).minusDays(days - 1L);
        return eventStatsQuery.countPerDayAndType(firstDay.atStartOfDay(clock.getZone()).toInstant(), clock.getZone());
    }
}
