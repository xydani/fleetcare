package io.github.xydani.fleetcare.maintenance.vehicle;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Declarative HTTP client for the vehicle service. Error responses are translated by
 * {@link VehicleClientErrorDecoder}; use it through {@link VehicleLookup}.
 */
@FeignClient(name = "vehicle-service", url = "${fleetcare.clients.vehicle-service-url}",
        configuration = VehicleClientConfiguration.class)
interface VehicleClient {

    @GetMapping("/api/vehicles/{id}")
    VehicleDto getVehicle(@PathVariable Long id);

    /**
     * The fields this service needs from the vehicle detail; the rest of the response is ignored.
     */
    record VehicleDto(Long id, String plate, String brand, String model) {
    }
}
