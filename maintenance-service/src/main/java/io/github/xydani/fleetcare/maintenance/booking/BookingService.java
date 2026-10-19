package io.github.xydani.fleetcare.maintenance.booking;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.xydani.fleetcare.maintenance.booking.dto.BookingRequest;
import io.github.xydani.fleetcare.maintenance.booking.dto.BookingResponse;
import io.github.xydani.fleetcare.maintenance.common.error.BusinessRuleException;
import io.github.xydani.fleetcare.maintenance.common.error.ConflictException;
import io.github.xydani.fleetcare.maintenance.common.web.PageResponse;
import io.github.xydani.fleetcare.maintenance.vehicle.VehicleLookup;
import io.github.xydani.fleetcare.maintenance.vehicle.VehicleSnapshot;

@Service
@Transactional(readOnly = true)
public class BookingService {

    private final BookingRepository bookingRepository;
    private final VehicleLookup vehicleLookup;
    private final BookingMapper bookingMapper;
    private final WorkshopProperties workshop;
    private final Clock clock;

    public BookingService(BookingRepository bookingRepository, VehicleLookup vehicleLookup,
                          BookingMapper bookingMapper, WorkshopProperties workshop, Clock clock) {
        this.bookingRepository = bookingRepository;
        this.vehicleLookup = vehicleLookup;
        this.bookingMapper = bookingMapper;
        this.workshop = workshop;
        this.clock = clock;
    }

    public PageResponse<BookingResponse> findAll(Long vehicleId, BookingStatus status, Pageable pageable) {
        var spec = BookingSpecifications.withFilters(vehicleId, status);
        return PageResponse.of(bookingRepository.findAll(spec, pageable), bookingMapper::toResponse);
    }

    public BookingResponse findById(Long id) {
        return bookingMapper.toResponse(getBooking(id));
    }

    /**
     * Books a slot after checking, in order: opening hours (422), that the vehicle exists (404),
     * that it has no other booking at that time (409) and that a bay is free (409).
     */
    @Transactional
    public BookingResponse create(BookingRequest request) {
        LocalDateTime startsAt = request.startsAt();
        LocalDateTime endsAt = startsAt.plus(durationOf(request));
        checkWithinOpeningHours(startsAt, endsAt);

        VehicleSnapshot vehicle = vehicleLookup.snapshotOf(request.vehicleId());

        if (bookingRepository.existsOverlappingForVehicle(request.vehicleId(), startsAt, endsAt,
                BookingStatus.ACTIVE)) {
            throw vehicleAlreadyBooked(request.vehicleId());
        }
        // Conservative: counts every booking touching the slot, even ones that do not overlap each other.
        if (bookingRepository.countOverlapping(startsAt, endsAt, BookingStatus.ACTIVE) >= workshop.bays()) {
            throw new ConflictException("No free bay between %s and %s".formatted(startsAt, endsAt));
        }

        Booking booking = new Booking(request.vehicleId(), vehicle, request.serviceType(), startsAt, endsAt,
                request.notes());
        try {
            return bookingMapper.toResponse(bookingRepository.saveAndFlush(booking));
        } catch (DataIntegrityViolationException ex) {
            // a concurrent request booked the same vehicle after our check: the exclusion constraint caught it
            throw vehicleAlreadyBooked(request.vehicleId());
        }
    }

    @Transactional
    public BookingResponse start(Long id) {
        Booking booking = getBooking(id);
        booking.start();
        return bookingMapper.toResponse(booking);
    }

    @Transactional
    public BookingResponse cancel(Long id) {
        Booking booking = getBooking(id);
        booking.cancel();
        return bookingMapper.toResponse(booking);
    }

    public Booking getBooking(Long id) {
        return bookingRepository.findById(id).orElseThrow(() -> new BookingNotFoundException(id));
    }

    private static Duration durationOf(BookingRequest request) {
        return request.durationMinutes() == null
                ? request.serviceType().defaultDuration()
                : Duration.ofMinutes(request.durationMinutes());
    }

    private void checkWithinOpeningHours(LocalDateTime startsAt, LocalDateTime endsAt) {
        if (!startsAt.isAfter(LocalDateTime.now(clock))) {
            throw new BusinessRuleException("A booking must start in the future");
        }
        DayOfWeek day = startsAt.getDayOfWeek();
        if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
            throw new BusinessRuleException("The workshop is closed at weekends");
        }
        if (startsAt.toLocalTime().isBefore(workshop.opensAt())
                || !endsAt.toLocalDate().equals(startsAt.toLocalDate())
                || endsAt.toLocalTime().isAfter(workshop.closesAt())) {
            throw new BusinessRuleException("A booking must fit within opening hours, %s-%s"
                    .formatted(workshop.opensAt(), workshop.closesAt()));
        }
    }

    private static ConflictException vehicleAlreadyBooked(Long vehicleId) {
        return new ConflictException("Vehicle " + vehicleId + " already has a booking at that time");
    }
}
