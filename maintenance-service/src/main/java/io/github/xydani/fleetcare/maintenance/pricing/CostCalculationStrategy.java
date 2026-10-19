package io.github.xydani.fleetcare.maintenance.pricing;

import java.math.BigDecimal;

/**
 * Strategy pattern: one implementation per {@link PricingPlan}. A new plan is a new class,
 * picked up by {@link CostStrategyResolver} without touching the services that use it.
 */
public interface CostCalculationStrategy {

    PricingPlan plan();

    CostBreakdown calculate(BigDecimal partsCost, BigDecimal labourHours);
}
