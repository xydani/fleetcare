package io.github.xydani.fleetcare.maintenance.booking;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import io.github.xydani.fleetcare.maintenance.booking.dto.BookingRequest;
import io.github.xydani.fleetcare.maintenance.booking.dto.BookingResponse;
import io.github.xydani.fleetcare.maintenance.common.error.BusinessRuleException;
import io.github.xydani.fleetcare.maintenance.common.error.ConflictException;
import io.github.xydani.fleetcare.maintenance.common.web.PageResponse;
import io.github.xydani.fleetcare.maintenance.vehicle.VehicleNotFoundException;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    private static final String BODY = """
            {"vehicleId": 7, "serviceType": "BRAKES", "startsAt": "2026-10-26T09:00:00"}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookingService bookingService;

    @Test
    void shouldReturn201WithLocation_whenBookingIsCreated() throws Exception {
        when(bookingService.create(any(BookingRequest.class))).thenReturn(response(5L, BookingStatus.BOOKED));

        mockMvc.perform(post("/api/bookings").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/bookings/5"))
                .andExpect(jsonPath("$.plate").value("FG482KL"))
                .andExpect(jsonPath("$.endsAt").value("2026-10-26T11:00:00"));
    }

    @Test
    void shouldReturn400_whenRequiredFieldsAreMissing() throws Exception {
        mockMvc.perform(post("/api/bookings").contentType(MediaType.APPLICATION_JSON).content("{\"durationMinutes\": 5}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.vehicleId").exists())
                .andExpect(jsonPath("$.errors.serviceType").exists())
                .andExpect(jsonPath("$.errors.startsAt").exists())
                .andExpect(jsonPath("$.errors.durationMinutes").exists());
    }

    @Test
    void shouldReturn404_whenVehicleDoesNotExist() throws Exception {
        when(bookingService.create(any(BookingRequest.class))).thenThrow(new VehicleNotFoundException(7L));

        mockMvc.perform(post("/api/bookings").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Vehicle 7 not found"));
    }

    @Test
    void shouldReturn409_whenSlotIsTaken() throws Exception {
        when(bookingService.create(any(BookingRequest.class)))
                .thenThrow(new ConflictException("Vehicle 7 already has a booking at that time"));

        mockMvc.perform(post("/api/bookings").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldReturnFilteredPage_whenListingBookings() throws Exception {
        when(bookingService.findAll(eq(7L), eq(BookingStatus.BOOKED), any(Pageable.class)))
                .thenReturn(new PageResponse<>(List.of(response(5L, BookingStatus.BOOKED)), 0, 20, 1, 1));

        mockMvc.perform(get("/api/bookings").param("vehicleId", "7").param("status", "BOOKED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(5))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void shouldReturnUpdatedBooking_whenStarted() throws Exception {
        when(bookingService.start(5L)).thenReturn(response(5L, BookingStatus.IN_PROGRESS));

        mockMvc.perform(post("/api/bookings/5/start"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    void shouldReturn422_whenTransitionIsNotAllowed() throws Exception {
        when(bookingService.cancel(5L))
                .thenThrow(new BusinessRuleException("Booking 5 cannot move from COMPLETED to CANCELLED"));

        mockMvc.perform(post("/api/bookings/5/cancel"))
                .andExpect(status().isUnprocessableContent());
    }

    private static BookingResponse response(Long id, BookingStatus status) {
        LocalDateTime startsAt = LocalDateTime.of(2026, 10, 26, 9, 0);
        return new BookingResponse(id, 7L, "FG482KL", "Fiat", "Panda", ServiceType.BRAKES, startsAt,
                startsAt.plusHours(2), status, null, Instant.parse("2026-10-19T08:00:00Z"));
    }
}
