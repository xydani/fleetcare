package io.github.xydani.fleetcare.gateway.filter;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

/**
 * Gives every routed request an {@code X-Correlation-Id}, passes it to the service and returns it to the
 * client, so one id ties together the logs of a request across services. Also logs each request with its
 * status and duration.
 */
@Component
public class CorrelationIdFilter implements GlobalFilter, Ordered {

    public static final String HEADER = "X-Correlation-Id";

    /* A client-supplied id is reused only if it is short and plain, so it cannot inject text into the logs. */
    private static final Pattern VALID_ID = Pattern.compile("[A-Za-z0-9._-]{1,64}");

    private static final Logger log = LoggerFactory.getLogger(CorrelationIdFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String correlationId = correlationIdOf(exchange.getRequest());
        ServerHttpRequest request = exchange.getRequest().mutate()
                .headers(headers -> headers.set(HEADER, correlationId))
                .build();
        exchange.getResponse().beforeCommit(() -> {
            exchange.getResponse().getHeaders().set(HEADER, correlationId);
            return Mono.empty();
        });

        long start = System.nanoTime();
        return chain.filter(exchange.mutate().request(request).build())
                .doFinally(signal -> log.info("{} {} -> {} in {} ms [{}]",
                        request.getMethod(),
                        request.getURI().getRawPath(),
                        exchange.getResponse().getStatusCode(),
                        TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start),
                        correlationId));
    }

    /** Runs before every other filter, so even a failing request carries the id. */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    private static String correlationIdOf(ServerHttpRequest request) {
        String incoming = request.getHeaders().getFirst(HEADER);
        return incoming != null && VALID_ID.matcher(incoming).matches() ? incoming : UUID.randomUUID().toString();
    }
}
