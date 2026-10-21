package io.github.xydani.fleetcare.notification.stats;

import static org.mockito.Mockito.verify;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EventStatsServiceTest {

    private static final ZoneId ROME = ZoneId.of("Europe/Rome");

    @Mock
    private EventStatsQuery eventStatsQuery;

    @Test
    void shouldStartWindowAtLocalMidnight_includingToday() {
        // given: Wednesday 21 October 2026, 10:00 in Rome
        var service = new EventStatsService(eventStatsQuery, Clock.fixed(Instant.parse("2026-10-21T08:00:00Z"), ROME));

        // when
        service.lastDays(7);

        // then: Thursday 15 at 00:00 in Rome is 22:00 UTC the day before
        verify(eventStatsQuery).countPerDayAndType(Instant.parse("2026-10-14T22:00:00Z"), ROME);
    }

    @Test
    void shouldCoverOnlyToday_whenAskedForOneDay() {
        var service = new EventStatsService(eventStatsQuery, Clock.fixed(Instant.parse("2026-10-21T08:00:00Z"), ROME));

        service.lastDays(1);

        verify(eventStatsQuery).countPerDayAndType(Instant.parse("2026-10-20T22:00:00Z"), ROME);
    }
}
