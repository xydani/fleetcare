package io.github.xydani.fleetcare.maintenance.report.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import io.github.xydani.fleetcare.maintenance.booking.ServiceType;

/**
 * @param costRank 1 for the most expensive intervention of the brand; ties share a rank
 */
public record TopIntervention(String brand, int costRank, Long interventionId, String plate, ServiceType serviceType,
                              BigDecimal totalCost, LocalDate completedOn) {
}
