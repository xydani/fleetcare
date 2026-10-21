package io.github.xydani.fleetcare.notification.notification;

import java.util.Map;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import io.github.xydani.fleetcare.notification.common.web.PageResponse;
import io.github.xydani.fleetcare.notification.notification.dto.NotificationResponse;

@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notifications", description = "Messages built from events")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Operation(summary = "List notifications", description = "Newest first, optionally only unread or for one vehicle.")
    @GetMapping
    public PageResponse<NotificationResponse> findAll(
            @RequestParam(required = false) Long vehicleId,
            @RequestParam(defaultValue = "false") boolean unread,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return notificationService.findAll(vehicleId, unread, pageable);
    }

    @Operation(summary = "Count unread notifications")
    @GetMapping("/unread-count")
    public Map<String, Long> countUnread() {
        return Map.of("unread", notificationService.countUnread());
    }

    @Operation(summary = "Mark a notification as read", description = "Idempotent: the first read time is kept.")
    @PatchMapping("/{id}/read")
    public NotificationResponse markRead(@PathVariable String id) {
        return notificationService.markRead(id);
    }
}
