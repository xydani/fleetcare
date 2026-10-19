package io.github.xydani.fleetcare.maintenance.vehicle;

import org.springframework.context.annotation.Bean;

import feign.codec.ErrorDecoder;

/**
 * Applies only to {@link VehicleClient}: deliberately not a {@code @Configuration},
 * so it is not picked up by component scanning and shared with other Feign clients.
 */
class VehicleClientConfiguration {

    @Bean
    ErrorDecoder vehicleClientErrorDecoder() {
        return new VehicleClientErrorDecoder();
    }
}
