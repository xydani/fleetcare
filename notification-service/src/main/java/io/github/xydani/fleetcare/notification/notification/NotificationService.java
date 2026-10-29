package io.github.xydani.fleetcare.notification.notification;

import java.time.Clock;
import java.time.Instant;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import io.github.xydani.fleetcare.notification.common.web.PageResponse;
import io.github.xydani.fleetcare.notification.notification.dto.NotificationResponse;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final Clock clock;

    public NotificationService(NotificationRepository notificationRepository, Clock clock) {
        this.notificationRepository = notificationRepository;
        this.clock = clock;
    }

    /**
     * @param vehicleId optional filter
     * @param unread    when true, only notifications not read yet
     */
    public PageResponse<NotificationResponse> findAll(Long vehicleId, boolean unread, Pageable pageable) {
        return PageResponse.of(findPage(vehicleId, unread, pageable), NotificationResponse::from);
    }

    public long countUnread() {
        return notificationRepository.countByReadFalse();
    }

    public NotificationResponse markRead(String id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new NotificationNotFoundException(id));
        notification.markRead(Instant.now(clock));
        return NotificationResponse.from(notificationRepository.save(notification));
    }

    private Page<Notification> findPage(Long vehicleId, boolean unread, Pageable pageable) {
        if (vehicleId != null && unread) {
            return notificationRepository.findByVehicleIdAndReadFalse(vehicleId, pageable);
        }
        if (vehicleId != null) {
            return notificationRepository.findByVehicleId(vehicleId, pageable);
        }
        if (unread) {
            return notificationRepository.findByReadFalse(pageable);
        }
        return notificationRepository.findAll(pageable);
    }
}
