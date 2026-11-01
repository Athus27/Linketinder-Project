package org.linketinder.http.handler

import com.sun.net.httpserver.Headers
import com.sun.net.httpserver.HttpExchange
import org.linketinder.controller.VagaController
import spock.lang.Specification

class VagaHandlerTest extends Specification {
    private VagaController vagaController
    private HttpExchange exchange
    private Headers responseHeaders
    private ByteArrayOutputStream responseBody
    private VagaHandler handler

    def setup() {
        vagaController = Mock()
        exchange = Mock()
        responseHeaders = new Headers()
        responseBody = new ByteArrayOutputStream()
        handler = new VagaHandler(vagaController)

        exchange.responseHeaders >> responseHeaders
        exchange.responseBody >> responseBody
    }

    def "deve responder 405 quando o método não for POST"() {
        given:
        exchange.requestMethod >> "GET"
        exchange.requestURI >> URI.create("/api/vagas")

        when:
        handler.handle(exchange)

        then:
        0 * vagaController.cadastrarVaga(_)
        1 * exchange.sendResponseHeaders(405, _)
        responseHeaders.getFirst("Allow") == "POST"
    }

    def "deve cadastrar vaga recebida como JSON"() {
        given:
        exchange.requestMethod >> "POST"
        exchange.requestURI >> URI.create("/api/vagas")
        exchange.requestBody >> jsonBody('{"idEmpresa":1,"titulo":"Desenvolvedor Groovy"}')

        when:
        handler.handle(exchange)

        then:
        1 * vagaController.cadastrarVaga({ Map vaga ->
            vaga.idEmpresa == 1 && vaga.titulo == "Desenvolvedor Groovy"
        }) >> true
        1 * exchange.sendResponseHeaders(201, _)
    }

    def "deve responder 400 para JSON inválido"() {
        given:
        exchange.requestMethod >> "POST"
        exchange.requestURI >> URI.create("/api/vagas")
        exchange.requestBody >> jsonBody('{"titulo":')

        when:
        handler.handle(exchange)

        then:
        0 * vagaController.cadastrarVaga(_)
        1 * exchange.sendResponseHeaders(400, _)
    }

    def "deve responder 500 quando a vaga não for persistida"() {
        given:
        exchange.requestMethod >> "POST"
        exchange.requestURI >> URI.create("/api/vagas")
        exchange.requestBody >> jsonBody('{"titulo":"Desenvolvedor Groovy"}')

        when:
        handler.handle(exchange)

        then:
        1 * vagaController.cadastrarVaga(_) >> false
        1 * exchange.sendResponseHeaders(500, _)
    }

    def "deve responder 500 quando o controller lançar uma exceção"() {
        given:
        exchange.requestMethod >> "POST"
        exchange.requestURI >> URI.create("/api/vagas")
        exchange.requestBody >> jsonBody('{"titulo":"Desenvolvedor Groovy"}')

        when:
        handler.handle(exchange)

        then:
        1 * vagaController.cadastrarVaga(_) >> { throw new RuntimeException("falha interna") }
        1 * exchange.sendResponseHeaders(500, _)
    }

    def "deve responder 404 para caminho diferente do endpoint"() {
        given:
        exchange.requestMethod >> "POST"
        exchange.requestURI >> URI.create("/api/vagas/qualquer-coisa")

        when:
        handler.handle(exchange)

        then:
        0 * vagaController.cadastrarVaga(_)
        1 * exchange.sendResponseHeaders(404, _)
    }

    private static InputStream jsonBody(String json) {
        new ByteArrayInputStream(json.getBytes("UTF-8"))
    }
}
