package org.linketinder.http

import com.sun.net.httpserver.Filter
import com.sun.net.httpserver.Headers
import com.sun.net.httpserver.HttpExchange
import spock.lang.Specification

class CorsFilterTest extends Specification {
    def exchange = Mock(HttpExchange)
    def chain = Mock(Filter.Chain)
    def requestHeaders = new Headers()
    def responseHeaders = new Headers()
    def filter = new CorsFilter("http://localhost:5173", ["GET", "POST"])

    def setup() {
        exchange.requestHeaders >> requestHeaders
        exchange.responseHeaders >> responseHeaders
    }

    def "autoriza a origem configurada inclusive quando o handler responde com erro"() {
        given:
        requestHeaders.set("Origin", "http://localhost:5173")
        exchange.requestMethod >> "GET"

        when:
        filter.doFilter(exchange, chain)

        then:
        1 * chain.doFilter(exchange) >> { exchange.sendResponseHeaders(500, -1) }
        1 * exchange.sendResponseHeaders(500, -1)
        responseHeaders.getFirst("Access-Control-Allow-Origin") == "http://localhost:5173"
        responseHeaders.getFirst("Vary") == "Origin"
    }

    def "permite consultas sem Origin sem adicionar autorização CORS"() {
        given:
        exchange.requestMethod >> "GET"

        when:
        filter.doFilter(exchange, chain)

        then:
        1 * chain.doFilter(exchange)
        responseHeaders.getFirst("Access-Control-Allow-Origin") == null
    }

    def "responde ao preflight de POST JSON sem executar o handler"() {
        given:
        requestHeaders.set("Origin", "http://localhost:5173")
        requestHeaders.set("Access-Control-Request-Method", "POST")
        requestHeaders.set("Access-Control-Request-Headers", "content-type")
        exchange.requestMethod >> "OPTIONS"

        when:
        filter.doFilter(exchange, chain)

        then:
        0 * chain.doFilter(_)
        1 * exchange.sendResponseHeaders(204, -1)
        1 * exchange.close()
        responseHeaders.getFirst("Access-Control-Allow-Origin") == "http://localhost:5173"
        responseHeaders.getFirst("Access-Control-Allow-Methods") == "GET, POST"
        responseHeaders.getFirst("Access-Control-Allow-Headers") == "Content-Type"
    }

    def "rejeita origem diferente sem executar o handler"() {
        given:
        requestHeaders.set("Origin", "http://site-nao-autorizado.test")
        exchange.requestMethod >> "GET"

        when:
        filter.doFilter(exchange, chain)

        then:
        0 * chain.doFilter(_)
        1 * exchange.sendResponseHeaders(403, -1)
        1 * exchange.close()
        responseHeaders.getFirst("Access-Control-Allow-Origin") == null
    }

    def "rejeita preflight com método ou cabeçalho não autorizado"() {
        given:
        requestHeaders.set("Origin", "http://localhost:5173")
        requestHeaders.set("Access-Control-Request-Method", method)
        requestHeaders.set("Access-Control-Request-Headers", headers)
        exchange.requestMethod >> "OPTIONS"

        when:
        filter.doFilter(exchange, chain)

        then:
        0 * chain.doFilter(_)
        1 * exchange.sendResponseHeaders(403, -1)
        1 * exchange.close()

        where:
        method   | headers
        "DELETE" | "content-type"
        "POST"   | "x-nao-autorizado"
    }
}
