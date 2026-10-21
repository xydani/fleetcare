package io.github.xydani.fleetcare.notification.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import io.github.xydani.fleetcare.notification.event.EventType;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    private static final Instant NOW = Instant.parse("2026-10-21T08:00:00Z");
    private static final Pageable PAGE = PageRequest.of(0, 20);

    @Mock
    private NotificationRepository notificationRepository;

    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService(notificationRepository, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void shouldUseUnreadQuery_whenOnlyUnreadAreRequested() {
        // given
        when(notificationRepository.findByReadFalse(PAGE)).thenReturn(new PageImpl<>(List.of(notification()), PAGE, 1));

        // when
        var page = notificationService.findAll(null, true, PAGE);

        // then
        assertThat(page.content()).singleElement().satisfies(n -> assertThat(n.plate()).isEqualTo("FG482KL"));
    }

    @Test
    void shouldUseVehicleQueries_whenVehicleIsGiven() {
        // given
        when(notificationRepository.findByVehicleId(7L, PAGE)).thenReturn(new PageImpl<>(List.of(), PAGE, 0));
        when(notificationRepository.findByVehicleIdAndReadFalse(7L, PAGE)).thenReturn(new PageImpl<>(List.of(), PAGE, 0));

        // when
        notificationService.findAll(7L, false, PAGE);
        notificationService.findAll(7L, true, PAGE);

        // then
        verify(notificationRepository).findByVehicleId(7L, PAGE);
        verify(notificationRepository).findByVehicleIdAndReadFalse(7L, PAGE);
    }

    @Test
    void shouldListEverything_whenNoFilterIsGiven() {
        // given
        when(notificationRepository.findAll(PAGE)).thenReturn(new PageImpl<>(List.of(notification()), PAGE, 1));

        // when / then
        assertThat(notificationService.findAll(null, false, PAGE).totalElements()).isEqualTo(1);
    }

    @Test
    void shouldMarkReadOnce_keepingTheFirstReadTime() {
        // given
        Notification notification = notification();
        notification.markRead(NOW.minusSeconds(60));
        when(notificationRepository.findById("n-1")).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        var response = notificationService.markRead("n-1");

        // then
        assertThat(response.read()).isTrue();
        assertThat(response.readAt()).isEqualTo(NOW.minusSeconds(60));
    }

    @Test
    void shouldThrowNotFound_whenNotificationDoesNotExist() {
        // given
        when(notificationRepository.findById("nope")).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> notificationService.markRead("nope"))
                .isInstanceOf(NotificationNotFoundException.class)
                .hasMessage("Notification nope not found");
    }

    private static Notification notification() {
        return new Notification(EventType.BOOKING_CONFIRMED, 7L, "FG482KL", null, "Title", "Message", null, NOW);
    }
}
