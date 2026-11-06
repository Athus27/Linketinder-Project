package org.linketinder.http.handler

import com.sun.net.httpserver.Headers
import com.sun.net.httpserver.HttpExchange
import org.linketinder.controller.CompanyController
import groovy.json.JsonSlurper
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

    def "deve responder 405 quando o método não for GET nem POST"() {
        given:
        exchange.requestMethod >> "DELETE"
        exchange.requestURI >> URI.create("/api/empresas")

        when:
        handler.handle(exchange)

        then:
        0 * companyController.cadastrarEmpresa(_)
        1 * exchange.sendResponseHeaders(405, _)
        responseHeaders.getFirst("Allow") == "GET, POST"
    }

    def "deve cadastrar empresa recebida como JSON"() {
        given:
        exchange.requestMethod >> "POST"
        exchange.requestURI >> URI.create("/api/empresas")
        exchange.requestBody >> jsonBody('{"nome":"Tapetes Bolinho","cnpj":"12.345.678/0001-00","senha":"123456"}')

        when:
        handler.handle(exchange)

        then:
        1 * companyController.cadastrarEmpresa({ Map empresa ->
            empresa.nome == "Tapetes Bolinho" &&
                    empresa.cnpj == "12.345.678/0001-00"
        }) >> true
        1 * exchange.sendResponseHeaders(201, _)
    }

    def "deve rejeitar empresa com senha menor que seis caracteres"() {
        given:
        exchange.requestMethod >> "POST"
        exchange.requestURI >> URI.create("/api/empresas")
        exchange.requestBody >> jsonBody('{"nome":"Tapetes Bolinho","senha":"123"}')

        when:
        handler.handle(exchange)

        then:
        0 * companyController.cadastrarEmpresa(_)
        1 * exchange.sendResponseHeaders(400, _)
        new JsonSlurper().parseText(responseBody.toString("UTF-8")) == [erro: "A senha deve ter pelo menos 6 caracteres"]
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
        exchange.requestBody >> jsonBody('{"nome":"Tapetes Bolinho","senha":"123456"}')

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
        exchange.requestBody >> jsonBody('{"nome":"Tapetes Bolinho","senha":"123456"}')

        when:
        handler.handle(exchange)

        then:
        1 * companyController.cadastrarEmpresa(_) >> { throw new RuntimeException("falha interna") }
        1 * exchange.sendResponseHeaders(500, _)
    }

    def "deve responder 409 quando os dados únicos da empresa já existem"() {
        given:
        exchange.requestMethod >> "POST"
        exchange.requestURI >> URI.create("/api/empresas")
        exchange.requestBody >> jsonBody('{"nome":"Tapetes Bolinho","senha":"123456"}')

        when:
        handler.handle(exchange)

        then:
        1 * companyController.cadastrarEmpresa(_) >> { throw new java.sql.SQLException("unique", "23505") }
        1 * exchange.sendResponseHeaders(409, _)
        new JsonSlurper().parseText(responseBody.toString("UTF-8")) == [erro: "Já existe um cadastro com esses dados"]
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

    def "deve listar empresas em JSON"() {
        given:
        exchange.requestMethod >> "GET"
        exchange.requestURI >> URI.create("/api/empresas")
        def registros = [[id: 7, nome: "Empresa teste", cnpj: "123", descricao: "Descrição", email: "teste@example.com", pais: "Brasil", cep: "12345"]]

        when:
        handler.handle(exchange)

        then:
        1 * companyController.listarEmpresas() >> registros
        1 * exchange.sendResponseHeaders(200, _)
        responseHeaders.getFirst("Content-Type") == "application/json; charset=UTF-8"
        new JsonSlurper().parseText(responseBody.toString("UTF-8")) == registros
    }

    def "deve retornar array vazio quando não houver empresas"() {
        given:
        exchange.requestMethod >> "GET"
        exchange.requestURI >> URI.create("/api/empresas")

        when:
        handler.handle(exchange)

        then:
        1 * companyController.listarEmpresas() >> []
        1 * exchange.sendResponseHeaders(200, _)
        new JsonSlurper().parseText(responseBody.toString("UTF-8")) == []
    }

    def "deve responder 500 quando a listagem de empresas falhar"() {
        given:
        exchange.requestMethod >> "GET"
        exchange.requestURI >> URI.create("/api/empresas")

        when:
        handler.handle(exchange)

        then:
        1 * companyController.listarEmpresas() >> { throw new RuntimeException("falha") }
        1 * exchange.sendResponseHeaders(500, _)
        new JsonSlurper().parseText(responseBody.toString("UTF-8")) == [erro: "Erro ao listar empresas"]
    }

    private static InputStream jsonBody(String json) {
        new ByteArrayInputStream(json.getBytes("UTF-8"))
    }
}
