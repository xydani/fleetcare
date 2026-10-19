package io.github.xydani.fleetcare.maintenance.intervention.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import io.github.xydani.fleetcare.maintenance.pricing.PricingPlan;

/**
 * What the mechanic reports when the work on a booking is finished.
 */
public record CompletionRequest(
        @NotBlank @Size(max = 1000) String description,
        @NotNull @PositiveOrZero @Digits(integer = 8, fraction = 2) BigDecimal partsCost,
        @NotNull @PositiveOrZero @DecimalMax("99.99") @Digits(integer = 2, fraction = 2) BigDecimal labourHours,
        @NotNull PricingPlan pricingPlan
) {
}
