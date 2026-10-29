package io.github.xydani.fleetcare.notification.notification;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Component;

import io.github.xydani.fleetcare.notification.event.EventType;
import io.github.xydani.fleetcare.notification.event.InvalidEventException;

/**
 * Factory pattern: callers hand over an event type and its raw payload and get a ready notification,
 * without knowing which fields each type needs or how its message is worded. A new event type means
 * a new enum constant and a new case here, and the compiler points out the missing case.
 */
@Component
public class NotificationFactory {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");

    private final Clock clock;

    public NotificationFactory(Clock clock) {
        this.clock = clock;
    }

    /**
     * @throws InvalidEventException if the payload lacks a field the type needs
     */
    public Notification create(EventType type, Map<String, Object> payload) {
        Payload p = new Payload(type, payload);
        return switch (type) {
            case INSPECTION_DUE -> inspectionDue(p);
            case BOOKING_CONFIRMED -> bookingConfirmed(p);
            case MAINTENANCE_COMPLETED -> maintenanceCompleted(p);
        };
    }

    private Notification inspectionDue(Payload p) {
        long vehicleId = p.number("vehicleId");
        String vehicle = p.text("brand") + " " + p.text("model") + " " + p.text("plate");
        LocalDate dueDate = p.date("dueDate");
        boolean overdue = p.flag("overdue");

        String title;
        String message;
        if (overdue) {
            title = "Inspection overdue";
            message = "The inspection of %s expired on %s. The vehicle must not be driven until it passes a new one."
                    .formatted(vehicle, DATE.format(dueDate));
        } else {
            title = "Inspection due soon";
            message = "The inspection of %s is due by %s.".formatted(vehicle, DATE.format(dueDate));
        }
        // one reminder per vehicle and due date, however often the scheduler runs
        return notification(EventType.INSPECTION_DUE, vehicleId, p.text("plate"), p.optionalText("ownerEmail"),
                title, message, "INSPECTION_DUE:" + vehicleId + ":" + dueDate);
    }

    private Notification bookingConfirmed(Payload p) {
        LocalDateTime startsAt = p.dateTime("startsAt");
        String message = "%s is booked for %s on %s at %s.".formatted(p.text("plate"),
                humanize(p.text("serviceType")), DATE.format(startsAt), TIME.format(startsAt));
        return notification(EventType.BOOKING_CONFIRMED, p.number("vehicleId"), p.text("plate"), null,
                "Workshop booking confirmed", message, "BOOKING_CONFIRMED:" + p.number("bookingId"));
    }

    private Notification maintenanceCompleted(Payload p) {
        String message = "The work on %s is done. Total: € %s.".formatted(p.text("plate"),
                p.amount("totalCost").toPlainString());
        return notification(EventType.MAINTENANCE_COMPLETED, p.number("vehicleId"), p.text("plate"), null,
                "Maintenance completed", message, "MAINTENANCE_COMPLETED:" + p.number("interventionId"));
    }

    private Notification notification(EventType type, long vehicleId, String plate, String recipient, String title,
                                       String message, String dedupKey) {
        return new Notification(type, vehicleId, plate, recipient, title, message, dedupKey, Instant.now(clock));
    }

    /* SCHEDULED_SERVICE -> "scheduled service" */
    private static String humanize(String constant) {
        return constant.toLowerCase(Locale.ROOT).replace('_', ' ');
    }

    /**
     * Typed, validated access to the untyped JSON payload.
     */
    private record Payload(EventType type, Map<String, Object> values) {

        Object required(String field) {
            Object value = valueOf(field);
            if (value == null || value.toString().isBlank()) {
                throw new InvalidEventException("%s event needs '%s' in its payload".formatted(type, field));
            }
            return value;
        }

        String text(String field) {
            return required(field).toString();
        }

        String optionalText(String field) {
            Object value = valueOf(field);
            return value == null ? null : value.toString();
        }

        /* The whole payload may be missing from the request, not just one field. */
        private Object valueOf(String field) {
            return values == null ? null : values.get(field);
        }

        long number(String field) {
            if (required(field) instanceof Number number) {
                return number.longValue();
            }
            throw invalid(field, "a number");
        }

        boolean flag(String field) {
            if (required(field) instanceof Boolean flag) {
                return flag;
            }
            throw invalid(field, "true or false");
        }

        BigDecimal amount(String field) {
            if (required(field) instanceof Number number) {
                return new BigDecimal(number.toString()).setScale(2, RoundingMode.HALF_EVEN);
            }
            throw invalid(field, "a number");
        }

        LocalDate date(String field) {
            try {
                return LocalDate.parse(text(field));
            } catch (DateTimeParseException ex) {
                throw invalid(field, "a date such as 2026-11-05");
            }
        }

        LocalDateTime dateTime(String field) {
            try {
                return LocalDateTime.parse(text(field));
            } catch (DateTimeParseException ex) {
                throw invalid(field, "a date-time such as 2026-11-05T09:00");
            }
        }

        private InvalidEventException invalid(String field, String expected) {
            return new InvalidEventException("%s event needs '%s' to be %s".formatted(type, field, expected));
        }
    }
}
