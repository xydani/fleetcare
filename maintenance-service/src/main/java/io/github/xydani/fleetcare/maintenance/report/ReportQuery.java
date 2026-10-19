package io.github.xydani.fleetcare.maintenance.report;

import java.time.LocalDate;
import java.util.List;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import io.github.xydani.fleetcare.maintenance.report.dto.BrandAverageCost;
import io.github.xydani.fleetcare.maintenance.report.dto.MonthlySpending;
import io.github.xydani.fleetcare.maintenance.report.dto.TopIntervention;
import io.github.xydani.fleetcare.maintenance.report.dto.WeekdayLoad;

/**
 * Cost and workload reports. Brands come from the vehicle snapshot stored on each booking,
 * because the vehicle table lives in the vehicle service's database.
 */
@Repository
public class ReportQuery {

    private static final String AVERAGE_COST_BY_BRAND_SQL = """
            SELECT b.vehicle_brand            AS brand,
                   count(*)                   AS interventions,
                   ROUND(AVG(i.total_cost), 2) AS average_cost,
                   SUM(i.total_cost)          AS total_spent
            FROM intervention i
            JOIN booking b ON b.id = i.booking_id
            GROUP BY b.vehicle_brand
            HAVING count(*) >= :minInterventions
            ORDER BY average_cost DESC, brand
            """;

    /* RANK() restarts at 1 for every brand; equal costs share a rank, so a brand can return more rows. */
    private static final String TOP_INTERVENTIONS_BY_BRAND_SQL = """
            WITH ranked AS (
                SELECT b.vehicle_brand AS brand,
                       RANK() OVER (PARTITION BY b.vehicle_brand ORDER BY i.total_cost DESC) AS cost_rank,
                       i.id            AS intervention_id,
                       b.vehicle_plate AS plate,
                       b.service_type,
                       i.total_cost,
                       i.completed_at::date AS completed_on
                FROM intervention i
                JOIN booking b ON b.id = i.booking_id
            )
            SELECT *
            FROM ranked
            WHERE cost_rank <= :limit
            ORDER BY brand, cost_rank, intervention_id
            """;

    /* generate_series lists all twelve months, so months without work show up as zero. */
    private static final String MONTHLY_SPENDING_SQL = """
            WITH months AS (
                SELECT generate_series(make_date(:year, 1, 1), make_date(:year, 12, 1), interval '1 month')::date
                       AS month
            ),
            spending AS (
                SELECT date_trunc('month', completed_at)::date AS month,
                       count(*)                                AS interventions,
                       SUM(total_cost)                         AS spent
                FROM intervention
                WHERE completed_at >= make_date(:year, 1, 1)
                  AND completed_at <  make_date(:year + 1, 1, 1)
                GROUP BY 1
            )
            SELECT m.month,
                   COALESCE(s.interventions, 0)                           AS interventions,
                   COALESCE(s.spent, 0)                                   AS spent,
                   SUM(COALESCE(s.spent, 0)) OVER (ORDER BY m.month)      AS cumulative_spent
            FROM months m
            LEFT JOIN spending s USING (month)
            ORDER BY m.month
            """;

    /* 'FMDAY' prints the English day name in capitals without padding, e.g. MONDAY, matching DayOfWeek. */
    private static final String WORKSHOP_LOAD_SQL = """
            WITH slots AS (
                SELECT starts_at,
                       EXTRACT(EPOCH FROM ends_at - starts_at) / 3600 AS hours
                FROM booking
                WHERE status <> 'CANCELLED'
                  AND starts_at >= :from
                  AND starts_at <  :to
            )
            SELECT to_char(starts_at, 'FMDAY')                          AS day_of_week,
                   count(*)                                             AS bookings,
                   ROUND(SUM(hours), 2)                                 AS booked_hours,
                   ROUND(100 * SUM(hours) / SUM(SUM(hours)) OVER (), 1) AS share
            FROM slots
            GROUP BY day_of_week
            ORDER BY MIN(EXTRACT(ISODOW FROM starts_at))
            """;

    private final JdbcClient jdbcClient;

    public ReportQuery(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<BrandAverageCost> averageCostByBrand(int minInterventions) {
        return jdbcClient.sql(AVERAGE_COST_BY_BRAND_SQL)
                .param("minInterventions", minInterventions)
                .query(BrandAverageCost.class)
                .list();
    }

    public List<TopIntervention> topInterventionsByBrand(int limit) {
        return jdbcClient.sql(TOP_INTERVENTIONS_BY_BRAND_SQL)
                .param("limit", limit)
                .query(TopIntervention.class)
                .list();
    }

    public List<MonthlySpending> monthlySpending(int year) {
        return jdbcClient.sql(MONTHLY_SPENDING_SQL)
                .param("year", year)
                .query(MonthlySpending.class)
                .list();
    }

    /**
     * @param from first day included
     * @param to   first day excluded
     */
    public List<WeekdayLoad> workshopLoad(LocalDate from, LocalDate to) {
        return jdbcClient.sql(WORKSHOP_LOAD_SQL)
                .param("from", from.atStartOfDay())
                .param("to", to.atStartOfDay())
                .query(WeekdayLoad.class)
                .list();
    }
}
