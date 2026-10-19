package io.github.xydani.fleetcare.vehicle.common.error;

/**
 * The request is well formed but breaks a domain rule. Mapped to HTTP 422.
 */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
