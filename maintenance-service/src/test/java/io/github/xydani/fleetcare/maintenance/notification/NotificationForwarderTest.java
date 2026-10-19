package io.github.xydani.fleetcare.maintenance.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.xydani.fleetcare.maintenance.booking.BookingConfirmedEvent;
import io.github.xydani.fleetcare.maintenance.booking.ServiceType;
import io.github.xydani.fleetcare.maintenance.intervention.InterventionCompletedEvent;
import io.github.xydani.fleetcare.maintenance.notification.NotificationClient.EventRequest;

@ExtendWith(MockitoExtension.class)
class NotificationForwarderTest {

    @Mock
    private NotificationClient notificationClient;

    @Test
    void shouldSendBookingConfirmedEvent_withBookingDetails() {
        // given
        var forwarder = new NotificationForwarder(notificationClient);

        // when
        forwarder.on(new BookingConfirmedEvent(5L, 7L, "FG482KL", ServiceType.BRAKES,
                LocalDateTime.of(2026, 10, 26, 9, 0)));

        // then
        EventRequest sent = captureSentEvent();
        assertThat(sent.type()).isEqualTo("BOOKING_CONFIRMED");
        assertThat(sent.source()).isEqualTo("maintenance-service");
        assertThat(sent.payload())
                .containsEntry("bookingId", 5L)
                .containsEntry("plate", "FG482KL")
                .containsEntry("serviceType", "BRAKES")
                .containsEntry("startsAt", "2026-10-26T09:00");
    }

    @Test
    void shouldSendMaintenanceCompletedEvent_withTotalCost() {
        // given
        var forwarder = new NotificationForwarder(notificationClient);

        // when
        forwarder.on(new InterventionCompletedEvent(11L, 5L, 7L, "FG482KL", new BigDecimal("163.20")));

        // then
        EventRequest sent = captureSentEvent();
        assertThat(sent.type()).isEqualTo("MAINTENANCE_COMPLETED");
        assertThat(sent.payload()).containsEntry("totalCost", new BigDecimal("163.20"));
    }

    @Test
    void shouldSwallowFailure_whenNotificationServiceIsDown() {
        // given
        var forwarder = new NotificationForwarder(notificationClient);
        doThrow(new IllegalStateException("connection refused")).when(notificationClient).publish(any());

        // when
        forwarder.on(new InterventionCompletedEvent(11L, 5L, 7L, "FG482KL", BigDecimal.TEN));

        // then: no exception reaches the caller
        verify(notificationClient).publish(any());
    }

    private EventRequest captureSentEvent() {
        ArgumentCaptor<EventRequest> captor = ArgumentCaptor.forClass(EventRequest.class);
        verify(notificationClient).publish(captor.capture());
        return captor.getValue();
    }
}
