package io.github.xydani.fleetcare.notification.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Map;

import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;

import io.github.xydani.fleetcare.notification.TestcontainersConfiguration;
import io.github.xydani.fleetcare.notification.common.config.MongoIndexConfig;
import io.github.xydani.fleetcare.notification.event.EventLog;
import io.github.xydani.fleetcare.notification.event.EventType;

@DataMongoTest
@Import({TestcontainersConfiguration.class, MongoIndexConfig.class})
class NotificationRepositoryTest {

    private static final Instant NOW = Instant.parse("2026-10-21T08:00:00Z");

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private MongoIndexConfig indexConfig;

    @BeforeEach
    void setUp() {
        mongoTemplate.getDb().drop();
        indexConfig.createIndexes();
    }

    @Test
    void shouldFilterByVehicleAndReadStatus_withDerivedQueries() {
        // given
        Notification read = notificationRepository.save(notification(1L, null));
        read.markRead(NOW);
        notificationRepository.save(read);
        notificationRepository.save(notification(1L, null));
        notificationRepository.save(notification(2L, null));

        // when / then
        var page = PageRequest.of(0, 10);
        assertThat(notificationRepository.findByReadFalse(page).getTotalElements()).isEqualTo(2);
        assertThat(notificationRepository.findByVehicleId(1L, page).getTotalElements()).isEqualTo(2);
        assertThat(notificationRepository.findByVehicleIdAndReadFalse(1L, page).getTotalElements()).isEqualTo(1);
        assertThat(notificationRepository.countByReadFalse()).isEqualTo(2);
    }

    @Test
    void shouldRejectSecondNotification_withTheSameDedupKey() {
        // given
        notificationRepository.save(notification(1L, "INSPECTION_DUE:1:2026-11-05"));

        // when / then
        assertThat(notificationRepository.findByDedupKey("INSPECTION_DUE:1:2026-11-05")).isPresent();
        assertThatThrownBy(() -> notificationRepository.save(notification(1L, "INSPECTION_DUE:1:2026-11-05")))
                .isInstanceOf(DuplicateKeyException.class);
    }

    @Test
    void shouldAllowManyNotifications_withoutDedupKey() {
        notificationRepository.save(notification(1L, null));
        notificationRepository.save(notification(1L, null));

        assertThat(notificationRepository.count()).isEqualTo(2);
    }

    @Test
    void shouldExpireEventsAfter90Days_throughATtlIndex() {
        // given: the collection exists once an event is stored
        mongoTemplate.save(new EventLog(EventType.BOOKING_CONFIRMED, "test", Map.of(), NOW));

        // when
        Document ttl = mongoTemplate.getCollection("event_log").listIndexes()
                .into(new ArrayList<>()).stream()
                .filter(index -> "ttl_received_at".equals(index.getString("name")))
                .findFirst().orElseThrow();

        // then
        assertThat(((Number) ttl.get("expireAfterSeconds")).longValue()).isEqualTo(90L * 24 * 60 * 60);
    }

    private static Notification notification(Long vehicleId, String dedupKey) {
        return new Notification(EventType.INSPECTION_DUE, vehicleId, "FG482KL", null, "Title", "Message", dedupKey,
                NOW);
    }
}
