package org.linketinder.http.handler

import com.sun.net.httpserver.Headers
import com.sun.net.httpserver.HttpExchange
import groovy.json.JsonSlurper
import org.linketinder.controller.AuthController
import org.linketinder.auth.SessionStore
import spock.lang.Specification

class AuthHandlerTest extends Specification {
    private AuthController authController
    private HttpExchange exchange
    private Headers responseHeaders
    private Headers requestHeaders = new Headers()
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

    def "deve retornar sessão e perfil sem credenciais quando o login for válido"() {
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
        def response = new JsonSlurper().parseText(responseBody.toString("UTF-8"))
        response.id == 7
        response.nome == "Athus"
        response.tipo == "candidate"
        response.token
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
        new JsonSlurper().parseText(responseBody.toString("UTF-8")) ==
                [erro: "E-mail ou senha inválidos", codigo: "INVALID_CREDENTIALS"]
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

    def "login de sessão retorna token e logout desconhecido é idempotente"() {
        given:
        def sessionStore = new SessionStore()
        def sessionHandler = new AuthHandler(authController, sessionStore)
        exchange.requestMethod >> "POST"
        exchange.requestURI >> URI.create("/api/auth/login")
        exchange.requestBody >> jsonBody('{"email":"recrutador@example.test","senha":"Senha123"}')
        authController.login(_, _) >> [id: 7, nome: "Recrutador", tipo: "recruiter", idEmpresa: 3]

        when:
        sessionHandler.handle(exchange)

        then:
        1 * exchange.sendResponseHeaders(200, _)
        def login = new JsonSlurper().parseText(responseBody.toString("UTF-8"))
        login.token
        login.id == 7
        login.idEmpresa == 3
        !login.containsKey("senha")
        !login.containsKey("cpf")
        !login.containsKey("dataNascimento")

        when:
        responseBody.reset()
        exchange.requestURI >> URI.create("/api/auth/logout")
        requestHeaders.set("Authorization", "Bearer desconhecido")
        exchange.requestHeaders >> requestHeaders
        sessionHandler.handle(exchange)

        then:
        1 * exchange.sendResponseHeaders(204, -1)
    }

    def "não aceita SessionStore nulo"() {
        when:
        new AuthHandler(authController, null)

        then:
        thrown(NullPointerException)
    }

    private static InputStream jsonBody(String json) {
        new ByteArrayInputStream(json.getBytes("UTF-8"))
    }
}
