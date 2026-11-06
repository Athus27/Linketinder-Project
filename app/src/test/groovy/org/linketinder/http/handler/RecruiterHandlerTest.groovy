package org.linketinder.http.handler

import com.sun.net.httpserver.Headers
import com.sun.net.httpserver.HttpExchange
import groovy.json.JsonSlurper
import org.linketinder.auth.AuthPrincipal
import org.linketinder.auth.SessionStore
import org.linketinder.controller.AuthController
import org.linketinder.controller.RecruiterController
import spock.lang.Specification

class RecruiterHandlerTest extends Specification {
    def recruiterController = Mock(RecruiterController)
    def authController = Mock(AuthController)
    def exchange = Mock(HttpExchange)
    def requestHeaders = new Headers()
    def responseHeaders = new Headers()
    def responseBody = new ByteArrayOutputStream()
    def store = new SessionStore()
    def handler

    def setup() {
        handler = new RecruiterHandler(recruiterController, authController, store)
        exchange.requestHeaders >> requestHeaders
        exchange.responseHeaders >> responseHeaders
        exchange.responseBody >> responseBody
    }

    def "recusa rota protegida sem token"() {
        given:
        exchange.requestURI >> URI.create("/api/recrutadores")
        exchange.requestMethod >> "GET"

        when:
        handler.handle(exchange)

        then:
        1 * exchange.sendResponseHeaders(401, _)
        new JsonSlurper().parseText(responseBody.toString("UTF-8")).codigo == "UNAUTHENTICATED"
        0 * authController._
        0 * recruiterController._
    }

    def "lista somente a empresa da sessão e não aceita recruiter como gestor"() {
        given:
        String token = store.issue(new AuthPrincipal(8, "Empresa", "company", 8))
        requestHeaders.set("Authorization", "Bearer ${token}")
        exchange.requestURI >> URI.create("/api/recrutadores")
        exchange.requestMethod >> "GET"
        authController.validar(_) >> new AuthPrincipal(8, "Empresa", "company", 8)
        recruiterController.listar(8) >> [[
                id: 12, nome: "Gestor", email: "gestor@example.test",
                area: "Tech", descricao: "Sintetico", idEmpresa: 8, ativo: true
        ]]

        when:
        handler.handle(exchange)

        then:
        1 * recruiterController.listar(8)
        1 * exchange.sendResponseHeaders(200, _)
        !responseBody.toString("UTF-8").contains("cpf")
        !responseBody.toString("UTF-8").contains("dataNascimento")
    }

    def "rejeita id negativo antes de acessar o DAO"() {
        given:
        String token = store.issue(new AuthPrincipal(8, "Empresa", "company", 8))
        requestHeaders.set("Authorization", "Bearer ${token}")
        exchange.requestURI >> URI.create("/api/recrutadores/-1")
        exchange.requestMethod >> "DELETE"
        authController.validar(_) >> new AuthPrincipal(8, "Empresa", "company", 8)

        when:
        handler.handle(exchange)

        then:
        1 * exchange.sendResponseHeaders(400, _)
        0 * recruiterController._
    }

    def "rejeita zero, overflow e idEmpresa não inteiro ou não positivo"() {
        given:
        String token = store.issue(new AuthPrincipal(8, "Empresa", "company", 8))
        requestHeaders.set("Authorization", "Bearer ${token}")
        exchange.requestURI >> URI.create(path)
        exchange.requestMethod >> method
        exchange.requestBody >> new ByteArrayInputStream(body.getBytes("UTF-8"))
        authController.validar(_) >> new AuthPrincipal(8, "Empresa", "company", 8)

        when:
        handler.handle(exchange)

        then:
        1 * exchange.sendResponseHeaders(status, _)
        0 * recruiterController._

        where:
        path                         | method | body                | status
        "/api/recrutadores/0"          | "DELETE" | ""                  | 400
        "/api/recrutadores/2147483648" | "DELETE" | ""                  | 400
        "/api/recrutadores/abc"        | "DELETE" | ""                  | 400
        "/api/recrutadores"            | "POST"   | '{"idEmpresa":8.5}' | 403
        "/api/recrutadores"            | "POST"   | '{"idEmpresa":0}'   | 403
    }

    def "converte falha do authDAO em erro JSON genérico"() {
        given:
        String token = store.issue(new AuthPrincipal(8, "Empresa", "company", 8))
        requestHeaders.set("Authorization", "Bearer ${token}")
        exchange.requestURI >> URI.create("/api/recrutadores")
        exchange.requestMethod >> "GET"
        authController.validar(_) >> { throw new IllegalStateException("database-secret") }

        when:
        handler.handle(exchange)

        then:
        1 * exchange.sendResponseHeaders(500, _)
        def response = new JsonSlurper().parseText(responseBody.toString("UTF-8"))
        response == [erro: "Erro interno do servidor", codigo: "INTERNAL_ERROR"]
        !response.toString().contains("database-secret")

    }

    def "converte falha ao listar em erro JSON genérico"() {
        given:
        String token = store.issue(new AuthPrincipal(8, "Empresa", "company", 8))
        requestHeaders.set("Authorization", "Bearer ${token}")
        exchange.requestURI >> URI.create("/api/recrutadores")
        exchange.requestMethod >> "GET"
        authController.validar(_) >> new AuthPrincipal(8, "Empresa", "company", 8)
        recruiterController.listar(8) >> { throw new IllegalStateException("database-secret") }

        when:
        handler.handle(exchange)

        then:
        1 * exchange.sendResponseHeaders(500, _)
        new JsonSlurper().parseText(responseBody.toString("UTF-8")) ==
                [erro: "Erro interno do servidor", codigo: "INTERNAL_ERROR"]
    }

    def "retorna 403 para papel recruiter"() {
        given:
        String token = store.issue(new AuthPrincipal(12, "Recruiter", "recruiter", 8))
        requestHeaders.set("Authorization", "Bearer ${token}")
        exchange.requestURI >> URI.create("/api/recrutadores")
        exchange.requestMethod >> "GET"
        authController.validar(_) >> new AuthPrincipal(12, "Recruiter", "recruiter", 8)

        when:
        handler.handle(exchange)

        then:
        1 * exchange.sendResponseHeaders(403, _)
        0 * recruiterController._
    }
}
