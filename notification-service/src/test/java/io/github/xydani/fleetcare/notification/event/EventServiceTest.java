package io.github.xydani.fleetcare.notification.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.util.ReflectionTestUtils;

import io.github.xydani.fleetcare.notification.event.dto.EventRequest;
import io.github.xydani.fleetcare.notification.notification.Notification;
import io.github.xydani.fleetcare.notification.notification.NotificationFactory;
import io.github.xydani.fleetcare.notification.notification.NotificationRepository;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-21T08:00:00Z"), ZoneOffset.UTC);
    private static final EventRequest BOOKING = new EventRequest(EventType.BOOKING_CONFIRMED, "maintenance-service",
            Map.of("bookingId", 5, "vehicleId", 7, "plate", "FG482KL", "serviceType", "BRAKES",
                    "startsAt", "2026-10-26T09:00"));

    @Mock
    private EventLogRepository eventLogRepository;

    @Mock
    private NotificationRepository notificationRepository;

    private EventService eventService;

    @BeforeEach
    void setUp() {
        eventService = new EventService(eventLogRepository, notificationRepository, new NotificationFactory(CLOCK),
                CLOCK);
    }

    @Test
    void shouldLogEventAndCreateNotification_whenEventIsNew() {
        // given
        givenEventIsSaved();
        when(notificationRepository.findByDedupKey("BOOKING_CONFIRMED:5")).thenReturn(Optional.empty());
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> withId(invocation.getArgument(0), "n-1"));

        // when
        var response = eventService.ingest(BOOKING);

        // then
        assertThat(response.eventId()).isEqualTo("e-1");
        assertThat(response.notificationId()).isEqualTo("n-1");
        assertThat(response.duplicate()).isFalse();
    }

    @Test
    void shouldReturnExistingNotification_whenEventIsRepeated() {
        // given
        givenEventIsSaved();
        when(notificationRepository.findByDedupKey("BOOKING_CONFIRMED:5"))
                .thenReturn(Optional.of(withId(notification(), "n-1")));

        // when
        var response = eventService.ingest(BOOKING);

        // then
        assertThat(response.duplicate()).isTrue();
        assertThat(response.notificationId()).isEqualTo("n-1");
        verify(notificationRepository, never()).save(any());
    }

    @Test
    void shouldReportDuplicate_whenAConcurrentEventWinsTheRace() {
        // given
        givenEventIsSaved();
        when(notificationRepository.findByDedupKey("BOOKING_CONFIRMED:5"))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(withId(notification(), "n-1")));
        when(notificationRepository.save(any(Notification.class))).thenThrow(new DuplicateKeyException("uk_dedup_key"));

        // when
        var response = eventService.ingest(BOOKING);

        // then
        assertThat(response.duplicate()).isTrue();
        assertThat(response.notificationId()).isEqualTo("n-1");
    }

    @Test
    void shouldStoreNothing_whenPayloadIsInvalid() {
        // given
        var invalid = new EventRequest(EventType.BOOKING_CONFIRMED, "maintenance-service", Map.of("bookingId", 5));

        // when / then
        assertThatThrownBy(() -> eventService.ingest(invalid)).isInstanceOf(InvalidEventException.class);
        verifyNoInteractions(eventLogRepository, notificationRepository);
    }

    private void givenEventIsSaved() {
        when(eventLogRepository.save(any(EventLog.class))).thenAnswer(invocation -> withId(invocation.getArgument(0), "e-1"));
    }

    private static Notification notification() {
        return new NotificationFactory(CLOCK).create(BOOKING.type(), BOOKING.payload());
    }

    /* Mongo assigns ids on save; the mocks stand in for that. */
    private static <T> T withId(T document, String id) {
        ReflectionTestUtils.setField(document, "id", id);
        return document;
    }
}
