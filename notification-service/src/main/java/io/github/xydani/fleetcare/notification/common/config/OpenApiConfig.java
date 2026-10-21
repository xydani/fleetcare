package io.github.xydani.fleetcare.notification.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

@Configuration(proxyBeanMethods = false)
public class OpenApiConfig {

    @Bean
    OpenAPI notificationServiceOpenApi() {
        return new OpenAPI().info(new Info()
                .title("FleetCare Notification Service")
                .version("v1")
                .description("Events from the other FleetCare services and the notifications built from them.")
                .license(new License().name("MIT").url("https://opensource.org/licenses/MIT")));
    }
}
