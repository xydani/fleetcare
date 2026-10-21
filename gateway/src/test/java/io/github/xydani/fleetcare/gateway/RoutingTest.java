package io.github.xydani.fleetcare.gateway;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;

/**
 * Starts the real gateway with three WireMock servers standing in for the services, each answering
 * with its own name, so a test can tell where a request ended up.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RoutingTest {

    @RegisterExtension
    static final WireMockExtension vehicleService = backend();

    @RegisterExtension
    static final WireMockExtension maintenanceService = backend();

    @RegisterExtension
    static final WireMockExtension notificationService = backend();

    @DynamicPropertySource
    static void serviceUrls(DynamicPropertyRegistry registry) {
        registry.add("VEHICLE_SERVICE_URL", vehicleService::baseUrl);
        registry.add("MAINTENANCE_SERVICE_URL", maintenanceService::baseUrl);
        registry.add("NOTIFICATION_SERVICE_URL", notificationService::baseUrl);
    }

    @LocalServerPort
    private int port;

    private WebTestClient client;

    @BeforeEach
    void setUp() {
        client = WebTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
        answerWithName(vehicleService, "vehicle");
        answerWithName(maintenanceService, "maintenance");
        answerWithName(notificationService, "notification");
    }

    @ParameterizedTest(name = "{0} -> {1}")
    @CsvSource({
            "/api/vehicles/1,                      vehicle",
            "/api/vehicles/inspections/expiring,   vehicle",
            "/api/owners,                          vehicle",
            "/api/bookings/5,                      maintenance",
            "/api/interventions?vehicleId=7,       maintenance",
            "/api/reports/monthly-spending,        maintenance",
            "/api/notifications/unread-count,      notification",
            "/api/events/stats,                    notification"
    })
    void shouldRouteEachApiToItsService(String path, String service) {
        client.get().uri(path).exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo(service);
    }

    @Test
    void shouldForwardMethodBodyAndStatus() {
        notificationService.stubFor(post(urlEqualTo("/api/reminders/inspections"))
                .willReturn(aResponse().withStatus(503).withBody("vehicle service down")));

        client.post().uri("/api/reminders/inspections").exchange()
                .expectStatus().isEqualTo(503)
                .expectBody(String.class).isEqualTo("vehicle service down");
    }

    @Test
    void shouldReturn404_whenNoRouteMatches() {
        client.get().uri("/api/unknown").exchange().expectStatus().isNotFound();
    }

    private static WireMockExtension backend() {
        return WireMockExtension.newInstance().options(wireMockConfig().dynamicPort()).build();
    }

    private static void answerWithName(WireMockExtension backend, String name) {
        backend.stubFor(get(anyUrl()).willReturn(aResponse().withBody(name)));
    }
}
