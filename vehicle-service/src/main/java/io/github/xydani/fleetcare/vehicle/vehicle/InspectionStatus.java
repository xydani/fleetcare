package io.github.xydani.fleetcare.vehicle.vehicle;

import java.time.LocalDate;

public enum InspectionStatus {
    VALID,
    DUE_SOON,
    OVERDUE;

    /** Days before the due date in which an inspection counts as due soon. */
    public static final int DUE_SOON_DAYS = 30;

    public static InspectionStatus of(LocalDate dueDate, LocalDate today) {
        if (dueDate.isBefore(today)) {
            return OVERDUE;
        }
        if (dueDate.isAfter(today.plusDays(DUE_SOON_DAYS))) {
            return VALID;
        }
        return DUE_SOON;
    }
}
