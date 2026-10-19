package io.github.xydani.fleetcare.maintenance.booking.dto;

import java.time.Instant;
import java.time.LocalDateTime;

import io.github.xydani.fleetcare.maintenance.booking.BookingStatus;
import io.github.xydani.fleetcare.maintenance.booking.ServiceType;

public record BookingResponse(
        Long id,
        Long vehicleId,
        String plate,
        String brand,
        String model,
        ServiceType serviceType,
        LocalDateTime startsAt,
        LocalDateTime endsAt,
        BookingStatus status,
        String notes,
        Instant createdAt
) {
}
