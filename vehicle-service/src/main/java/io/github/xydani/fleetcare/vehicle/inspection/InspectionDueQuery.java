package io.github.xydani.fleetcare.vehicle.inspection;

import java.time.LocalDate;
import java.util.List;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import io.github.xydani.fleetcare.vehicle.inspection.dto.InspectionDue;

/**
 * Finds vehicles whose inspection is overdue or due within a number of days.
 */
@Repository
public class InspectionDueQuery {

    /*
     * DISTINCT ON keeps only the latest inspection per vehicle. Vehicles never inspected
     * are due four years after registration, as required by Italian law.
     */
    private static final String DUE_SQL = """
            WITH latest_inspection AS (
                SELECT DISTINCT ON (vehicle_id) vehicle_id, expiry_date
                FROM inspection
                ORDER BY vehicle_id, inspection_date DESC
            ),
            due AS (
                SELECT v.id AS vehicle_id,
                       COALESCE(li.expiry_date, (v.registration_date + INTERVAL '4 years')::date) AS due_date
                FROM vehicle v
                LEFT JOIN latest_inspection li ON li.vehicle_id = v.id
            )
            SELECT v.id                                  AS vehicle_id,
                   v.plate,
                   v.brand,
                   v.model,
                   o.id                                  AS owner_id,
                   o.first_name || ' ' || o.last_name    AS owner_name,
                   o.email                               AS owner_email,
                   d.due_date,
                   d.due_date - :today                   AS days_left,
                   d.due_date < :today                   AS overdue
            FROM due d
            JOIN vehicle v ON v.id = d.vehicle_id
            JOIN owner o ON o.id = v.owner_id
            WHERE d.due_date <= :today + :days
            ORDER BY d.due_date, v.plate
            """;

    private final JdbcClient jdbcClient;

    public InspectionDueQuery(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<InspectionDue> findDueWithin(int days, LocalDate today) {
        return jdbcClient.sql(DUE_SQL)
                .param("today", today)
                .param("days", days)
                .query(InspectionDue.class)
                .list();
    }
}
