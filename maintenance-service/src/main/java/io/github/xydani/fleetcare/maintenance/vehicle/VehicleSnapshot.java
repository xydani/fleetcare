package io.github.xydani.fleetcare.maintenance.vehicle;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * Copy of the vehicle's identifying data, taken from the vehicle service when a booking is made.
 * Each service owns its database, so reports here cannot join the vehicle table: they group by
 * these columns instead. The copy also keeps history accurate if the vehicle is later changed.
 */
@Embeddable
public record VehicleSnapshot(
        @Column(name = "vehicle_plate", nullable = false, length = 7) String plate,
        @Column(name = "vehicle_brand", nullable = false, length = 60) String brand,
        @Column(name = "vehicle_model", nullable = false, length = 60) String model
) {
}
