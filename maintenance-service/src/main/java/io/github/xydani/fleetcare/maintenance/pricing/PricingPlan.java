package io.github.xydani.fleetcare.maintenance.pricing;

/**
 * How an intervention is billed. Each plan has its own {@link CostCalculationStrategy}.
 */
public enum PricingPlan {
    /** Parts and labour at list price. */
    STANDARD,
    /** Parts are covered by the manufacturer's warranty: only labour is billed. */
    WARRANTY,
    /** Business customers with a fleet contract get a percentage discount on the whole bill. */
    FLEET
}
