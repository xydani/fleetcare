package io.github.xydani.fleetcare.maintenance.pricing;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Workshop tariffs, from {@code fleetcare.pricing.*}.
 *
 * @param hourlyRate           labour price per hour, in euros
 * @param fleetDiscountPercent discount applied to the whole bill of fleet customers
 */
@Validated
@ConfigurationProperties("fleetcare.pricing")
public record PricingProperties(
        @NotNull @PositiveOrZero BigDecimal hourlyRate,
        @NotNull @PositiveOrZero @DecimalMax("100") BigDecimal fleetDiscountPercent
) {

    BigDecimal labourCost(BigDecimal hours) {
        return hourlyRate.multiply(hours);
    }
}
