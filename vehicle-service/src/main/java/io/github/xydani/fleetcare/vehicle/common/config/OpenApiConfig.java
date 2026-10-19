package io.github.xydani.fleetcare.vehicle.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

@Configuration(proxyBeanMethods = false)
public class OpenApiConfig {

    @Bean
    OpenAPI vehicleServiceOpenApi() {
        return new OpenAPI().info(new Info()
                .title("FleetCare Vehicle Service")
                .version("v1")
                .description("Vehicles, owners, mileage readings and inspections of the FleetCare fleet.")
                .license(new License().name("MIT").url("https://opensource.org/licenses/MIT")));
    }
}
