package org.linketinder.http.handler

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.linketinder.auth.AuthPrincipal
import org.linketinder.auth.SessionStore
import org.linketinder.auth.AuthSupport
import org.linketinder.controller.AuthController
import org.linketinder.http.HttpLogger

import java.util.Objects

class AuthHandler implements HttpHandler {
    private final AuthController authController
    private final SessionStore sessionStore

    AuthHandler(AuthController authController) {
        this(authController, new SessionStore())
    }

    AuthHandler(AuthController authController, SessionStore sessionStore) {
        this.authController = authController
        this.sessionStore = Objects.requireNonNull(sessionStore, "sessionStore")
    }

    @Override
    void handle(HttpExchange exchange) throws IOException {
        long start = HttpLogger.iniciar()
        String path = exchange.requestURI.path
        if (path == "/api/auth/logout") {
            logout(exchange, start)
            return
        }
        if (path != "/api/auth/login") {
            respond(exchange, 404, error("Rota não encontrada", "ROUTE_NOT_FOUND"), start)
            return
        }
        if (exchange.requestMethod != "POST") {
            exchange.responseHeaders.set("Allow", "POST")
            respond(exchange, 405, error("Método não permitido", "METHOD_NOT_ALLOWED"), start)
            return
        }

        Map data
        try {
            Object parsed = new JsonSlurper().parse(exchange.requestBody)
            if (!(parsed instanceof Map)) {
                throw new IllegalArgumentException()
            }
            data = parsed as Map
        } catch (Exception ignored) {
            respond(exchange, 400, error("JSON inválido", "INVALID_JSON"), start)
            return
        }
        if (!(data.email instanceof String) || !data.email.trim() ||
                !(data.senha instanceof String) || !data.senha) {
            respond(exchange, 400, error("Informe e-mail e senha", "INVALID_INPUT"), start)
            return
        }
        try {
            Map user = authController.login(data.email as String, data.senha as String)
            if (user == null) {
                respond(exchange, 401,
                        error("E-mail ou senha inválidos", "INVALID_CREDENTIALS"), start)
                return
            }
            AuthPrincipal principal = new AuthPrincipal(
                    user.id as Integer, user.nome as String, user.tipo as String,
                    user.idEmpresa as Integer
            )
            String token = sessionStore.issue(principal)
            respond(exchange, 200, [
                    token: token,
                    id: principal.id,
                    nome: principal.nome,
                    tipo: principal.tipo,
                    idEmpresa: principal.idEmpresa
            ].findAll { key, value -> value != null }, start)
        } catch (Exception ignored) {
            respond(exchange, 500, error("Erro interno do servidor", "INTERNAL_ERROR"), start)
        }
    }

    private void logout(HttpExchange exchange, long start) {
        if (exchange.requestMethod != "POST") {
            exchange.responseHeaders.set("Allow", "POST")
            respond(exchange, 405, error("Método não permitido", "METHOD_NOT_ALLOWED"), start)
            return
        }
        sessionStore.revoke(AuthSupport.bearerToken(exchange))
        exchange.sendResponseHeaders(204, -1)
        exchange.close()
        HttpLogger.registrar(exchange.requestMethod, exchange.requestURI.path, 204, start)
    }

    private static Map error(String message, String code) {
        [erro: message, codigo: code]
    }

    private static void respond(HttpExchange exchange, int status, Object body, long start) {
        byte[] bytes = JsonOutput.toJson(body).getBytes("UTF-8")
        exchange.responseHeaders.set("Content-Type", "application/json; charset=UTF-8")
        exchange.sendResponseHeaders(status, bytes.length)
        exchange.responseBody.withStream { it.write(bytes) }
        HttpLogger.registrar(exchange.requestMethod, exchange.requestURI.path, status, start)
    }
}
