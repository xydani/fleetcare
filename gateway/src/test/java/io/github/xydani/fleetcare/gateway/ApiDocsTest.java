package io.github.xydani.fleetcare.gateway;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApiDocsTest {

    @RegisterExtension
    static final WireMockExtension maintenanceService = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    @DynamicPropertySource
    static void serviceUrls(DynamicPropertyRegistry registry) {
        registry.add("MAINTENANCE_SERVICE_URL", maintenanceService::baseUrl);
    }

    @LocalServerPort
    private int port;

    private WebTestClient client;

    @BeforeEach
    void setUp() {
        client = WebTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
    }

    @Test
    void shouldServeEachServicesOpenApiDocument_underItsPrefix() {
        maintenanceService.stubFor(get(urlEqualTo("/v3/api-docs"))
                .willReturn(okJson("{\"info\": {\"title\": \"FleetCare Maintenance Service\"}}")));

        client.get().uri("/maintenance-service/v3/api-docs").exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.info.title").isEqualTo("FleetCare Maintenance Service");
    }

    @Test
    void shouldListAllServicesInTheSwaggerUiConfig() {
        client.get().uri("/v3/api-docs/swagger-config").exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.urls.length()").isEqualTo(3)
                .jsonPath("$.urls[?(@.name == 'vehicle-service')].url").isEqualTo("/vehicle-service/v3/api-docs");
    }

    @Test
    void shouldServeTheSwaggerUi() {
        client.get().uri("/webjars/swagger-ui/index.html").exchange()
                .expectStatus().isOk();
    }
}
