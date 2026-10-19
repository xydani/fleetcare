package io.github.xydani.fleetcare.vehicle.vehicle;

import io.github.xydani.fleetcare.vehicle.common.error.ConflictException;

public class DuplicatePlateException extends ConflictException {

    public DuplicatePlateException(String plate) {
        super("A vehicle with plate " + plate + " is already registered");
    }
}
