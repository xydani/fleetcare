package io.github.xydani.fleetcare.maintenance.pricing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class CostStrategyResolverTest {

    private final PricingProperties pricing = new PricingProperties(new BigDecimal("48.00"), new BigDecimal("15"));

    @ParameterizedTest
    @EnumSource(PricingPlan.class)
    void shouldResolveStrategyOfTheSamePlan_forEveryPlan(PricingPlan plan) {
        // given
        var resolver = new CostStrategyResolver(List.of(
                new StandardCostStrategy(pricing),
                new WarrantyCostStrategy(pricing),
                new FleetDiscountStrategy(pricing)));

        // when / then
        assertThat(resolver.resolve(plan).plan()).isEqualTo(plan);
    }

    @Test
    void shouldFailFast_whenAPlanHasNoStrategy() {
        assertThatThrownBy(() -> new CostStrategyResolver(List.of(new StandardCostStrategy(pricing))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("No cost strategy for plan WARRANTY");
    }

    @Test
    void shouldFailFast_whenAPlanHasTwoStrategies() {
        assertThatThrownBy(() -> new CostStrategyResolver(List.of(
                new StandardCostStrategy(pricing), new StandardCostStrategy(pricing))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("More than one cost strategy for plan STANDARD");
    }
}
