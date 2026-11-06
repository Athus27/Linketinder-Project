package org.linketinder.auth

import com.sun.net.httpserver.HttpExchange

final class AuthSupport {
    private AuthSupport() {
    }

    static String bearerToken(HttpExchange exchange) {
        String value = exchange.requestHeaders.getFirst("Authorization")
        if (!value || !value.startsWith("Bearer ")) {
            return null
        }
        String token = value.substring("Bearer ".length()).trim()
        token ? token : null
    }
}
