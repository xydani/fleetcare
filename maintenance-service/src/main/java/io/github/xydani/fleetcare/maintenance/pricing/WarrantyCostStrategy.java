package io.github.xydani.fleetcare.maintenance.pricing;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

/**
 * Parts are reimbursed by the manufacturer, so the customer pays only the labour.
 * The parts cost is still recorded as a discount, to keep the real value of the work visible.
 */
@Component
class WarrantyCostStrategy implements CostCalculationStrategy {

    private final PricingProperties pricing;

    WarrantyCostStrategy(PricingProperties pricing) {
        this.pricing = pricing;
    }

    @Override
    public PricingPlan plan() {
        return PricingPlan.WARRANTY;
    }

    @Override
    public CostBreakdown calculate(BigDecimal partsCost, BigDecimal labourHours) {
        return CostBreakdown.of(partsCost, pricing.labourCost(labourHours), partsCost);
    }
}
