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

    private final Map<PricingPlan, CostCalculationStrategy> strategiesByPlan = new EnumMap<>(PricingPlan.class);

    public CostStrategyResolver(List<CostCalculationStrategy> strategies) {
        for (CostCalculationStrategy strategy : strategies) {
            if (strategiesByPlan.containsKey(strategy.plan())) {
                throw new IllegalStateException("More than one cost strategy for plan " + strategy.plan());
            }
            strategiesByPlan.put(strategy.plan(), strategy);
        }
        for (PricingPlan plan : PricingPlan.values()) {
            if (!strategiesByPlan.containsKey(plan)) {
                throw new IllegalStateException("No cost strategy for plan " + plan);
            }
        }
    }

    public CostCalculationStrategy resolve(PricingPlan plan) {
        return strategiesByPlan.get(plan);
    }
}
