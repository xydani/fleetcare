package io.github.xydani.fleetcare.maintenance.intervention;

import org.springframework.data.jpa.domain.Specification;

import io.github.xydani.fleetcare.maintenance.pricing.PricingPlan;

/**
 * Optional filters for the intervention list. A null argument means "no filter".
 */
final class InterventionSpecifications {

    private InterventionSpecifications() {
    }

    static Specification<Intervention> withFilters(Long vehicleId, PricingPlan pricingPlan) {
        return Specification.allOf(hasVehicle(vehicleId), hasPricingPlan(pricingPlan));
    }

    static Specification<Intervention> hasVehicle(Long vehicleId) {
        return (root, query, cb) -> vehicleId == null
                ? null
                : cb.equal(root.get("booking").get("vehicleId"), vehicleId);
    }

    static Specification<Intervention> hasPricingPlan(PricingPlan pricingPlan) {
        return (root, query, cb) -> pricingPlan == null ? null : cb.equal(root.get("pricingPlan"), pricingPlan);
    }
}
