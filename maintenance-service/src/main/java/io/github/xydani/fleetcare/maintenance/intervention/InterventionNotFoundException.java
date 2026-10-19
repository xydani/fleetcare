package io.github.xydani.fleetcare.maintenance.intervention;

import io.github.xydani.fleetcare.maintenance.common.error.NotFoundException;

public class InterventionNotFoundException extends NotFoundException {

    public InterventionNotFoundException(Long id) {
        super("Intervention " + id + " not found");
    }
}
