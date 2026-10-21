package io.github.xydani.fleetcare.notification.common.config;

import java.time.Duration;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;

import io.github.xydani.fleetcare.notification.event.EventLog;
import io.github.xydani.fleetcare.notification.notification.Notification;

/**
 * Creates the indexes at startup, explicitly rather than through Spring Data's automatic index
 * creation, so they are visible in one place. Creating an index that already exists is a no-op.
 */
@Configuration(proxyBeanMethods = false)
public class MongoIndexConfig {

    static final Duration EVENT_RETENTION = Duration.ofDays(90);

    private final MongoTemplate mongoTemplate;

    public MongoIndexConfig(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void createIndexes() {
        // TTL: MongoDB deletes each event 90 days after it was received
        mongoTemplate.indexOps(EventLog.class).createIndex(new Index("received_at", Sort.Direction.ASC)
                .named("ttl_received_at")
                .expire(EVENT_RETENTION));

        // sparse: notifications without a dedup key are not indexed, so they never clash
        mongoTemplate.indexOps(Notification.class).createIndex(new Index("dedup_key", Sort.Direction.ASC)
                .named("uk_dedup_key")
                .unique()
                .sparse());
        mongoTemplate.indexOps(Notification.class).createIndex(new Index("vehicle_id", Sort.Direction.ASC)
                .on("created_at", Sort.Direction.DESC)
                .named("ix_vehicle_created_at"));
    }
}
