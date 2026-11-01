package org.linketinder.http.handler

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import groovy.json.JsonSlurper
import org.linketinder.controller.VagaController

class VagaHandler implements HttpHandler {
    private static final String ENDPOINT = "/api/vagas"
    private final VagaController vagaController

    VagaHandler(VagaController vagaController) {
        this.vagaController = vagaController
    }

    @Override
    void handle(HttpExchange exchange) throws IOException {
        if (exchange.requestURI.path != ENDPOINT) {
            responder(exchange, 404, '{"erro":"Rota não encontrada"}')
            return
        }

        if (exchange.requestMethod != "POST") {
            exchange.responseHeaders.set("Allow", "POST")
            responder(exchange, 405, '{"erro":"Método não permitido"}')
            return
        }

        Map vaga
        try {
            vaga = new JsonSlurper().parse(exchange.requestBody) as Map
        } catch (Exception ignored) {
            responder(exchange, 400, '{"erro":"JSON inválido"}')
            return
        }

        String responseText
        int status

        try {
            boolean cadastrada = vagaController.cadastrarVaga(vaga)
            status = cadastrada ? 201 : 500
            responseText = cadastrada
                    ? '{"mensagem":"Vaga cadastrada com sucesso"}'
                    : '{"erro":"Erro ao persistir a vaga"}'
        } catch (Exception ignored) {
            status = 500
            responseText = '{"erro":"Erro interno do servidor"}'
        }

        responder(exchange, status, responseText)
    }

    private static void responder(
            HttpExchange exchange,
            int status,
            String responseText
    ) {
        byte[] responseBytes = responseText.getBytes("UTF-8")

        exchange.responseHeaders.set(
                "Content-Type",
                "application/json; charset=UTF-8"
        )

        exchange.sendResponseHeaders(status, responseBytes.length)

        exchange.responseBody.withStream { out ->
            out.write(responseBytes)
        }
    }
}
