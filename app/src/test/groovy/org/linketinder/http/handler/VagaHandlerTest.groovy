package org.linketinder.http.handler

import com.sun.net.httpserver.Headers
import com.sun.net.httpserver.HttpExchange
import org.linketinder.controller.VagaController
import org.linketinder.controller.AuthController
import org.linketinder.auth.SessionStore
import groovy.json.JsonSlurper
import spock.lang.Specification

class VagaHandlerTest extends Specification {
    private VagaController vagaController
    private HttpExchange exchange
    private Headers responseHeaders
    private ByteArrayOutputStream responseBody
    private VagaHandler handler
    private AuthController authController
    private SessionStore sessions

    def setup() {
        vagaController = Mock()
        exchange = Mock()
        responseHeaders = new Headers()
        responseBody = new ByteArrayOutputStream()
        authController = Mock()
        sessions = Mock()
        handler = new VagaHandler(vagaController, authController, sessions)

        exchange.responseHeaders >> responseHeaders
        exchange.responseBody >> responseBody
        exchange.requestHeaders >> new Headers()
    }

    def "deve responder 405 quando o método não for GET nem POST"() {
        given:
        exchange.requestMethod >> "DELETE"
        exchange.requestURI >> URI.create("/api/vagas")

        when:
        handler.handle(exchange)

        then:
        0 * vagaController.cadastrarVaga(_)
        1 * exchange.sendResponseHeaders(405, _)
        responseHeaders.getFirst("Allow") == "GET, POST"
    }

    def "deve cadastrar vaga recebida como JSON"() {
        given:
        exchange.requestMethod >> "POST"
        exchange.requestURI >> URI.create("/api/vagas")
        exchange.requestBody >> jsonBody('{"idEmpresa":1,"titulo":"Desenvolvedor Groovy","descricao":"Backend","localVaga":"Remoto","competencias":["Groovy"]}')

        when:
        handler.handle(exchange)

        then:
        1 * exchange.sendResponseHeaders(401, _)
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
        1 * exchange.sendResponseHeaders(401, _)
    }

    def "deve responder 500 quando a vaga não for persistida"() {
        given:
        exchange.requestMethod >> "POST"
        exchange.requestURI >> URI.create("/api/vagas")
        exchange.requestBody >> jsonBody('{"titulo":"Desenvolvedor Groovy","descricao":"Backend","localVaga":"Remoto","competencias":["Groovy"]}')

        when:
        handler.handle(exchange)

        then:
        0 * vagaController._
        1 * exchange.sendResponseHeaders(401, _)
    }

    def "deve responder 500 quando o controller lançar uma exceção"() {
        given:
        exchange.requestMethod >> "POST"
        exchange.requestURI >> URI.create("/api/vagas")
        exchange.requestBody >> jsonBody('{"titulo":"Desenvolvedor Groovy","descricao":"Backend","localVaga":"Remoto","competencias":["Groovy"]}')

        when:
        handler.handle(exchange)

        then:
        0 * vagaController._
        1 * exchange.sendResponseHeaders(401, _)
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

    def "deve listar vagas em JSON"() {
        given:
        exchange.requestMethod >> "GET"
        exchange.requestURI >> URI.create("/api/vagas")
        def registros = [[id: 9, idEmpresa: 7, empresa: "Empresa teste", titulo: "Dev", descricao: "Descrição", localVaga: "Remoto", competencias: ["Groovy"]]]

        when:
        handler.handle(exchange)

        then:
        1 * vagaController.listarVagasPublicas() >> registros
        1 * exchange.sendResponseHeaders(200, _)
        responseHeaders.getFirst("Content-Type") == "application/json; charset=UTF-8"
        new JsonSlurper().parseText(responseBody.toString("UTF-8")) == registros
    }

    def "deve retornar array vazio quando não houver vagas"() {
        given:
        exchange.requestMethod >> "GET"
        exchange.requestURI >> URI.create("/api/vagas")

        when:
        handler.handle(exchange)

        then:
        1 * vagaController.listarVagasPublicas() >> []
        1 * exchange.sendResponseHeaders(200, _)
        new JsonSlurper().parseText(responseBody.toString("UTF-8")) == []
    }

    def "deve responder 500 quando a listagem de vagas falhar"() {
        given:
        exchange.requestMethod >> "GET"
        exchange.requestURI >> URI.create("/api/vagas")

        when:
        handler.handle(exchange)

        then:
        1 * vagaController.listarVagasPublicas() >> { throw new RuntimeException("falha") }
        1 * exchange.sendResponseHeaders(500, _)
        new JsonSlurper().parseText(responseBody.toString("UTF-8")) == [erro: "Erro interno do servidor", codigo: "INTERNAL_ERROR"]
    }

    private static InputStream jsonBody(String json) {
        new ByteArrayInputStream(json.getBytes("UTF-8"))
    }
}
