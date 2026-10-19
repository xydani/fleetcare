package io.github.xydani.fleetcare.maintenance.pricing;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

@Component
class FleetDiscountStrategy implements CostCalculationStrategy {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final PricingProperties pricing;

    FleetDiscountStrategy(PricingProperties pricing) {
        this.pricing = pricing;
    }

    @Override
    public PricingPlan plan() {
        return PricingPlan.FLEET;
    }

    @Override
    public CostBreakdown calculate(BigDecimal partsCost, BigDecimal labourHours) {
        BigDecimal labourCost = pricing.labourCost(labourHours);
        BigDecimal discount = partsCost.add(labourCost)
                .multiply(pricing.fleetDiscountPercent())
                .divide(HUNDRED);
        return CostBreakdown.of(partsCost, labourCost, discount);
    }
}
