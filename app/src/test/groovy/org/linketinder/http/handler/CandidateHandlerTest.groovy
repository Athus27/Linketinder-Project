package org.linketinder.http.handler

import com.sun.net.httpserver.Headers
import groovy.json.JsonSlurper
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

    def "deve responder 405 quando o método não for GET nem POST"() {
        given:
        exchange.requestMethod >> "DELETE"
        exchange.requestURI >> URI.create("/api/candidatos")

        when:
        handler.handle(exchange)

        then:
        0 * candidatoController.cadastrarCandidato(_)
        1 * exchange.sendResponseHeaders(405, _)
        responseHeaders.getFirst("Allow") == "GET, POST"
    }

    def "deve listar candidatos como JSON com data de nascimento no formato ISO"() {
        given:
        exchange.requestMethod >> "GET"
        exchange.requestURI >> URI.create("/api/candidatos")
        def candidatos = [
                [id: 1, nome: "João", dataNascimento: java.sql.Date.valueOf("1990-01-15"), competencias: ["Groovy"]],
                [id: 2, nome: "Ana", dataNascimento: java.sql.Date.valueOf("2000-12-31"), competencias: []]
        ]

        when:
        handler.handle(exchange)

        then:
        1 * candidatoController.listarCandidatos() >> candidatos
        0 * candidatoController.cadastrarCandidato(_)
        1 * exchange.sendResponseHeaders(200, _)
        responseHeaders.getFirst("Content-Type") == "application/json; charset=UTF-8"
        new JsonSlurper().parseText(responseBody.toString("UTF-8")) == [
                [id: 1, nome: "João", dataNascimento: "1990-01-15", competencias: ["Groovy"]],
                [id: 2, nome: "Ana", dataNascimento: "2000-12-31", competencias: []]
        ]
    }

    def "deve responder 200 com lista vazia quando não houver candidatos"() {
        given:
        exchange.requestMethod >> "GET"
        exchange.requestURI >> URI.create("/api/candidatos")

        when:
        handler.handle(exchange)

        then:
        1 * candidatoController.listarCandidatos() >> []
        0 * candidatoController.cadastrarCandidato(_)
        1 * exchange.sendResponseHeaders(200, _)
        new JsonSlurper().parseText(responseBody.toString("UTF-8")) == []
    }

    def "deve responder 500 quando a listagem lançar exceção"() {
        given:
        exchange.requestMethod >> "GET"
        exchange.requestURI >> URI.create("/api/candidatos")

        when:
        handler.handle(exchange)

        then:
        1 * candidatoController.listarCandidatos() >> { throw new RuntimeException("falha interna") }
        0 * candidatoController.cadastrarCandidato(_)
        1 * exchange.sendResponseHeaders(500, _)
        new JsonSlurper().parseText(responseBody.toString("UTF-8")) == [erro: "Erro ao listar candidatos"]
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

    def "deve responder 400 para ID inválido"() {
        given:
        exchange.requestMethod >> "POST"
        exchange.requestURI >> URI.create("/api/candidatos/qualquer-coisa")

        when:
        handler.handle(exchange)

        then:
        0 * candidatoController.cadastrarCandidato(_)
        1 * exchange.sendResponseHeaders(400, _)
    }

    def "deve buscar candidato por ID com data ISO"() {
        given:
        exchange.requestMethod >> "GET"
        exchange.requestURI >> URI.create("/api/candidatos/1")

        when:
        handler.handle(exchange)

        then:
        1 * candidatoController.buscarCandidatoPorId(1) >> [id: 1, nome: "Fulano", dataNascimento: java.sql.Date.valueOf("2002-01-15")]
        0 * candidatoController.listarCandidatos()
        1 * exchange.sendResponseHeaders(200, _)
        new JsonSlurper().parseText(responseBody.toString("UTF-8")) == [id: 1, nome: "Fulano", dataNascimento: "2002-01-15"]
    }

    def "deve responder 404 para candidato inexistente"() {
        given:
        exchange.requestMethod >> "GET"
        exchange.requestURI >> URI.create("/api/candidatos/999")

        when:
        handler.handle(exchange)

        then:
        1 * candidatoController.buscarCandidatoPorId(999) >> null
        1 * exchange.sendResponseHeaders(404, _)
        new JsonSlurper().parseText(responseBody.toString("UTF-8")) == [erro: "Candidato não encontrado"]
    }

    def "deve rejeitar POST com ID sem cadastrar"() {
        given:
        exchange.requestMethod >> "POST"
        exchange.requestURI >> URI.create("/api/candidatos/1")

        when:
        handler.handle(exchange)

        then:
        0 * candidatoController._
        1 * exchange.sendResponseHeaders(405, _)
    }

    private static InputStream jsonBody(String json) {
        new ByteArrayInputStream(json.getBytes("UTF-8"))
    }
}
