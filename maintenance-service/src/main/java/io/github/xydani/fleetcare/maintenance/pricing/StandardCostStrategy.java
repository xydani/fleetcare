package io.github.xydani.fleetcare.maintenance.pricing;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

@Component
class StandardCostStrategy implements CostCalculationStrategy {

    private final PricingProperties pricing;

    StandardCostStrategy(PricingProperties pricing) {
        this.pricing = pricing;
    }

    @Override
    public PricingPlan plan() {
        return PricingPlan.STANDARD;
    }

    @Override
    public CostBreakdown calculate(BigDecimal partsCost, BigDecimal labourHours) {
        return CostBreakdown.of(partsCost, pricing.labourCost(labourHours), BigDecimal.ZERO);
    }
}
