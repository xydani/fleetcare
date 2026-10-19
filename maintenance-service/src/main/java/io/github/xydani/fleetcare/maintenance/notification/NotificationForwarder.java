package io.github.xydani.fleetcare.maintenance.notification;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import io.github.xydani.fleetcare.maintenance.booking.BookingConfirmedEvent;
import io.github.xydani.fleetcare.maintenance.intervention.InterventionCompletedEvent;
import io.github.xydani.fleetcare.maintenance.notification.NotificationClient.EventRequest;

/**
 * Forwards domain events to the notification service.
 * <ul>
 *   <li>Only after commit: a rolled-back booking never produces a notification.</li>
 *   <li>Asynchronously: the HTTP call does not slow down the request that caused it.</li>
 *   <li>Best effort: a failure is logged and dropped, a booking never fails because of a notification.
 *       A message broker would add retries; see the README.</li>
 * </ul>
 */
@Component
class NotificationForwarder {

    static final String SOURCE = "maintenance-service";

    private static final Logger log = LoggerFactory.getLogger(NotificationForwarder.class);

    private final NotificationClient notificationClient;

    NotificationForwarder(NotificationClient notificationClient) {
        this.notificationClient = notificationClient;
    }

    @Async
    @TransactionalEventListener
    void on(BookingConfirmedEvent event) {
        send(new EventRequest("BOOKING_CONFIRMED", SOURCE, Map.of(
                "bookingId", event.bookingId(),
                "vehicleId", event.vehicleId(),
                "plate", event.plate(),
                "serviceType", event.serviceType().name(),
                "startsAt", event.startsAt().toString())));
    }

    @Async
    @TransactionalEventListener
    void on(InterventionCompletedEvent event) {
        send(new EventRequest("MAINTENANCE_COMPLETED", SOURCE, Map.of(
                "interventionId", event.interventionId(),
                "bookingId", event.bookingId(),
                "vehicleId", event.vehicleId(),
                "plate", event.plate(),
                "totalCost", event.totalCost())));
    }

    private void send(EventRequest event) {
        try {
            notificationClient.publish(event);
        } catch (RuntimeException ex) {
            log.warn("Could not forward {} event to the notification service: {}", event.type(), ex.getMessage());
        }
    }
}
