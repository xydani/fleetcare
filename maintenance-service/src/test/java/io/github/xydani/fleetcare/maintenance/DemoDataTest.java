package io.github.xydani.fleetcare.maintenance;

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
    void shouldLoadHistoryAndUpcomingBookings_whenDemoProfileIsActive() {
        assertThat(count("SELECT count(*) FROM booking WHERE status = 'COMPLETED'")).isEqualTo(120);
        assertThat(count("SELECT count(*) FROM intervention")).isEqualTo(120);
        assertThat(count("SELECT count(*) FROM booking WHERE status = 'BOOKED' AND starts_at > now()")).isEqualTo(7);
    }

    @Test
    void shouldPlaceEverySlotOnAWeekdayWithinOpeningHours() {
        assertThat(count("""
                SELECT count(*) FROM booking
                WHERE EXTRACT(ISODOW FROM starts_at) > 5
                   OR starts_at::time < '08:00' OR ends_at::time > '18:00'
                   OR ends_at::date <> starts_at::date
                """)).isZero();
    }

    @Test
    void shouldKeepTotalsConsistent_andUseEveryPricingPlan() {
        assertThat(count("""
                SELECT count(*) FROM intervention
                WHERE total_cost <> parts_cost + labour_cost - discount
                """)).isZero();
        assertThat(count("SELECT count(DISTINCT pricing_plan) FROM intervention")).isEqualTo(3);
    }

    private long count(String sql) {
        return jdbc.queryForObject(sql, Long.class);
    }
}
