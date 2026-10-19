package io.github.xydani.fleetcare.vehicle.owner;

import io.github.xydani.fleetcare.vehicle.common.error.ConflictException;

public class DuplicateEmailException extends ConflictException {

    public DuplicateEmailException(String email) {
        super("An owner with email " + email + " already exists");
    }
}
