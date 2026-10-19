package io.github.xydani.fleetcare.maintenance.report.dto;

import java.math.BigDecimal;

public record BrandAverageCost(String brand, long interventions, BigDecimal averageCost, BigDecimal totalSpent) {
}
