package io.github.xydani.fleetcare.maintenance.pricing;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

/**
 * Picks the {@link CostCalculationStrategy} for a pricing plan. Spring injects every strategy bean;
 * the application fails at startup if a plan has no strategy or two of them.
 */
@Component
public class CostStrategyResolver {

    private final Map<PricingPlan, CostCalculationStrategy> strategies = new EnumMap<>(PricingPlan.class);

    public CostStrategyResolver(List<CostCalculationStrategy> strategies) {
        for (CostCalculationStrategy strategy : strategies) {
            if (this.strategies.putIfAbsent(strategy.plan(), strategy) != null) {
                throw new IllegalStateException("More than one cost strategy for plan " + strategy.plan());
            }
        }
        for (PricingPlan plan : PricingPlan.values()) {
            if (!this.strategies.containsKey(plan)) {
                throw new IllegalStateException("No cost strategy for plan " + plan);
            }
        }
    }

    public CostCalculationStrategy resolve(PricingPlan plan) {
        return strategies.get(plan);
    }
}
