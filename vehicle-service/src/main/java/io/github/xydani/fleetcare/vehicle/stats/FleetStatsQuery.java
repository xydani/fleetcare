package io.github.xydani.fleetcare.vehicle.stats;

import java.time.LocalDate;
import java.util.List;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import io.github.xydani.fleetcare.vehicle.stats.dto.FleetStatsResponse.AgeBand;
import io.github.xydani.fleetcare.vehicle.stats.dto.FleetStatsResponse.BrandCount;
import io.github.xydani.fleetcare.vehicle.stats.dto.FleetStatsResponse.FuelTypeShare;

/**
 * Aggregate queries over the whole fleet.
 */
@Repository
public class FleetStatsQuery {

    /* The window over the grouped rows gives the fleet total without a second query. */
    private static final String BY_FUEL_TYPE_SQL = """
            SELECT fuel_type,
                   count(*)                                             AS vehicles,
                   ROUND(100.0 * count(*) / SUM(count(*)) OVER (), 1)  AS percentage
            FROM vehicle
            GROUP BY fuel_type
            ORDER BY vehicles DESC, fuel_type
            """;

    private static final String BY_AGE_SQL = """
            WITH latest_km AS (
                SELECT DISTINCT ON (vehicle_id) vehicle_id, km
                FROM mileage_reading
                ORDER BY vehicle_id, reading_date DESC
            ),
            aged AS (
                SELECT EXTRACT(YEAR FROM age(:today, v.registration_date))::int AS age_years,
                       lk.km
                FROM vehicle v
                LEFT JOIN latest_km lk ON lk.vehicle_id = v.id
            )
            SELECT CASE
                       WHEN age_years < 4  THEN '0-3 years'
                       WHEN age_years < 8  THEN '4-7 years'
                       WHEN age_years < 12 THEN '8-11 years'
                       ELSE '12+ years'
                   END                    AS age_band,
                   count(*)               AS vehicles,
                   ROUND(AVG(km))::int    AS average_km
            FROM aged
            GROUP BY age_band
            ORDER BY MIN(age_years)
            """;

    private static final String TOP_BRANDS_SQL = """
            SELECT brand, count(*) AS vehicles
            FROM vehicle
            GROUP BY brand
            ORDER BY vehicles DESC, brand
            LIMIT :limit
            """;

    private final JdbcClient jdbcClient;

    public FleetStatsQuery(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<FuelTypeShare> countByFuelType() {
        return jdbcClient.sql(BY_FUEL_TYPE_SQL).query(FuelTypeShare.class).list();
    }

    public List<AgeBand> countByAge(LocalDate today) {
        return jdbcClient.sql(BY_AGE_SQL).param("today", today).query(AgeBand.class).list();
    }

    public List<BrandCount> topBrands(int limit) {
        return jdbcClient.sql(TOP_BRANDS_SQL).param("limit", limit).query(BrandCount.class).list();
    }
}
