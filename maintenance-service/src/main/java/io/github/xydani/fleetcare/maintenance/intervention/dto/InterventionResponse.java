package io.github.xydani.fleetcare.maintenance.intervention.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.github.xydani.fleetcare.maintenance.booking.ServiceType;
import io.github.xydani.fleetcare.maintenance.pricing.PricingPlan;

public record InterventionResponse(
        Long id,
        Long bookingId,
        Long vehicleId,
        String plate,
        String brand,
        String model,
        ServiceType serviceType,
        String description,
        PricingPlan pricingPlan,
        BigDecimal labourHours,
        BigDecimal partsCost,
        BigDecimal labourCost,
        BigDecimal discount,
        BigDecimal totalCost,
        LocalDateTime completedAt
) {
}
