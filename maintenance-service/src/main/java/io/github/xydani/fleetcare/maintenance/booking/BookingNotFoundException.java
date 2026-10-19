package io.github.xydani.fleetcare.maintenance.booking;

import io.github.xydani.fleetcare.maintenance.common.error.NotFoundException;

public class BookingNotFoundException extends NotFoundException {

    public BookingNotFoundException(Long id) {
        super("Booking " + id + " not found");
    }
}
