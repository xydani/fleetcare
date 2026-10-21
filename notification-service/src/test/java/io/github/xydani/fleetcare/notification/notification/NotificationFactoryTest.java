package io.github.xydani.fleetcare.notification.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import io.github.xydani.fleetcare.notification.event.EventType;
import io.github.xydani.fleetcare.notification.event.InvalidEventException;

class NotificationFactoryTest {

    private static final Instant NOW = Instant.parse("2026-10-21T08:00:00Z");

    private final NotificationFactory factory = new NotificationFactory(Clock.fixed(NOW, ZoneOffset.UTC));

    @Test
    void shouldWarnOwner_whenInspectionIsDueSoon() {
        // when
        Notification notification = factory.create(EventType.INSPECTION_DUE, inspection(false));

        // then
        assertThat(notification.getTitle()).isEqualTo("Inspection due soon");
        assertThat(notification.getMessage()).isEqualTo("The inspection of Fiat Panda FG482KL is due by 05/11/2026.");
        assertThat(notification.getRecipient()).isEqualTo("giulia.bianchi@example.it");
        assertThat(notification.getDedupKey()).isEqualTo("INSPECTION_DUE:1:2026-11-05");
        assertThat(notification.getCreatedAt()).isEqualTo(NOW);
        assertThat(notification.isRead()).isFalse();
    }

    @Test
    void shouldSayVehicleMustNotBeDriven_whenInspectionIsOverdue() {
        // when
        Notification notification = factory.create(EventType.INSPECTION_DUE, inspection(true));

        // then
        assertThat(notification.getTitle()).isEqualTo("Inspection overdue");
        assertThat(notification.getMessage()).contains("expired on 05/11/2026", "must not be driven");
    }

    @Test
    void shouldDescribeSlot_whenBookingIsConfirmed() {
        // when
        Notification notification = factory.create(EventType.BOOKING_CONFIRMED, Map.of(
                "bookingId", 5, "vehicleId", 7, "plate", "FG482KL",
                "serviceType", "SCHEDULED_SERVICE", "startsAt", "2026-10-26T09:00"));

        // then
        assertThat(notification.getMessage()).isEqualTo("FG482KL is booked for scheduled service on 26/10/2026 at 09:00.");
        assertThat(notification.getVehicleId()).isEqualTo(7L);
        assertThat(notification.getRecipient()).isNull();
        assertThat(notification.getDedupKey()).isEqualTo("BOOKING_CONFIRMED:5");
    }

    @Test
    void shouldShowTotalWithTwoDecimals_whenMaintenanceIsCompleted() {
        // when
        Notification notification = factory.create(EventType.MAINTENANCE_COMPLETED, Map.of(
                "interventionId", 11, "bookingId", 5, "vehicleId", 7, "plate", "FG482KL", "totalCost", 163.2));

        // then
        assertThat(notification.getTitle()).isEqualTo("Maintenance completed");
        assertThat(notification.getMessage()).isEqualTo("The work on FG482KL is done. Total: € 163.20.");
        assertThat(notification.getDedupKey()).isEqualTo("MAINTENANCE_COMPLETED:11");
    }

    @ParameterizedTest
    @ValueSource(strings = {"vehicleId", "plate", "brand", "model", "dueDate", "overdue"})
    void shouldRejectEvent_whenARequiredFieldIsMissing(String field) {
        // given
        Map<String, Object> payload = new HashMap<>(inspection(false));
        payload.remove(field);

        // when / then
        assertThatThrownBy(() -> factory.create(EventType.INSPECTION_DUE, payload))
                .isInstanceOf(InvalidEventException.class)
                .hasMessage("INSPECTION_DUE event needs '%s' in its payload", field);
    }

    @Test
    void shouldRejectEvent_whenAFieldHasTheWrongType() {
        // given
        Map<String, Object> payload = new HashMap<>(inspection(false));
        payload.put("dueDate", "next week");

        // when / then
        assertThatThrownBy(() -> factory.create(EventType.INSPECTION_DUE, payload))
                .isInstanceOf(InvalidEventException.class)
                .hasMessageContaining("'dueDate' to be a date");
    }

    @Test
    void shouldRejectEvent_whenPayloadIsMissing() {
        assertThatThrownBy(() -> factory.create(EventType.MAINTENANCE_COMPLETED, null))
                .isInstanceOf(InvalidEventException.class);
    }

    private static Map<String, Object> inspection(boolean overdue) {
        return Map.of("vehicleId", 1, "plate", "FG482KL", "brand", "Fiat", "model", "Panda",
                "dueDate", "2026-11-05", "overdue", overdue, "ownerEmail", "giulia.bianchi@example.it");
    }
}
