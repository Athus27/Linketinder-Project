package org.linketinder.http.handler

import com.sun.net.httpserver.Headers
import com.sun.net.httpserver.HttpExchange
import org.linketinder.controller.CompanyController
import spock.lang.Specification

class CompanyHandlerTest extends Specification {
    private CompanyController companyController
    private HttpExchange exchange
    private Headers responseHeaders
    private ByteArrayOutputStream responseBody
    private CompanyHandler handler

    def setup() {
        companyController = Mock()
        exchange = Mock()
        responseHeaders = new Headers()
        responseBody = new ByteArrayOutputStream()
        handler = new CompanyHandler(companyController)

        exchange.responseHeaders >> responseHeaders
        exchange.responseBody >> responseBody
    }

    def "deve responder 405 quando o método não for POST"() {
        given:
        exchange.requestMethod >> "GET"
        exchange.requestURI >> URI.create("/api/empresas")

        when:
        handler.handle(exchange)

        then:
        0 * companyController.cadastrarEmpresa(_)
        1 * exchange.sendResponseHeaders(405, _)
        responseHeaders.getFirst("Allow") == "POST"
    }

    def "deve cadastrar empresa recebida como JSON"() {
        given:
        exchange.requestMethod >> "POST"
        exchange.requestURI >> URI.create("/api/empresas")
        exchange.requestBody >> jsonBody('{"nome":"Tapetes Bolinho","cnpj":"12.345.678/0001-00"}')

        when:
        handler.handle(exchange)

        then:
        1 * companyController.cadastrarEmpresa({ Map empresa ->
            empresa.nome == "Tapetes Bolinho" &&
                    empresa.cnpj == "12.345.678/0001-00"
        }) >> true
        1 * exchange.sendResponseHeaders(201, _)
    }

    def "deve responder 400 para JSON inválido"() {
        given:
        exchange.requestMethod >> "POST"
        exchange.requestURI >> URI.create("/api/empresas")
        exchange.requestBody >> jsonBody('{"nome":')

        when:
        handler.handle(exchange)

        then:
        0 * companyController.cadastrarEmpresa(_)
        1 * exchange.sendResponseHeaders(400, _)
    }

    def "deve responder 500 quando o cadastro não for persistido"() {
        given:
        exchange.requestMethod >> "POST"
        exchange.requestURI >> URI.create("/api/empresas")
        exchange.requestBody >> jsonBody('{"nome":"Tapetes Bolinho"}')

        when:
        handler.handle(exchange)

        then:
        1 * companyController.cadastrarEmpresa(_) >> false
        1 * exchange.sendResponseHeaders(500, _)
    }

    def "deve responder 500 quando o controller lançar uma exceção"() {
        given:
        exchange.requestMethod >> "POST"
        exchange.requestURI >> URI.create("/api/empresas")
        exchange.requestBody >> jsonBody('{"nome":"Tapetes Bolinho"}')

        when:
        handler.handle(exchange)

        then:
        1 * companyController.cadastrarEmpresa(_) >> { throw new RuntimeException("falha interna") }
        1 * exchange.sendResponseHeaders(500, _)
    }

    def "deve responder 404 para caminho diferente do endpoint"() {
        given:
        exchange.requestMethod >> "POST"
        exchange.requestURI >> URI.create("/api/empresas/qualquer-coisa")

        when:
        handler.handle(exchange)

        then:
        0 * companyController.cadastrarEmpresa(_)
        1 * exchange.sendResponseHeaders(404, _)
    }

    private static InputStream jsonBody(String json) {
        new ByteArrayInputStream(json.getBytes("UTF-8"))
    }
}
