package io.github.xydani.fleetcare.notification.event.dto;

import java.util.Map;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import io.github.xydani.fleetcare.notification.event.EventType;

/**
 * @param source  the service that reports the event, e.g. maintenance-service
 * @param payload event details; the fields each type needs are checked by the notification factory
 */
public record EventRequest(
        @NotNull EventType type,
        @NotBlank @Size(max = 60) String source,
        @NotNull Map<String, Object> payload
) {
}
