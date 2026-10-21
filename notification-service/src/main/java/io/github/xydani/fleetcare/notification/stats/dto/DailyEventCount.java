package io.github.xydani.fleetcare.notification.stats.dto;

import java.time.LocalDate;

import io.github.xydani.fleetcare.notification.event.EventType;

public record DailyEventCount(LocalDate day, EventType type, long count) {
}
