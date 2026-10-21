package io.github.xydani.fleetcare.notification.event;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import io.github.xydani.fleetcare.notification.event.dto.EventRequest;
import io.github.xydani.fleetcare.notification.event.dto.EventResponse;

@WebMvcTest(EventController.class)
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EventService eventService;

    @Test
    void shouldReturn201_whenEventIsAccepted() throws Exception {
        when(eventService.ingest(any(EventRequest.class))).thenReturn(new EventResponse("e-1", "n-1", false));

        mockMvc.perform(post("/api/events").contentType(MediaType.APPLICATION_JSON).content("""
                        {"type": "BOOKING_CONFIRMED", "source": "maintenance-service", "payload": {"bookingId": 5}}
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.notificationId").value("n-1"))
                .andExpect(jsonPath("$.duplicate").value(false));
    }

    @Test
    void shouldReturn400_whenTypeOrSourceIsMissing() throws Exception {
        mockMvc.perform(post("/api/events").contentType(MediaType.APPLICATION_JSON).content("{\"payload\": {}}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.type").exists())
                .andExpect(jsonPath("$.errors.source").exists());
    }

    @Test
    void shouldReturn400_whenTypeIsUnknown() throws Exception {
        mockMvc.perform(post("/api/events").contentType(MediaType.APPLICATION_JSON).content("""
                        {"type": "TYRES_ON_FIRE", "source": "test", "payload": {}}
                        """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn422_whenPayloadDoesNotFitTheType() throws Exception {
        when(eventService.ingest(any(EventRequest.class)))
                .thenThrow(new InvalidEventException("BOOKING_CONFIRMED event needs 'plate' in its payload"));

        mockMvc.perform(post("/api/events").contentType(MediaType.APPLICATION_JSON).content("""
                        {"type": "BOOKING_CONFIRMED", "source": "maintenance-service", "payload": {"bookingId": 5}}
                        """))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.detail").value("BOOKING_CONFIRMED event needs 'plate' in its payload"));
    }
}
