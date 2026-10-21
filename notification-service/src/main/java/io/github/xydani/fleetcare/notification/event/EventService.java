package io.github.xydani.fleetcare.notification.event;

import java.time.Clock;
import java.time.Instant;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import io.github.xydani.fleetcare.notification.event.dto.EventRequest;
import io.github.xydani.fleetcare.notification.event.dto.EventResponse;
import io.github.xydani.fleetcare.notification.notification.Notification;
import io.github.xydani.fleetcare.notification.notification.NotificationFactory;
import io.github.xydani.fleetcare.notification.notification.NotificationRepository;

@Service
public class EventService {

    private final EventLogRepository eventLogRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationFactory notificationFactory;
    private final Clock clock;

    public EventService(EventLogRepository eventLogRepository, NotificationRepository notificationRepository,
                        NotificationFactory notificationFactory, Clock clock) {
        this.eventLogRepository = eventLogRepository;
        this.notificationRepository = notificationRepository;
        this.notificationFactory = notificationFactory;
        this.clock = clock;
    }

    /**
     * Validates the event, logs it and creates its notification. An event whose notification already
     * exists (same deduplication key) is logged but creates nothing new, so senders may safely retry.
     *
     * @throws InvalidEventException if the payload does not fit the event type; nothing is stored
     */
    public EventResponse ingest(EventRequest request) {
        Notification notification = notificationFactory.create(request.type(), request.payload());
        EventLog event = eventLogRepository.save(
                new EventLog(request.type(), request.source(), request.payload(), Instant.now(clock)));

        String existing = existingNotificationId(notification.getDedupKey());
        if (existing != null) {
            return new EventResponse(event.getId(), existing, true);
        }
        try {
            return new EventResponse(event.getId(), notificationRepository.save(notification).getId(), false);
        } catch (DuplicateKeyException ex) {
            // a concurrent event created it between the check and the insert: the unique index caught it
            return new EventResponse(event.getId(), existingNotificationId(notification.getDedupKey()), true);
        }
    }

    private String existingNotificationId(String dedupKey) {
        return notificationRepository.findByDedupKey(dedupKey).map(Notification::getId).orElse(null);
    }
}
