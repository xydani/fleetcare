package io.github.xydani.fleetcare.maintenance.intervention;

import java.time.Clock;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
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

    private static final Logger log = LoggerFactory.getLogger(InterventionService.class);

    private final InterventionRepository interventionRepository;
    private final BookingService bookingService;
    private final CostStrategyResolver costStrategyResolver;
    private final InterventionMapper interventionMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final Clock clock;

    public InterventionService(InterventionRepository interventionRepository, BookingService bookingService,
                               CostStrategyResolver costStrategyResolver, InterventionMapper interventionMapper,
                               ApplicationEventPublisher eventPublisher, Clock clock) {
        this.interventionRepository = interventionRepository;
        this.bookingService = bookingService;
        this.costStrategyResolver = costStrategyResolver;
        this.interventionMapper = interventionMapper;
        this.eventPublisher = eventPublisher;
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
     * Publishes an {@link InterventionCompletedEvent}.
     */
    @Transactional
    public InterventionResponse complete(Long bookingId, CompletionRequest request) {
        Booking booking = bookingService.getBooking(bookingId);
        booking.complete();

        CostBreakdown cost = costStrategyResolver.resolve(request.pricingPlan())
                .calculate(request.partsCost(), request.labourHours());
        Intervention intervention = new Intervention(booking, request.description(), request.pricingPlan(),
                request.labourHours(), cost, LocalDateTime.now(clock));
        intervention = interventionRepository.save(intervention);
        log.info("Booking {} completed: intervention {} priced {} at {}", bookingId, intervention.getId(),
                request.pricingPlan(), cost.totalCost());

        eventPublisher.publishEvent(new InterventionCompletedEvent(intervention.getId(), booking.getId(),
                booking.getVehicleId(), booking.getVehicle().plate(), cost.totalCost()));
        return interventionMapper.toResponse(intervention);
    }
}
