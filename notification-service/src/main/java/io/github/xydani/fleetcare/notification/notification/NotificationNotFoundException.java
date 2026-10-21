package io.github.xydani.fleetcare.notification.notification;

import io.github.xydani.fleetcare.notification.common.error.NotFoundException;

public class NotificationNotFoundException extends NotFoundException {

    public NotificationNotFoundException(String id) {
        super("Notification " + id + " not found");
    }
}
