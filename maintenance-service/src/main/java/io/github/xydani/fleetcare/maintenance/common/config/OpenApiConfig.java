package io.github.xydani.fleetcare.maintenance.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

@Configuration(proxyBeanMethods = false)
public class OpenApiConfig {

    @Bean
    OpenAPI maintenanceServiceOpenApi() {
        return new OpenAPI().info(new Info()
                .title("FleetCare Maintenance Service")
                .version("v1")
                .description("Workshop bookings, completed interventions and cost reports of the FleetCare fleet.")
                .license(new License().name("MIT").url("https://opensource.org/licenses/MIT")));
    }
}
