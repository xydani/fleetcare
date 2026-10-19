package io.github.xydani.fleetcare.maintenance.booking.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import io.github.xydani.fleetcare.maintenance.booking.ServiceType;

/**
 * @param durationMinutes optional: when missing, the default length of the service type is used
 */
public record BookingRequest(
        @NotNull Long vehicleId,
        @NotNull ServiceType serviceType,
        @NotNull LocalDateTime startsAt,
        @Min(15) @Max(600) Integer durationMinutes,
        @Size(max = 500) String notes
) {
}
