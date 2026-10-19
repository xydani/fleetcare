package io.github.xydani.fleetcare.maintenance.intervention;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.springframework.context.ApplicationEventPublisher;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.xydani.fleetcare.maintenance.booking.Booking;
import io.github.xydani.fleetcare.maintenance.booking.BookingService;
import io.github.xydani.fleetcare.maintenance.booking.BookingStatus;
import io.github.xydani.fleetcare.maintenance.booking.ServiceType;
import io.github.xydani.fleetcare.maintenance.common.error.BusinessRuleException;
import io.github.xydani.fleetcare.maintenance.intervention.dto.CompletionRequest;
import io.github.xydani.fleetcare.maintenance.pricing.CostBreakdown;
import io.github.xydani.fleetcare.maintenance.pricing.CostCalculationStrategy;
import io.github.xydani.fleetcare.maintenance.pricing.CostStrategyResolver;
import io.github.xydani.fleetcare.maintenance.pricing.PricingPlan;
import io.github.xydani.fleetcare.maintenance.vehicle.VehicleSnapshot;

@ExtendWith(MockitoExtension.class)
class InterventionServiceTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-26T10:30:00Z"), ZoneId.of("Europe/Rome"));
    private static final LocalDateTime NINE = LocalDateTime.of(2026, 10, 26, 9, 0);

    @Mock
    private InterventionRepository interventionRepository;

    @Mock
    private BookingService bookingService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private InterventionService interventionService;

    private final CompletionRequest request =
            new CompletionRequest("Front pads and discs", new BigDecimal("120.00"), new BigDecimal("1.5"),
                    PricingPlan.WARRANTY);

    @BeforeEach
    void setUp() {
        // a fake warranty strategy keeps this test about the service, not about the pricing rules
        CostCalculationStrategy warranty = new CostCalculationStrategy() {
            @Override
            public PricingPlan plan() {
                return PricingPlan.WARRANTY;
            }

            @Override
            public CostBreakdown calculate(BigDecimal partsCost, BigDecimal labourHours) {
                return CostBreakdown.of(partsCost, new BigDecimal("72"), partsCost);
            }
        };
        var resolver = new CostStrategyResolver(List.of(warranty, plan(PricingPlan.STANDARD), plan(PricingPlan.FLEET)));
        interventionService = new InterventionService(interventionRepository, bookingService, resolver,
                Mappers.getMapper(InterventionMapper.class), eventPublisher, CLOCK);
    }

    @Test
    void shouldCompleteBookingAndPriceIntervention_whenBookingIsInProgress() {
        // given
        Booking booking = booking();
        booking.start();
        when(bookingService.getBooking(1L)).thenReturn(booking);
        when(interventionRepository.save(any(Intervention.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        var response = interventionService.complete(1L, request);

        // then
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.COMPLETED);
        assertThat(response.pricingPlan()).isEqualTo(PricingPlan.WARRANTY);
        assertThat(response.totalCost()).isEqualByComparingTo("72.00");
        assertThat(response.plate()).isEqualTo("FG482KL");
        assertThat(response.serviceType()).isEqualTo(ServiceType.BRAKES);
        assertThat(response.completedAt()).isEqualTo(LocalDateTime.of(2026, 10, 26, 11, 30));
        verify(eventPublisher).publishEvent(any(InterventionCompletedEvent.class));
    }

    @Test
    void shouldRefuseCompletion_whenWorkHasNotStarted() {
        // given
        when(bookingService.getBooking(1L)).thenReturn(booking());

        // when / then
        assertThatThrownBy(() -> interventionService.complete(1L, request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("from BOOKED to COMPLETED");
        verify(interventionRepository, never()).save(any());
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void shouldThrowNotFound_whenInterventionDoesNotExist() {
        // given
        when(interventionRepository.findWithBookingById(9L)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> interventionService.findById(9L))
                .isInstanceOf(InterventionNotFoundException.class)
                .hasMessage("Intervention 9 not found");
    }

    private static Booking booking() {
        return new Booking(7L, new VehicleSnapshot("FG482KL", "Fiat", "Panda"), ServiceType.BRAKES, NINE,
                NINE.plusHours(2), null);
    }

    private static CostCalculationStrategy plan(PricingPlan plan) {
        return new CostCalculationStrategy() {
            @Override
            public PricingPlan plan() {
                return plan;
            }

            @Override
            public CostBreakdown calculate(BigDecimal partsCost, BigDecimal labourHours) {
                throw new UnsupportedOperationException();
            }
        };
    }
}
