package io.github.xydani.fleetcare.notification.event;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import io.github.xydani.fleetcare.notification.event.dto.EventRequest;
import io.github.xydani.fleetcare.notification.event.dto.EventResponse;

@RestController
@RequestMapping("/api/events")
@Tag(name = "Events", description = "Events reported by the other services")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @Operation(summary = "Report an event",
            description = "Logs the event and creates its notification. Sending the same event twice is safe: "
                    + "the second one is logged but returns the existing notification with duplicate = true. "
                    + "A payload missing a field required by the type is rejected with 422.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventResponse ingest(@Valid @RequestBody EventRequest request) {
        return eventService.ingest(request);
    }
}
