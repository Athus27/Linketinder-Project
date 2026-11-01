package org.linketinder.http.handler

import com.sun.net.httpserver.Headers
import com.sun.net.httpserver.HttpExchange
import org.linketinder.controller.CandidatoController
import spock.lang.Specification

class CandidateHandlerTest extends Specification {
    private CandidatoController candidatoController
    private HttpExchange exchange
    private Headers responseHeaders
    private ByteArrayOutputStream responseBody
    private CandidateHandler handler

    def setup() {
        candidatoController = Mock()
        exchange = Mock()
        responseHeaders = new Headers()
        responseBody = new ByteArrayOutputStream()
        handler = new CandidateHandler(candidatoController)

        exchange.responseHeaders >> responseHeaders
        exchange.responseBody >> responseBody
    }

    def "deve responder 405 quando o método não for POST"() {
        given:
        exchange.requestMethod >> "GET"
        exchange.requestURI >> URI.create("/api/candidatos")

        when:
        handler.handle(exchange)

        then:
        0 * candidatoController.cadastrarCandidato(_)
        1 * exchange.sendResponseHeaders(405, _)
        responseHeaders.getFirst("Allow") == "POST"
    }

    def "deve cadastrar candidato recebido como JSON"() {
        given:
        exchange.requestMethod >> "POST"
        exchange.requestURI >> URI.create("/api/candidatos")
        exchange.requestBody >> jsonBody('{"nome":"Sandubinha","email":"sandubinha@email.com"}')

        when:
        handler.handle(exchange)

        then:
        1 * candidatoController.cadastrarCandidato({ Map candidato ->
            candidato.nome == "Sandubinha" &&
                    candidato.email == "sandubinha@email.com"
        }) >> true
        1 * exchange.sendResponseHeaders(201, _)
    }

    def "deve responder 400 para JSON inválido"() {
        given:
        exchange.requestMethod >> "POST"
        exchange.requestURI >> URI.create("/api/candidatos")
        exchange.requestBody >> jsonBody('{"nome":')

        when:
        handler.handle(exchange)

        then:
        0 * candidatoController.cadastrarCandidato(_)
        1 * exchange.sendResponseHeaders(400, _)
    }

    def "deve responder 500 quando o cadastro não for persistido"() {
        given:
        exchange.requestMethod >> "POST"
        exchange.requestURI >> URI.create("/api/candidatos")
        exchange.requestBody >> jsonBody('{"nome":"Sandubinha"}')

        when:
        handler.handle(exchange)

        then:
        1 * candidatoController.cadastrarCandidato(_) >> false
        1 * exchange.sendResponseHeaders(500, _)
    }

    def "deve responder 500 quando o controller lançar uma exceção"() {
        given:
        exchange.requestMethod >> "POST"
        exchange.requestURI >> URI.create("/api/candidatos")
        exchange.requestBody >> jsonBody('{"nome":"Sandubinha"}')

        when:
        handler.handle(exchange)

        then:
        1 * candidatoController.cadastrarCandidato(_) >> { throw new RuntimeException("falha interna") }
        1 * exchange.sendResponseHeaders(500, _)
    }

    def "deve responder 404 para caminho diferente do endpoint"() {
        given:
        exchange.requestMethod >> "POST"
        exchange.requestURI >> URI.create("/api/candidatos/qualquer-coisa")

        when:
        handler.handle(exchange)

        then:
        0 * candidatoController.cadastrarCandidato(_)
        1 * exchange.sendResponseHeaders(404, _)
    }

    private static InputStream jsonBody(String json) {
        new ByteArrayInputStream(json.getBytes("UTF-8"))
    }
}
