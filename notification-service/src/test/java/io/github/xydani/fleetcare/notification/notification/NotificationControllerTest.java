package io.github.xydani.fleetcare.notification.notification;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import io.github.xydani.fleetcare.notification.common.web.PageResponse;
import io.github.xydani.fleetcare.notification.event.EventType;
import io.github.xydani.fleetcare.notification.notification.dto.NotificationResponse;

@WebMvcTest(NotificationController.class)
class NotificationControllerTest {

    private static final Instant NOW = Instant.parse("2026-10-21T08:00:00Z");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationService notificationService;

    @Test
    void shouldListUnreadNotifications_whenAsked() throws Exception {
        when(notificationService.findAll(eq(null), eq(true), any(Pageable.class)))
                .thenReturn(new PageResponse<>(List.of(response(false)), 0, 20, 1, 1));

        mockMvc.perform(get("/api/notifications").param("unread", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Inspection due soon"))
                .andExpect(jsonPath("$.content[0].read").value(false));
    }

    @Test
    void shouldReturnUnreadCount() throws Exception {
        when(notificationService.countUnread()).thenReturn(4L);

        mockMvc.perform(get("/api/notifications/unread-count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unread").value(4));
    }

    @Test
    void shouldMarkAsRead() throws Exception {
        when(notificationService.markRead("n-1")).thenReturn(response(true));

        mockMvc.perform(patch("/api/notifications/n-1/read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.read").value(true));
    }

    @Test
    void shouldReturn404_whenNotificationDoesNotExist() throws Exception {
        when(notificationService.markRead("nope")).thenThrow(new NotificationNotFoundException("nope"));

        mockMvc.perform(patch("/api/notifications/nope/read"))
                .andExpect(status().isNotFound());
    }

    private static NotificationResponse response(boolean read) {
        return new NotificationResponse("n-1", EventType.INSPECTION_DUE, 1L, "FG482KL", "giulia.bianchi@example.it",
                "Inspection due soon", "The inspection of Fiat Panda FG482KL is due by 05/11/2026.", read, NOW,
                read ? NOW : null);
    }
}
