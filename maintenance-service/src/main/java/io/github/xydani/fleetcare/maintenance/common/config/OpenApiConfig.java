package io.github.xydani.fleetcare.maintenance.common.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;

/**
 * The server URL is relative, so "Try it out" calls whichever host serves the Swagger UI:
 * this service directly, or the gateway when the document is viewed through it.
 */
@Configuration(proxyBeanMethods = false)
public class OpenApiConfig {

    @Bean
    OpenAPI maintenanceServiceOpenApi() {
        return new OpenAPI().info(new Info()
                .title("FleetCare Maintenance Service")
                .version("v1")
                .description("Workshop bookings, completed interventions and cost reports of the FleetCare fleet.")
                .license(new License().name("MIT").url("https://opensource.org/licenses/MIT")))
                .servers(List.of(new Server().url("/")));
    }
}
