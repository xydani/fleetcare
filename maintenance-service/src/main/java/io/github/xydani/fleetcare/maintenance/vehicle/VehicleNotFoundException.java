package io.github.xydani.fleetcare.maintenance.vehicle;

import io.github.xydani.fleetcare.maintenance.common.error.NotFoundException;

public class VehicleNotFoundException extends NotFoundException {

    public VehicleNotFoundException(Long id) {
        super("Vehicle " + id + " not found");
    }
}
