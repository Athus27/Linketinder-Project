package org.linketinder.http.handler

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.linketinder.controller.AuthController
import org.linketinder.http.HttpLogger

class AuthHandler implements HttpHandler {
    private static final String ENDPOINT = "/api/auth/login"
    private final AuthController authController

    AuthHandler(AuthController authController) {
        this.authController = authController
    }

    @Override
    void handle(HttpExchange exchange) throws IOException {
        long inicio = HttpLogger.iniciar()
        String path = exchange.requestURI.path

        if (path != ENDPOINT) {
            responder(exchange, 404, [erro: "Rota não encontrada"], inicio)
            return
        }

        if (exchange.requestMethod != "POST") {
            exchange.responseHeaders.set("Allow", "POST")
            responder(exchange, 405, [erro: "Método não permitido"], inicio)
            return
        }

        Map dados
        try {
            dados = new JsonSlurper().parse(exchange.requestBody) as Map
        } catch (Exception ignored) {
            responder(exchange, 400, [erro: "JSON inválido"], inicio)
            return
        }

        Object email = dados.email
        Object senha = dados.senha
        if (!(email instanceof String) || !email.trim() || !(senha instanceof String) || !senha) {
            responder(exchange, 400, [erro: "Informe e-mail e senha"], inicio)
            return
        }

        try {
            Map usuario = authController.login(email as String, senha as String)
            if (usuario == null) {
                responder(exchange, 401, [erro: "E-mail ou senha inválidos"], inicio)
                return
            }

            responder(exchange, 200, usuario, inicio)
        } catch (Exception ignored) {
            responder(exchange, 500, [erro: "Erro interno do servidor"], inicio)
        }
    }

    private static void responder(
            HttpExchange exchange,
            int status,
            Map response,
            long inicio
    ) {
        byte[] responseBytes = JsonOutput.toJson(response).getBytes("UTF-8")
        exchange.responseHeaders.set("Content-Type", "application/json; charset=UTF-8")
        exchange.sendResponseHeaders(status, responseBytes.length)
        exchange.responseBody.withStream { out -> out.write(responseBytes) }
        HttpLogger.registrar(exchange.requestMethod, exchange.requestURI.path, status, inicio)
    }
}
