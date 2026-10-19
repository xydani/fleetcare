package io.github.xydani.fleetcare.maintenance.booking;

import org.springframework.data.jpa.domain.Specification;

/**
 * Optional filters for the booking list. A null argument means "no filter".
 */
final class BookingSpecifications {

    private BookingSpecifications() {
    }

    static Specification<Booking> withFilters(Long vehicleId, BookingStatus status) {
        return Specification.allOf(hasVehicle(vehicleId), hasStatus(status));
    }

    static Specification<Booking> hasVehicle(Long vehicleId) {
        return (root, query, cb) -> vehicleId == null ? null : cb.equal(root.get("vehicleId"), vehicleId);
    }

    static Specification<Booking> hasStatus(BookingStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }
}
