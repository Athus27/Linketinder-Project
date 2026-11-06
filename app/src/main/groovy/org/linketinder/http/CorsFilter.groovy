package org.linketinder.http

import com.sun.net.httpserver.Filter
import com.sun.net.httpserver.HttpExchange

class CorsFilter extends Filter {
    private final String allowedOrigin
    private final List<String> allowedMethods

    CorsFilter(String allowedOrigin, List<String> allowedMethods) {
        this.allowedOrigin = allowedOrigin
        this.allowedMethods = allowedMethods
    }

    @Override
    String description() {
        return "Autoriza a origem do frontend e responde ao preflight CORS"
    }

    @Override
    void doFilter(HttpExchange exchange, Filter.Chain chain) throws IOException {
        String origin = exchange.requestHeaders.getFirst("Origin")
        exchange.responseHeaders.add("Vary", "Origin")

        if (origin != null && origin != allowedOrigin) {
            terminar(exchange, 403)
            return
        }

        if (origin == allowedOrigin) {
            exchange.responseHeaders.set("Access-Control-Allow-Origin", allowedOrigin)
        }

        if (exchange.requestMethod == "OPTIONS") {
            String requestedMethod = exchange.requestHeaders.getFirst("Access-Control-Request-Method")
            String requestedHeaders = exchange.requestHeaders.getFirst("Access-Control-Request-Headers")
            boolean headersAllowed = !requestedHeaders || requestedHeaders.split(",").every {
                it.trim().equalsIgnoreCase("Content-Type")
            }

            if (origin != allowedOrigin || !allowedMethods.contains(requestedMethod) || !headersAllowed) {
                terminar(exchange, 403)
                return
            }

            exchange.responseHeaders.set("Access-Control-Allow-Methods", allowedMethods.join(", "))
            exchange.responseHeaders.set("Access-Control-Allow-Headers", "Content-Type")
            terminar(exchange, 204)
            return
        }

        chain.doFilter(exchange)
    }

    private static void terminar(HttpExchange exchange, int status) {
        exchange.sendResponseHeaders(status, -1)
        exchange.close()
    }
}
