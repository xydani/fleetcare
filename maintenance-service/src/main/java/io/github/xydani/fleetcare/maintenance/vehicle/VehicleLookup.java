package io.github.xydani.fleetcare.maintenance.vehicle;

import org.springframework.stereotype.Component;

import feign.RetryableException;

import io.github.xydani.fleetcare.maintenance.common.error.ServiceUnavailableException;

/**
 * The only entry point to the vehicle service. Hides Feign from the rest of the code.
 */
@Component
public class VehicleLookup {

    private final VehicleClient vehicleClient;

    VehicleLookup(VehicleClient vehicleClient) {
        this.vehicleClient = vehicleClient;
    }

    /**
     * @throws VehicleNotFoundException    if the vehicle does not exist
     * @throws ServiceUnavailableException if the vehicle service is down, slow or failing
     */
    public VehicleSnapshot snapshotOf(Long vehicleId) {
        try {
            var vehicle = vehicleClient.getVehicle(vehicleId);
            return new VehicleSnapshot(vehicle.plate(), vehicle.brand(), vehicle.model());
        } catch (RetryableException ex) {
            // connection refused or timed out: raised by Feign before any response is decoded
            throw new ServiceUnavailableException("Vehicle service is unreachable", ex);
        }
    }
}
