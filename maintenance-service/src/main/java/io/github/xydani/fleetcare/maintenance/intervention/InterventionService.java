package io.github.xydani.fleetcare.maintenance.intervention;

import java.time.Clock;
import java.time.LocalDateTime;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.xydani.fleetcare.maintenance.booking.Booking;
import io.github.xydani.fleetcare.maintenance.booking.BookingService;
import io.github.xydani.fleetcare.maintenance.common.web.PageResponse;
import io.github.xydani.fleetcare.maintenance.intervention.dto.CompletionRequest;
import io.github.xydani.fleetcare.maintenance.intervention.dto.InterventionResponse;
import io.github.xydani.fleetcare.maintenance.pricing.CostBreakdown;
import io.github.xydani.fleetcare.maintenance.pricing.CostStrategyResolver;
import io.github.xydani.fleetcare.maintenance.pricing.PricingPlan;

@Service
@Transactional(readOnly = true)
public class InterventionService {

    private final InterventionRepository interventionRepository;
    private final BookingService bookingService;
    private final CostStrategyResolver costStrategyResolver;
    private final InterventionMapper interventionMapper;
    private final Clock clock;

    public InterventionService(InterventionRepository interventionRepository, BookingService bookingService,
                               CostStrategyResolver costStrategyResolver, InterventionMapper interventionMapper,
                               Clock clock) {
        this.interventionRepository = interventionRepository;
        this.bookingService = bookingService;
        this.costStrategyResolver = costStrategyResolver;
        this.interventionMapper = interventionMapper;
        this.clock = clock;
    }

    public PageResponse<InterventionResponse> findAll(Long vehicleId, PricingPlan pricingPlan, Pageable pageable) {
        var spec = InterventionSpecifications.withFilters(vehicleId, pricingPlan);
        return PageResponse.of(interventionRepository.findAll(spec, pageable), interventionMapper::toResponse);
    }

    public InterventionResponse findById(Long id) {
        return interventionRepository.findWithBookingById(id)
                .map(interventionMapper::toResponse)
                .orElseThrow(() -> new InterventionNotFoundException(id));
    }

    /**
     * Completes an in-progress booking and records the intervention, priced by the strategy of the
     * requested plan. Fails with 422 if the booking is not in progress.
     */
    @Transactional
    public InterventionResponse complete(Long bookingId, CompletionRequest request) {
        Booking booking = bookingService.getBooking(bookingId);
        booking.complete();

        CostBreakdown cost = costStrategyResolver.resolve(request.pricingPlan())
                .calculate(request.partsCost(), request.labourHours());
        Intervention intervention = new Intervention(booking, request.description(), request.pricingPlan(),
                request.labourHours(), cost, LocalDateTime.now(clock));
        return interventionMapper.toResponse(interventionRepository.save(intervention));
    }
}
