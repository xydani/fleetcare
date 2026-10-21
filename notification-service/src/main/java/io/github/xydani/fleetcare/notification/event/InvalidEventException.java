package io.github.xydani.fleetcare.notification.event;

import io.github.xydani.fleetcare.notification.common.error.BusinessRuleException;

/**
 * The event's payload lacks a field its type requires, or the field has the wrong type. Mapped to HTTP 422.
 */
public class InvalidEventException extends BusinessRuleException {

    public InvalidEventException(String message) {
        super(message);
    }
}
