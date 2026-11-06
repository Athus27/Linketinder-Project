package org.linketinder.http.handler

import com.sun.net.httpserver.Headers
import groovy.json.JsonSlurper
import com.sun.net.httpserver.HttpExchange
import org.linketinder.controller.CandidatoController
import org.linketinder.controller.AuthController
import org.linketinder.auth.AuthPrincipal
import org.linketinder.auth.SessionStore
import java.util.Optional
import spock.lang.Specification

class CandidateHandlerTest extends Specification {
    private CandidatoController candidatoController
    private HttpExchange exchange
    private Headers responseHeaders
    private ByteArrayOutputStream responseBody
    private CandidateHandler handler
    private AuthController authController
    private SessionStore sessions

    def setup() {
        candidatoController = Mock()
        exchange = Mock()
        responseHeaders = new Headers()
        responseBody = new ByteArrayOutputStream()
        authController = Mock()
        sessions = Mock()
        handler = new CandidateHandler(candidatoController, authController, sessions)

        exchange.responseHeaders >> responseHeaders
        exchange.responseBody >> responseBody
        exchange.requestHeaders >> new Headers()
    }

    def "deve responder 405 quando o método não for GET, POST ou DELETE"() {
        given:
        exchange.requestMethod >> "PUT"
        exchange.requestURI >> URI.create("/api/candidatos")

        when:
        handler.handle(exchange)

        then:
        0 * candidatoController.cadastrarCandidato(_)
        1 * exchange.sendResponseHeaders(405, _)
        responseHeaders.getFirst("Allow") == "GET, POST"
    }

    def "deve informar GET e DELETE como métodos permitidos na rota com ID"() {
        given:
        exchange.requestMethod >> "PUT"
        exchange.requestURI >> URI.create("/api/candidatos/23")

        when:
        handler.handle(exchange)

        then:
        0 * candidatoController._
        1 * exchange.sendResponseHeaders(405, _)
        responseHeaders.getFirst("Allow") == "GET, DELETE"
    }

    def "deve exigir sessão para excluir candidato"() {
        given:
        exchange.requestMethod >> "DELETE"
        exchange.requestURI >> URI.create("/api/candidatos/23")

        when:
        handler.handle(exchange)

        then:
        0 * candidatoController._
        1 * exchange.sendResponseHeaders(401, _)
    }

    def "deve responder 404 sem excluir quando o candidato não existir"() {
        given:
        exchange.requestMethod >> "DELETE"
        exchange.requestURI >> URI.create("/api/candidatos/999")

        when:
        handler.handle(exchange)

        then:
        0 * candidatoController._
        1 * exchange.sendResponseHeaders(401, _)
        new JsonSlurper().parseText(responseBody.toString("UTF-8")).codigo == "UNAUTHENTICATED"
    }

    def "deve exigir sessão antes de tentar excluir"() {
        given:
        exchange.requestMethod >> "DELETE"
        exchange.requestURI >> URI.create("/api/candidatos/23")

        when:
        handler.handle(exchange)

        then:
        0 * candidatoController._
        1 * exchange.sendResponseHeaders(401, _)
    }

    def "deve responder 405 para DELETE sem ID"() {
        given:
        exchange.requestMethod >> "DELETE"
        exchange.requestURI >> URI.create("/api/candidatos")

        when:
        handler.handle(exchange)

        then:
        0 * candidatoController._
        1 * exchange.sendResponseHeaders(405, _)
        responseHeaders.getFirst("Allow") == "GET, POST"
    }

    def "deve listar candidatos somente com projeção anônima"() {
        given:
        exchange.requestMethod >> "GET"
        exchange.requestURI >> URI.create("/api/candidatos")
        def candidatos = [
                [id: 1, formacao: "Engenharia", competencias: ["Groovy"]],
                [id: 2, formacao: "Administração", competencias: []]
        ]

        when:
        handler.handle(exchange)

        then:
        1 * candidatoController.listarCandidatosPublicos(null) >> candidatos
        0 * candidatoController.cadastrarCandidato(_)
        1 * exchange.sendResponseHeaders(200, _)
        responseHeaders.getFirst("Content-Type") == "application/json; charset=UTF-8"
        new JsonSlurper().parseText(responseBody.toString("UTF-8")) == candidatos
    }

    def "deve responder 200 com lista vazia quando não houver candidatos"() {
        given:
        exchange.requestMethod >> "GET"
        exchange.requestURI >> URI.create("/api/candidatos")

        when:
        handler.handle(exchange)

        then:
        1 * candidatoController.listarCandidatosPublicos(null) >> []
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
        1 * candidatoController.listarCandidatosPublicos(null) >> { throw new RuntimeException("falha interna") }
        0 * candidatoController.cadastrarCandidato(_)
        1 * exchange.sendResponseHeaders(500, _)
        new JsonSlurper().parseText(responseBody.toString("UTF-8")) == [erro: "Erro interno do servidor", codigo: "INTERNAL_ERROR"]
    }

    def "deve cadastrar candidato recebido como JSON"() {
        given:
        exchange.requestMethod >> "POST"
        exchange.requestURI >> URI.create("/api/candidatos")
        exchange.requestBody >> jsonBody('{"nome":"Sandubinha","email":"sandubinha@email.com","senha":"123456","pais":"Brasil","cep":"01000-000"}')

        when:
        handler.handle(exchange)

        then:
        1 * candidatoController.cadastrarCandidato({ Map candidato ->
            candidato.nome == "Sandubinha" &&
                    candidato.email == "sandubinha@email.com"
        }) >> true
        1 * exchange.sendResponseHeaders(201, _)
    }

    def "deve rejeitar candidato com senha menor que seis caracteres"() {
        given:
        exchange.requestMethod >> "POST"
        exchange.requestURI >> URI.create("/api/candidatos")
        exchange.requestBody >> jsonBody('{"nome":"Sandubinha","senha":"123"}')

        when:
        handler.handle(exchange)

        then:
        0 * candidatoController.cadastrarCandidato(_)
        1 * exchange.sendResponseHeaders(400, _)
        new JsonSlurper().parseText(responseBody.toString("UTF-8")) == [erro: "A senha deve ter pelo menos 6 caracteres", codigo: "INVALID_INPUT"]
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
        exchange.requestBody >> jsonBody('{"nome":"Sandubinha","senha":"123456","pais":"Brasil","cep":"01000-000"}')

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
        exchange.requestBody >> jsonBody('{"nome":"Sandubinha","senha":"123456","pais":"Brasil","cep":"01000-000"}')

        when:
        handler.handle(exchange)

        then:
        1 * candidatoController.cadastrarCandidato(_) >> { throw new RuntimeException("falha interna") }
        1 * exchange.sendResponseHeaders(500, _)
    }

    def "deve responder 409 quando os dados únicos do candidato já existem"() {
        given:
        exchange.requestMethod >> "POST"
        exchange.requestURI >> URI.create("/api/candidatos")
        exchange.requestBody >> jsonBody('{"nome":"Sandubinha","senha":"123456","pais":"Brasil","cep":"01000-000"}')

        when:
        handler.handle(exchange)

        then:
        1 * candidatoController.cadastrarCandidato(_) >> { throw new java.sql.SQLException("unique", "23505") }
        1 * exchange.sendResponseHeaders(409, _)
        new JsonSlurper().parseText(responseBody.toString("UTF-8")) == [erro: "Cadastro já existente", codigo: "CONFLICT"]
    }

    def "deve responder 400 para ID inválido"() {
        given:
        exchange.requestMethod >> "DELETE"
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
        1 * candidatoController.buscarCandidatoPublico(1, null) >> [id: 1, competencias: ["Groovy"], formacao: "Engenharia"]
        1 * exchange.sendResponseHeaders(200, _)
        new JsonSlurper().parseText(responseBody.toString("UTF-8")) == [id: 1, competencias: ["Groovy"], formacao: "Engenharia"]
    }

    def "deve responder 404 para candidato inexistente"() {
        given:
        exchange.requestMethod >> "GET"
        exchange.requestURI >> URI.create("/api/candidatos/999")

        when:
        handler.handle(exchange)

        then:
        1 * candidatoController.buscarCandidatoPublico(999, null) >> null
        1 * exchange.sendResponseHeaders(404, _)
        new JsonSlurper().parseText(responseBody.toString("UTF-8")) == [erro: "Candidato não encontrado", codigo: "NOT_FOUND"]
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
        responseHeaders.getFirst("Allow") == "GET, DELETE"
    }

    private static InputStream jsonBody(String json) {
        new ByteArrayInputStream(json.getBytes("UTF-8"))
    }
}
