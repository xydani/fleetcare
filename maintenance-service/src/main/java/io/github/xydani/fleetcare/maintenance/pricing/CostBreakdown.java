package io.github.xydani.fleetcare.maintenance.pricing;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Billed amounts of an intervention, in euros with two decimals.
 * {@code total = parts + labour - discount}.
 */
public record CostBreakdown(BigDecimal partsCost, BigDecimal labourCost, BigDecimal discount, BigDecimal totalCost) {

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
