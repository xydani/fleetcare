package io.github.xydani.fleetcare.notification.common.error;

/**
 * A requested resource does not exist. Mapped to HTTP 404.
 */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}
