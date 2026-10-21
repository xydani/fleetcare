package io.github.xydani.fleetcare.notification.event;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface EventLogRepository extends MongoRepository<EventLog, String> {
}
