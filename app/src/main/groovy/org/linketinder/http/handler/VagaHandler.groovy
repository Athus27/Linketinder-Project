package org.linketinder.http.handler

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import org.linketinder.http.HttpLogger
import groovy.json.JsonOutput
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
        long inicio = HttpLogger.iniciar()

        if (exchange.requestURI.path != ENDPOINT) {
            responder(exchange, 404, '{"erro":"Rota não encontrada"}', inicio)
            return
        }

        if (exchange.requestMethod == "GET") {
            try {
                List<Map> registros = vagaController.listarVagas()
                responder(exchange, 200, JsonOutput.toJson(registros), inicio)
            } catch (Exception ignored) {
                responder(exchange, 500, '{"erro":"Erro ao listar vagas"}', inicio)
            }
            return
        }

        if (exchange.requestMethod != "POST") {
            exchange.responseHeaders.set("Allow", "GET, POST")
            responder(exchange, 405, '{"erro":"Método não permitido"}', inicio)
            return
        }

        Map vaga
        try {
            vaga = new JsonSlurper().parse(exchange.requestBody) as Map
        } catch (Exception ignored) {
            responder(exchange, 400, '{"erro":"JSON inválido"}', inicio)
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

        responder(exchange, status, responseText, inicio)
    }

    private static void responder(
            HttpExchange exchange,
            int status,
            String responseText,
            long inicio
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

        HttpLogger.registrar(
                exchange.requestMethod,
                exchange.requestURI.path,
                status,
                inicio
        )
    }
}
