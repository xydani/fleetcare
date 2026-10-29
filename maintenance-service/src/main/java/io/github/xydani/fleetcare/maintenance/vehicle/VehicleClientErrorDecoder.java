package io.github.xydani.fleetcare.maintenance.vehicle;

import feign.Response;
import feign.codec.ErrorDecoder;

import io.github.xydani.fleetcare.maintenance.common.error.ServiceUnavailableException;

/**
 * Maps the vehicle service's error responses to this service's exceptions:
 * 404 means the vehicle does not exist, anything else means the service cannot be relied on.
 */
class VehicleClientErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {
        if (response.status() == 404) {
            return new VehicleNotFoundException(vehicleIdOf(response));
        }
        return new ServiceUnavailableException("Vehicle service answered with HTTP " + response.status());
    }

    /* The id is the last segment of the requested URL, e.g. /api/vehicles/42. */
    private static Long vehicleIdOf(Response response) {
        String url = response.request().url();
        return Long.valueOf(url.substring(url.lastIndexOf('/') + 1));
    }
}
