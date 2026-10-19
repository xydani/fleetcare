package io.github.xydani.fleetcare.maintenance.booking;

import java.net.URI;

import jakarta.validation.Valid;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import io.github.xydani.fleetcare.maintenance.booking.dto.BookingRequest;
import io.github.xydani.fleetcare.maintenance.booking.dto.BookingResponse;
import io.github.xydani.fleetcare.maintenance.common.web.PageResponse;

@RestController
@RequestMapping("/api/bookings")
@Tag(name = "Bookings", description = "Workshop slots and their lifecycle")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @Operation(summary = "List bookings", description = "Paginated, optionally filtered by vehicle and status.")
    @GetMapping
    public PageResponse<BookingResponse> findAll(
            @RequestParam(required = false) Long vehicleId,
            @RequestParam(required = false) BookingStatus status,
            @PageableDefault(size = 20, sort = "startsAt", direction = Sort.Direction.ASC) Pageable pageable) {
        return bookingService.findAll(vehicleId, status, pageable);
    }

    @Operation(summary = "Get a booking")
    @GetMapping("/{id}")
    public BookingResponse findById(@PathVariable Long id) {
        return bookingService.findById(id);
    }

    @Operation(summary = "Book a workshop slot",
            description = "The vehicle must exist in the vehicle service (404). The slot must be in the future, "
                    + "on a weekday and within opening hours (422), and must not clash with another booking "
                    + "of the vehicle or exceed the workshop's bays (409).")
    @PostMapping
    public ResponseEntity<BookingResponse> create(@Valid @RequestBody BookingRequest request) {
        BookingResponse created = bookingService.create(request);
        return ResponseEntity.created(URI.create("/api/bookings/" + created.id())).body(created);
    }

    @Operation(summary = "Start the work", description = "BOOKED -> IN_PROGRESS.")
    @PostMapping("/{id}/start")
    public BookingResponse start(@PathVariable Long id) {
        return bookingService.start(id);
    }

    @Operation(summary = "Cancel a booking", description = "Allowed until the work is completed.")
    @PostMapping("/{id}/cancel")
    public BookingResponse cancel(@PathVariable Long id) {
        return bookingService.cancel(id);
    }
}
