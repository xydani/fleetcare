package io.github.xydani.fleetcare.maintenance.common.error;

/**
 * A service this one depends on cannot be reached or answered with an error. Mapped to HTTP 503.
 */
public class ServiceUnavailableException extends RuntimeException {

    public ServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
