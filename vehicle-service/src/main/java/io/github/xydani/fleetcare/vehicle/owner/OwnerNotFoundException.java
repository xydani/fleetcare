package io.github.xydani.fleetcare.vehicle.owner;

import io.github.xydani.fleetcare.vehicle.common.error.NotFoundException;

public class OwnerNotFoundException extends NotFoundException {

    public OwnerNotFoundException(Long id) {
        super("Owner " + id + " not found");
    }
}
