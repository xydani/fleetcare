package io.github.xydani.fleetcare.vehicle;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class VehicleServiceApplicationTests {

    @Test
    void contextLoads() {
    }
}
