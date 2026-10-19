package io.github.xydani.fleetcare.maintenance.vehicle;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;

import io.github.xydani.fleetcare.maintenance.TestcontainersConfiguration;
import io.github.xydani.fleetcare.maintenance.common.error.ServiceUnavailableException;

/**
 * Runs the real Feign client against a WireMock server that plays the vehicle service.
 */
@SpringBootTest(properties = "spring.cloud.openfeign.client.config.default.read-timeout=500")
@Import(TestcontainersConfiguration.class)
class VehicleLookupTest {

    @RegisterExtension
    static final WireMockExtension vehicleService = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    @DynamicPropertySource
    static void vehicleServiceUrl(DynamicPropertyRegistry registry) {
        registry.add("fleetcare.clients.vehicle-service-url", vehicleService::baseUrl);
    }

    @Autowired
    private VehicleLookup vehicleLookup;

    @Test
    void shouldReturnSnapshot_whenVehicleExists() {
        // given
        vehicleService.stubFor(get(urlEqualTo("/api/vehicles/7")).willReturn(okJson("""
                {"id": 7, "plate": "FG482KL", "brand": "Fiat", "model": "Panda",
                 "currentKm": 98400, "inspectionStatus": "VALID"}
                """)));

        // when
        VehicleSnapshot snapshot = vehicleLookup.snapshotOf(7L);

        // then
        assertThat(snapshot).isEqualTo(new VehicleSnapshot("FG482KL", "Fiat", "Panda"));
    }

    @Test
    void shouldThrowVehicleNotFound_whenVehicleServiceAnswers404() {
        // given
        vehicleService.stubFor(get(urlEqualTo("/api/vehicles/42")).willReturn(aResponse().withStatus(404)));

        // when / then
        assertThatThrownBy(() -> vehicleLookup.snapshotOf(42L))
                .isInstanceOf(VehicleNotFoundException.class)
                .hasMessage("Vehicle 42 not found");
    }

    @Test
    void shouldThrowServiceUnavailable_whenVehicleServiceFails() {
        // given
        vehicleService.stubFor(get(urlEqualTo("/api/vehicles/7")).willReturn(aResponse().withStatus(500)));

        // when / then
        assertThatThrownBy(() -> vehicleLookup.snapshotOf(7L))
                .isInstanceOf(ServiceUnavailableException.class)
                .hasMessage("Vehicle service answered with HTTP 500");
    }

    @Test
    void shouldThrowServiceUnavailable_whenVehicleServiceIsTooSlow() {
        // given
        vehicleService.stubFor(get(urlEqualTo("/api/vehicles/7"))
                .willReturn(okJson("{}").withFixedDelay(1_500)));

        // when / then
        assertThatThrownBy(() -> vehicleLookup.snapshotOf(7L))
                .isInstanceOf(ServiceUnavailableException.class)
                .hasMessage("Vehicle service is unreachable");
    }
}
