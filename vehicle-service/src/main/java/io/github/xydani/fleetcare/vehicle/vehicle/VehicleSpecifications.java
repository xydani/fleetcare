package io.github.xydani.fleetcare.vehicle.vehicle;

import org.springframework.data.jpa.domain.Specification;

/**
 * Optional filters for the vehicle list. A null argument means "no filter".
 */
final class VehicleSpecifications {

    private VehicleSpecifications() {
    }

    static Specification<Vehicle> withFilters(String brand, FuelType fuelType, Long ownerId) {
        return Specification.allOf(hasBrand(brand), hasFuelType(fuelType), hasOwner(ownerId));
    }

    static Specification<Vehicle> hasBrand(String brand) {
        return (root, query, cb) -> brand == null || brand.isBlank()
                ? null
                : cb.equal(cb.lower(root.get("brand")), brand.toLowerCase());
    }

    static Specification<Vehicle> hasFuelType(FuelType fuelType) {
        return (root, query, cb) -> fuelType == null ? null : cb.equal(root.get("fuelType"), fuelType);
    }

    static Specification<Vehicle> hasOwner(Long ownerId) {
        return (root, query, cb) -> ownerId == null ? null : cb.equal(root.get("owner").get("id"), ownerId);
    }
}
