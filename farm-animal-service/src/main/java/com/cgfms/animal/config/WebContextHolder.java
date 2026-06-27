package com.cgfms.animal.config;

import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Holds the current ServerWebExchange in a reactive Context so that
 * GlobalExceptionHandler can access the request path without blocking.
 */
public class WebContextHolder {

    private static final String EXCHANGE_KEY = "serverWebExchange";

    private WebContextHolder() {}

    /**
     * Returns a Mono that makes its value available downstream
     * in the Reactor Context under {@link #EXCHANGE_KEY}.
     */
    public static Mono<ServerWebExchange> getExchange() {
        return Mono.deferContextual(ctx -> ctx.hasKey(EXCHANGE_KEY) ? Mono.just(ctx.get(EXCHANGE_KEY)) : Mono.empty());
    }

    /**
     * Utility to write the exchange into the reactor context.
     */
    public static <T> Mono<T> withExchange(Mono<T> source, ServerWebExchange exchange) {
        return source.contextWrite(ctx -> ctx.put(EXCHANGE_KEY, exchange));
    }
}
