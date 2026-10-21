package io.github.xydani.fleetcare.notification.stats;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;

import io.github.xydani.fleetcare.notification.TestcontainersConfiguration;
import io.github.xydani.fleetcare.notification.event.EventLog;
import io.github.xydani.fleetcare.notification.event.EventType;
import io.github.xydani.fleetcare.notification.stats.dto.DailyEventCount;

@DataMongoTest
@Import({TestcontainersConfiguration.class, EventStatsQuery.class})
class EventStatsQueryTest {

    private static final ZoneId ROME = ZoneId.of("Europe/Rome");

    @Autowired
    private EventStatsQuery eventStatsQuery;

    @Autowired
    private MongoTemplate mongoTemplate;

    @BeforeEach
    void setUp() {
        mongoTemplate.dropCollection(EventLog.class);
        event(EventType.BOOKING_CONFIRMED, "2026-10-19T08:00:00Z");
        event(EventType.BOOKING_CONFIRMED, "2026-10-19T15:00:00Z");
        event(EventType.MAINTENANCE_COMPLETED, "2026-10-19T16:00:00Z");
        // 23:30 UTC on the 19th is already the 20th in Rome
        event(EventType.INSPECTION_DUE, "2026-10-19T23:30:00Z");
        // before the window
        event(EventType.INSPECTION_DUE, "2026-10-10T09:00:00Z");
    }

    @Test
    void shouldCountEventsPerLocalDayAndType_sinceTheGivenInstant() {
        var stats = eventStatsQuery.countPerDayAndType(Instant.parse("2026-10-18T22:00:00Z"), ROME);

        assertThat(stats).containsExactly(
                new DailyEventCount(LocalDate.of(2026, 10, 19), EventType.BOOKING_CONFIRMED, 2),
                new DailyEventCount(LocalDate.of(2026, 10, 19), EventType.MAINTENANCE_COMPLETED, 1),
                new DailyEventCount(LocalDate.of(2026, 10, 20), EventType.INSPECTION_DUE, 1));
    }

    @Test
    void shouldReturnNothing_whenNoEventIsInTheWindow() {
        assertThat(eventStatsQuery.countPerDayAndType(Instant.parse("2026-10-21T00:00:00Z"), ROME)).isEmpty();
    }

    private void event(EventType type, String receivedAt) {
        mongoTemplate.save(new EventLog(type, "test", Map.of(), Instant.parse(receivedAt)));
    }
}
