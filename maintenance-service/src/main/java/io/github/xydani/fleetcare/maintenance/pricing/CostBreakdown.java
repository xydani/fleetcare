package io.github.xydani.fleetcare.maintenance.pricing;

import java.math.BigDecimal;
import java.math.RoundingMode;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * Billed amounts of an intervention, in euros with two decimals.
 * {@code total = parts + labour - discount}. Stored as part of an intervention.
 */
@Embeddable
public record CostBreakdown(
        @Column(name = "parts_cost", nullable = false) BigDecimal partsCost,
        @Column(name = "labour_cost", nullable = false) BigDecimal labourCost,
        @Column(nullable = false) BigDecimal discount,
        @Column(name = "total_cost", nullable = false) BigDecimal totalCost
) {

    public static CostBreakdown of(BigDecimal partsCost, BigDecimal labourCost, BigDecimal discount) {
        BigDecimal parts = money(partsCost);
        BigDecimal labour = money(labourCost);
        BigDecimal off = money(discount);
        return new CostBreakdown(parts, labour, off, parts.add(labour).subtract(off));
    }

    static BigDecimal money(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_EVEN);
    }
}
