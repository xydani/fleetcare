package io.github.xydani.fleetcare.notification;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.jayway.jsonpath.JsonPath;

import io.github.xydani.fleetcare.notification.event.EventLog;
import io.github.xydani.fleetcare.notification.notification.Notification;

/**
 * The whole service against MongoDB in a container, with WireMock playing the vehicle service.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class NotificationFlowIntegrationTest {

    @RegisterExtension
    static final WireMockExtension vehicleService = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    @DynamicPropertySource
    static void vehicleServiceUrl(DynamicPropertyRegistry registry) {
        registry.add("fleetcare.clients.vehicle-service-url", vehicleService::baseUrl);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MongoTemplate mongoTemplate;

    @BeforeEach
    void clean() {
        mongoTemplate.remove(new Query(), Notification.class);
        mongoTemplate.remove(new Query(), EventLog.class);
    }

    @Test
    void shouldTurnMaintenanceEventIntoUnreadNotification_thenMarkItRead() throws Exception {
        // the event exactly as the maintenance service sends it
        String event = """
                {"type": "MAINTENANCE_COMPLETED", "source": "maintenance-service",
                 "payload": {"interventionId": 11, "bookingId": 5, "vehicleId": 7, "plate": "FG482KL",
                             "totalCost": 163.20}}
                """;
        String body = mockMvc.perform(post("/api/events").contentType(MediaType.APPLICATION_JSON).content(event))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.duplicate").value(false))
                .andReturn().getResponse().getContentAsString();
        String notificationId = JsonPath.read(body, "$.notificationId");

        // sending it again creates nothing new
        mockMvc.perform(post("/api/events").contentType(MediaType.APPLICATION_JSON).content(event))
                .andExpect(jsonPath("$.duplicate").value(true))
                .andExpect(jsonPath("$.notificationId").value(notificationId));

        mockMvc.perform(get("/api/notifications").param("unread", "true").param("vehicleId", "7"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].message").value("The work on FG482KL is done. Total: € 163.20."));

        mockMvc.perform(patch("/api/notifications/{id}/read", notificationId))
                .andExpect(jsonPath("$.read").value(true));
        mockMvc.perform(get("/api/notifications/unread-count"))
                .andExpect(jsonPath("$.unread").value(0));

        // both deliveries were logged
        mockMvc.perform(get("/api/events/stats"))
                .andExpect(jsonPath("$[0].type").value("MAINTENANCE_COMPLETED"))
                .andExpect(jsonPath("$[0].count").value(2));
    }

    @Test
    void shouldRemindEachDueInspectionOnce_acrossRepeatedChecks() throws Exception {
        vehicleService.stubFor(WireMock.get(urlPathEqualTo("/api/vehicles/inspections/expiring")).willReturn(okJson("""
                [{"vehicleId": 1, "plate": "FG482KL", "brand": "Fiat", "model": "Panda", "ownerId": 1,
                  "ownerName": "Giulia Bianchi", "ownerEmail": "giulia.bianchi@example.it",
                  "dueDate": "2026-11-05", "daysLeft": 15, "overdue": false},
                 {"vehicleId": 3, "plate": "EZ307MB", "brand": "Volkswagen", "model": "Golf", "ownerId": 2,
                  "ownerName": "Marco Ferrari", "ownerEmail": "marco.ferrari@example.it",
                  "dueDate": "2026-10-02", "daysLeft": -19, "overdue": true}]
                """)));

        mockMvc.perform(post("/api/reminders/inspections"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.created").value(2));
        mockMvc.perform(post("/api/reminders/inspections"))
                .andExpect(jsonPath("$.created").value(0))
                .andExpect(jsonPath("$.alreadySent").value(2));

        mockMvc.perform(get("/api/notifications").param("vehicleId", "3"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Inspection overdue"))
                .andExpect(jsonPath("$.content[0].recipient").value("marco.ferrari@example.it"));
    }

    @Test
    void shouldReturn503_whenVehicleServiceFailsDuringManualCheck() throws Exception {
        vehicleService.stubFor(WireMock.get(urlPathEqualTo("/api/vehicles/inspections/expiring"))
                .willReturn(aResponse().withStatus(500)));

        mockMvc.perform(post("/api/reminders/inspections"))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    void shouldReturn422_whenEventPayloadDoesNotFitItsType() throws Exception {
        mockMvc.perform(post("/api/events").contentType(MediaType.APPLICATION_JSON).content("""
                        {"type": "BOOKING_CONFIRMED", "source": "maintenance-service", "payload": {"bookingId": 5}}
                        """))
                .andExpect(status().isUnprocessableContent());
    }
}
