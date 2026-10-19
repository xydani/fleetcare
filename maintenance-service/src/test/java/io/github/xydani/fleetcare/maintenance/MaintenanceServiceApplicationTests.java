package io.github.xydani.fleetcare.maintenance;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class MaintenanceServiceApplicationTests {

    @Test
    void contextLoads() {
    }
}
