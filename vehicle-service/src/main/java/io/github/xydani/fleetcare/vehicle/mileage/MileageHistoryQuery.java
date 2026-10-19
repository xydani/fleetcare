package io.github.xydani.fleetcare.vehicle.mileage;

import java.util.List;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import io.github.xydani.fleetcare.vehicle.mileage.dto.MileageHistoryEntry;

/**
 * Mileage history computed in SQL with window functions.
 */
@Repository
public class MileageHistoryQuery {

    private static final String HISTORY_SQL = """
            WITH deltas AS (
                SELECT reading_date,
                       km,
                       km - LAG(km) OVER w                     AS km_since_previous,
                       reading_date - LAG(reading_date) OVER w AS days_since_previous
                FROM mileage_reading
                WHERE vehicle_id = :vehicleId
                WINDOW w AS (ORDER BY reading_date)
            )
            SELECT reading_date,
                   km,
                   km_since_previous,
                   days_since_previous,
                   ROUND(AVG(km_since_previous) OVER (
                       ORDER BY reading_date ROWS BETWEEN 2 PRECEDING AND CURRENT ROW
                   ))::int AS moving_average_km
            FROM deltas
            ORDER BY reading_date
            """;

    private final JdbcClient jdbcClient;

    public MileageHistoryQuery(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<MileageHistoryEntry> findHistory(Long vehicleId) {
        return jdbcClient.sql(HISTORY_SQL)
                .param("vehicleId", vehicleId)
                .query(MileageHistoryEntry.class)
                .list();
    }
}
