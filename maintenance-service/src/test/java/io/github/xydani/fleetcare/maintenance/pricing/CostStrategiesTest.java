package io.github.xydani.fleetcare.maintenance.pricing;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CostStrategiesTest {

    private final PricingProperties pricing = new PricingProperties(new BigDecimal("48.00"), new BigDecimal("15"));

    @ParameterizedTest(name = "parts {0} + {1} h -> total {2}")
    @CsvSource({
            "120.00, 1.5, 192.00",
            "0,      2,   96.00",
            "35.40,  0,   35.40",
            "10.005, 0,   10.00"
    })
    void shouldChargePartsAndLabour_whenPlanIsStandard(BigDecimal parts, BigDecimal hours, BigDecimal total) {
        // when
        CostBreakdown cost = new StandardCostStrategy(pricing).calculate(parts, hours);

        // then
        assertThat(cost.totalCost()).isEqualByComparingTo(total);
        assertThat(cost.discount()).isEqualByComparingTo("0");
    }

    @ParameterizedTest(name = "parts {0} + {1} h -> total {2}")
    @CsvSource({
            "120.00, 1.5, 72.00",
            "980.00, 0,   0.00",
            "0,      1,   48.00"
    })
    void shouldChargeOnlyLabour_whenPlanIsWarranty(BigDecimal parts, BigDecimal hours, BigDecimal total) {
        // when
        CostBreakdown cost = new WarrantyCostStrategy(pricing).calculate(parts, hours);

        // then
        assertThat(cost.totalCost()).isEqualByComparingTo(total);
        assertThat(cost.partsCost()).isEqualByComparingTo(parts);
        assertThat(cost.discount()).isEqualByComparingTo(parts);
    }

    @ParameterizedTest(name = "parts {0} + {1} h -> discount {2}, total {3}")
    @CsvSource({
            "120.00, 1.5, 28.80, 163.20",
            "0,      1,   7.20,  40.80",
            "33.33,  0,   5.00,  28.33"
    })
    void shouldApplyFleetDiscount_whenPlanIsFleet(BigDecimal parts, BigDecimal hours,
                                                  BigDecimal discount, BigDecimal total) {
        // when
        CostBreakdown cost = new FleetDiscountStrategy(pricing).calculate(parts, hours);

        // then
        assertThat(cost.discount()).isEqualByComparingTo(discount);
        assertThat(cost.totalCost()).isEqualByComparingTo(total);
    }
}
