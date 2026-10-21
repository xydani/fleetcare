package io.github.xydani.fleetcare.notification.reminder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import feign.FeignException;
import feign.Request;
import feign.RetryableException;

import io.github.xydani.fleetcare.notification.common.error.ServiceUnavailableException;
import io.github.xydani.fleetcare.notification.event.EventService;
import io.github.xydani.fleetcare.notification.event.EventType;
import io.github.xydani.fleetcare.notification.event.dto.EventRequest;
import io.github.xydani.fleetcare.notification.event.dto.EventResponse;
import io.github.xydani.fleetcare.notification.reminder.VehicleClient.InspectionDue;

@ExtendWith(MockitoExtension.class)
class InspectionReminderJobTest {

    private static final InspectionDue PANDA = new InspectionDue(1L, "FG482KL", "Fiat", "Panda",
            "giulia.bianchi@example.it", LocalDate.of(2026, 11, 5), false);
    private static final InspectionDue GOLF = new InspectionDue(3L, "EZ307MB", "Volkswagen", "Golf", null,
            LocalDate.of(2026, 10, 2), true);

    @Mock
    private VehicleClient vehicleClient;

    @Mock
    private EventService eventService;

    private InspectionReminderJob job;

    @BeforeEach
    void setUp() {
        job = new InspectionReminderJob(vehicleClient, eventService, new ReminderProperties("-", 30));
    }

    @Test
    void shouldSendOneInspectionDueEventPerVehicle_countingRepeats() {
        // given: the Golf was already reminded for this due date by an earlier run
        when(vehicleClient.findExpiring(30)).thenReturn(List.of(PANDA, GOLF));
        when(eventService.ingest(any(EventRequest.class)))
                .thenReturn(new EventResponse("e-1", "n-1", false))
                .thenReturn(new EventResponse("e-2", "n-0", true));

        // when
        var run = job.run();

        // then
        assertThat(run.vehiclesDue()).isEqualTo(2);
        assertThat(run.created()).isEqualTo(1);
        assertThat(run.alreadySent()).isEqualTo(1);
    }

    @Test
    void shouldPassInspectionDetailsAsPayload_evenWithoutOwnerEmail() {
        // given
        when(vehicleClient.findExpiring(30)).thenReturn(List.of(GOLF));
        when(eventService.ingest(any(EventRequest.class))).thenReturn(new EventResponse("e-1", "n-1", false));

        // when
        job.run();

        // then
        ArgumentCaptor<EventRequest> event = ArgumentCaptor.forClass(EventRequest.class);
        verify(eventService).ingest(event.capture());
        assertThat(event.getValue().type()).isEqualTo(EventType.INSPECTION_DUE);
        assertThat(event.getValue().source()).isEqualTo("notification-service");
        assertThat(event.getValue().payload())
                .containsEntry("plate", "EZ307MB")
                .containsEntry("dueDate", "2026-10-02")
                .containsEntry("overdue", true)
                .containsEntry("ownerEmail", null);
    }

    @Test
    void shouldThrowServiceUnavailable_whenVehicleServiceIsDown() {
        // given
        when(vehicleClient.findExpiring(30)).thenThrow(unreachable());

        // when / then
        assertThatThrownBy(() -> job.run()).isInstanceOf(ServiceUnavailableException.class);
        verifyNoInteractions(eventService);
    }

    @Test
    void shouldNotFail_whenScheduledRunCannotReachVehicleService() {
        // given
        when(vehicleClient.findExpiring(30)).thenThrow(unreachable());

        // when: no exception escapes the scheduler thread
        job.runScheduled();

        // then
        verifyNoInteractions(eventService);
    }

    private static FeignException unreachable() {
        Request request = Request.create(Request.HttpMethod.GET,
                "http://vehicle-service/api/vehicles/inspections/expiring", Map.of(), null, null, null);
        return new RetryableException(-1, "Connection refused", Request.HttpMethod.GET, (Long) null, request);
    }
}
