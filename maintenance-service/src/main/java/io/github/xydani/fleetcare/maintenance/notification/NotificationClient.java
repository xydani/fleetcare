package io.github.xydani.fleetcare.maintenance.notification;

import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Declarative HTTP client for the notification service's event intake.
 */
@FeignClient(name = "notification-service", url = "${fleetcare.clients.notification-service-url}")
interface NotificationClient {

    @PostMapping("/api/events")
    void publish(@RequestBody EventRequest event);

    /**
     * @param type    what happened, e.g. BOOKING_CONFIRMED
     * @param source  the service that reports it
     * @param payload event details; the notification service turns them into a message
     */
    record EventRequest(String type, String source, Map<String, Object> payload) {
    }
}
