package io.github.xydani.fleetcare.maintenance.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Enables {@code @Async}. Boot's auto-configured executor runs the tasks on virtual threads
 * when {@code spring.threads.virtual.enabled} is set, on a bounded pool otherwise.
 */
@Configuration(proxyBeanMethods = false)
@EnableAsync
public class AsyncConfig {
}
