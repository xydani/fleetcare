package io.github.xydani.fleetcare.maintenance.common.error;

/**
 * The request clashes with existing data, such as a duplicate unique value. Mapped to HTTP 409.
 */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
