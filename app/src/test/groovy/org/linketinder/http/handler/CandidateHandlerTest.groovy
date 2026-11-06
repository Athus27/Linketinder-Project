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

    def "deve excluir candidato por ID sem corpo na resposta"() {
        given:
        exchange.requestMethod >> "DELETE"
        exchange.requestURI >> URI.create("/api/candidatos/23")

        when:
        handler.handle(exchange)

        then:
        1 * candidatoController.buscarCandidatoPorId(23) >> [id: 23]
        1 * candidatoController.removerCandidato(23) >> true
        1 * exchange.sendResponseHeaders(204, -1)
        1 * exchange.close()
        responseBody.size() == 0
    }

    def "deve responder 404 sem excluir quando o candidato não existir"() {
        given:
        exchange.requestMethod >> "DELETE"
        exchange.requestURI >> URI.create("/api/candidatos/999")

        when:
        handler.handle(exchange)

        then:
        1 * candidatoController.buscarCandidatoPorId(999) >> null
        0 * candidatoController.removerCandidato(_)
        1 * exchange.sendResponseHeaders(404, _)
        new JsonSlurper().parseText(responseBody.toString("UTF-8")) == [erro: "Candidato não encontrado"]
    }

    def "deve responder 500 quando a persistência da exclusão falhar"() {
        given:
        exchange.requestMethod >> "DELETE"
        exchange.requestURI >> URI.create("/api/candidatos/23")

        when:
        handler.handle(exchange)

        then:
        1 * candidatoController.buscarCandidatoPorId(23) >> [id: 23]
        1 * candidatoController.removerCandidato(23) >> false
        1 * exchange.sendResponseHeaders(500, _)
        new JsonSlurper().parseText(responseBody.toString("UTF-8")) == [erro: "Erro ao excluir candidato"]
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
        exchange.requestBody >> jsonBody('{"nome":"Sandubinha","email":"sandubinha@email.com","senha":"123456"}')

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
        new JsonSlurper().parseText(responseBody.toString("UTF-8")) == [erro: "A senha deve ter pelo menos 6 caracteres"]
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
        exchange.requestBody >> jsonBody('{"nome":"Sandubinha","senha":"123456"}')

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
        exchange.requestBody >> jsonBody('{"nome":"Sandubinha","senha":"123456"}')

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
        exchange.requestBody >> jsonBody('{"nome":"Sandubinha","senha":"123456"}')

        when:
        handler.handle(exchange)

        then:
        1 * candidatoController.cadastrarCandidato(_) >> { throw new java.sql.SQLException("unique", "23505") }
        1 * exchange.sendResponseHeaders(409, _)
        new JsonSlurper().parseText(responseBody.toString("UTF-8")) == [erro: "Já existe um cadastro com esses dados"]
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
        responseHeaders.getFirst("Allow") == "GET, DELETE"
    }

    private static InputStream jsonBody(String json) {
        new ByteArrayInputStream(json.getBytes("UTF-8"))
    }
}
