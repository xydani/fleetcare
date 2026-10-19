package io.github.xydani.fleetcare.vehicle;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("demo")
class DemoDataTest {

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void shouldLoadOwnersAndVehicles_whenDemoProfileIsActive() {
        assertThat(count("SELECT count(*) FROM owner")).isEqualTo(12);
        assertThat(count("SELECT count(*) FROM vehicle")).isEqualTo(30);
    }

    @Test
    void shouldAssignIdsInListedOrder_soOtherServicesCanReferToThem() {
        assertThat(jdbc.queryForObject("SELECT plate FROM vehicle WHERE id = 1", String.class)).isEqualTo("FG482KL");
        assertThat(jdbc.queryForObject("SELECT plate FROM vehicle WHERE id = 30", String.class)).isEqualTo("GP036XE");
        assertThat(count("SELECT count(*) FROM vehicle WHERE owner_id IS NULL")).isZero();
    }

    @Test
    void shouldHaveNonDecreasingMileage_forEveryVehicle() {
        var decreasing = count("""
                SELECT count(*) FROM (
                    SELECT km - LAG(km) OVER (PARTITION BY vehicle_id ORDER BY reading_date) AS delta
                    FROM mileage_reading
                ) d
                WHERE d.delta < 0
                """);

        assertThat(count("SELECT count(*) FROM mileage_reading")).isGreaterThan(100);
        assertThat(decreasing).isZero();
    }

    @Test
    void shouldIncludeExpiredAndSoonExpiringInspections() {
        assertThat(count("SELECT count(*) FROM inspection WHERE expiry_date < CURRENT_DATE")).isPositive();
        assertThat(count("""
                SELECT count(*) FROM inspection
                WHERE expiry_date BETWEEN CURRENT_DATE AND CURRENT_DATE + 30
                """)).isPositive();
    }

    private long count(String sql) {
        return jdbc.queryForObject(sql, Long.class);
    }
}
