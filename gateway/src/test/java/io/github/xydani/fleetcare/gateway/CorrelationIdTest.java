package io.github.xydani.fleetcare.gateway;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;

import io.github.xydani.fleetcare.gateway.filter.CorrelationIdFilter;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CorrelationIdTest {

    @RegisterExtension
    static final WireMockExtension vehicleService = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    @DynamicPropertySource
    static void serviceUrls(DynamicPropertyRegistry registry) {
        registry.add("VEHICLE_SERVICE_URL", vehicleService::baseUrl);
    }

    @LocalServerPort
    private int port;

    private WebTestClient client;

    @BeforeEach
    void setUp() {
        client = WebTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
        vehicleService.stubFor(get(anyUrl()).willReturn(aResponse().withBody("ok")));
    }

    @Test
    void shouldGenerateId_andSendItToTheServiceAndBackToTheClient() {
        // when
        String returned = client.get().uri("/api/vehicles/1").exchange()
                .expectStatus().isOk()
                .returnResult(String.class).getResponseHeaders().getFirst(CorrelationIdFilter.HEADER);

        // then
        assertThat(UUID.fromString(returned)).isNotNull();
        vehicleService.verify(getRequestedFor(urlEqualTo("/api/vehicles/1"))
                .withHeader(CorrelationIdFilter.HEADER, equalTo(returned)));
    }

    @Test
    void shouldKeepTheClientsId_whenItIsValid() {
        client.get().uri("/api/vehicles/1").header(CorrelationIdFilter.HEADER, "checkout-42").exchange()
                .expectHeader().valueEquals(CorrelationIdFilter.HEADER, "checkout-42");

        vehicleService.verify(getRequestedFor(urlEqualTo("/api/vehicles/1"))
                .withHeader(CorrelationIdFilter.HEADER, equalTo("checkout-42")));
    }

    @Test
    void shouldReplaceTheClientsId_whenItCouldPolluteTheLogs() {
        String returned = client.get().uri("/api/vehicles/1")
                .header(CorrelationIdFilter.HEADER, "fake\" status=200 user=admin").exchange()
                .returnResult(String.class).getResponseHeaders().getFirst(CorrelationIdFilter.HEADER);

        assertThat(UUID.fromString(returned)).isNotNull();
    }

    @Test
    void shouldReturnId_evenWhenTheServiceFails() {
        vehicleService.stubFor(get(urlEqualTo("/api/vehicles/99")).willReturn(aResponse().withStatus(500)));

        client.get().uri("/api/vehicles/99").exchange()
                .expectStatus().is5xxServerError()
                .expectHeader().exists(CorrelationIdFilter.HEADER);
    }
}
