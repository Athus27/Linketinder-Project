package org.linketinder.http.handler

import com.sun.net.httpserver.Headers
import com.sun.net.httpserver.HttpExchange
import groovy.json.JsonSlurper
import org.linketinder.controller.AuthController
import spock.lang.Specification

class AuthHandlerTest extends Specification {
    private AuthController authController
    private HttpExchange exchange
    private Headers responseHeaders
    private ByteArrayOutputStream responseBody
    private AuthHandler handler

    def setup() {
        authController = Mock()
        exchange = Mock()
        responseHeaders = new Headers()
        responseBody = new ByteArrayOutputStream()
        handler = new AuthHandler(authController)
        exchange.responseHeaders >> responseHeaders
        exchange.responseBody >> responseBody
    }

    def "deve retornar o perfil sem credenciais quando o login for válido"() {
        given:
        exchange.requestMethod >> "POST"
        exchange.requestURI >> URI.create("/api/auth/login")
        exchange.requestBody >> jsonBody('{"email":"ATHUS@example.com","senha":"secreta"}')

        when:
        handler.handle(exchange)

        then:
        1 * authController.login("ATHUS@example.com", "secreta") >> [id: 7, nome: "Athus", tipo: "candidate"]
        1 * exchange.sendResponseHeaders(200, _)
        responseHeaders.getFirst("Content-Type") == "application/json; charset=UTF-8"
        new JsonSlurper().parseText(responseBody.toString("UTF-8")) == [id: 7, nome: "Athus", tipo: "candidate"]
        !responseBody.toString("UTF-8").contains("senha")
    }

    def "deve responder 401 com mensagem genérica quando as credenciais forem inválidas"() {
        given:
        exchange.requestMethod >> "POST"
        exchange.requestURI >> URI.create("/api/auth/login")
        exchange.requestBody >> jsonBody('{"email":"nao-existe@example.com","senha":"errada"}')

        when:
        handler.handle(exchange)

        then:
        1 * authController.login(_, _) >> null
        1 * exchange.sendResponseHeaders(401, _)
        new JsonSlurper().parseText(responseBody.toString("UTF-8")) == [erro: "E-mail ou senha inválidos"]
    }

    def "deve responder 400 se faltar e-mail ou senha"() {
        given:
        exchange.requestMethod >> "POST"
        exchange.requestURI >> URI.create("/api/auth/login")
        exchange.requestBody >> jsonBody('{"email":"athus@example.com"}')

        when:
        handler.handle(exchange)

        then:
        0 * authController.login(_, _)
        1 * exchange.sendResponseHeaders(400, _)
    }

    def "deve responder 400 para JSON inválido"() {
        given:
        exchange.requestMethod >> "POST"
        exchange.requestURI >> URI.create("/api/auth/login")
        exchange.requestBody >> jsonBody('{"email":')

        when:
        handler.handle(exchange)

        then:
        0 * authController.login(_, _)
        1 * exchange.sendResponseHeaders(400, _)
    }

    def "deve responder 405 para método diferente de POST"() {
        given:
        exchange.requestMethod >> "GET"
        exchange.requestURI >> URI.create("/api/auth/login")

        when:
        handler.handle(exchange)

        then:
        0 * authController.login(_, _)
        1 * exchange.sendResponseHeaders(405, _)
        responseHeaders.getFirst("Allow") == "POST"
    }

    private static InputStream jsonBody(String json) {
        new ByteArrayInputStream(json.getBytes("UTF-8"))
    }
}
