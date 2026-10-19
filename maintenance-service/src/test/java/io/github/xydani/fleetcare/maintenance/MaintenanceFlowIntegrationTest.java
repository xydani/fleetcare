package io.github.xydani.fleetcare.maintenance;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;

/**
 * The whole service, wired for real: PostgreSQL in a container, and a WireMock server playing both the
 * vehicle service and the notification service.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class MaintenanceFlowIntegrationTest {

    @RegisterExtension
    static final WireMockExtension otherServices = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    @DynamicPropertySource
    static void otherServicesUrls(DynamicPropertyRegistry registry) {
        registry.add("fleetcare.clients.vehicle-service-url", otherServices::baseUrl);
        registry.add("fleetcare.clients.notification-service-url", otherServices::baseUrl);
    }

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void stubOtherServices() {
        otherServices.stubFor(WireMock.get(urlEqualTo("/api/vehicles/7")).willReturn(okJson("""
                {"id": 7, "plate": "FG482KL", "brand": "Fiat", "model": "Panda"}
                """)));
        otherServices.stubFor(WireMock.get(urlEqualTo("/api/vehicles/404")).willReturn(aResponse().withStatus(404)));
        otherServices.stubFor(post(urlEqualTo("/api/events")).willReturn(aResponse().withStatus(201)));
    }

    @Test
    void shouldBookStartAndCompleteAnIntervention_andNotifyEachStep() throws Exception {
        LocalDateTime slot = nextMondayAt(9);

        // book
        String location = mockMvc.perform(postJson("/api/bookings", """
                        {"vehicleId": 7, "serviceType": "BRAKES", "startsAt": "%s"}
                        """.formatted(slot)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.brand").value("Fiat"))
                .andReturn().getResponse().getHeader("Location");

        // start and complete with the fleet plan: 120 + 1.5 h x 48 = 192, minus 15% = 163.20
        mockMvc.perform(postJson(location + "/start", ""))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
        mockMvc.perform(postJson(location + "/complete", """
                        {"description": "Front pads and discs", "partsCost": 120.00, "labourHours": 1.5,
                         "pricingPlan": "FLEET"}
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalCost").value(163.20));

        // the vehicle's history and the reports see the work
        mockMvc.perform(get("/api/interventions").param("vehicleId", "7"))
                .andExpect(jsonPath("$.content[0].plate").value("FG482KL"));
        mockMvc.perform(get("/api/reports/brands/average-cost").param("minInterventions", "1"))
                .andExpect(jsonPath("$[0].brand").value("Fiat"));

        // and the notification service heard about both steps
        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            otherServices.verify(postRequestedFor(urlEqualTo("/api/events"))
                    .withRequestBody(matchingJsonPath("$.type", equalTo("BOOKING_CONFIRMED"))));
            otherServices.verify(postRequestedFor(urlEqualTo("/api/events"))
                    .withRequestBody(matchingJsonPath("$.type", equalTo("MAINTENANCE_COMPLETED")))
                    .withRequestBody(matchingJsonPath("$.payload.plate", equalTo("FG482KL"))));
        });
    }

    @Test
    void shouldReturn404Problem_whenVehicleIsUnknownToTheVehicleService() throws Exception {
        mockMvc.perform(postJson("/api/bookings", """
                        {"vehicleId": 404, "serviceType": "OIL_CHANGE", "startsAt": "%s"}
                        """.formatted(nextMondayAt(14))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Vehicle 404 not found"));
    }

    @Test
    void shouldStillBook_whenNotificationServiceIsDown() throws Exception {
        otherServices.stubFor(post(urlPathMatching("/api/events")).willReturn(aResponse().withStatus(503)));

        mockMvc.perform(postJson("/api/bookings", """
                        {"vehicleId": 7, "serviceType": "OIL_CHANGE", "startsAt": "%s"}
                        """.formatted(nextMondayAt(16))))
                .andExpect(status().isCreated());
    }

    private static LocalDateTime nextMondayAt(int hour) {
        return LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY)).atTime(hour, 0);
    }

    private static MockHttpServletRequestBuilder postJson(String url, String body) {
        return MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(body);
    }
}
