package org.linketinder.http.handler

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.linketinder.controller.CandidatoController

class CandidateHandler implements HttpHandler {
    private static final String ENDPOINT = "/api/candidatos"
    private final CandidatoController candidatoController

    CandidateHandler(CandidatoController candidatoController) {
        this.candidatoController = candidatoController
    }

    @Override
    void handle(HttpExchange exchange) throws IOException {
        if (exchange.requestURI.path != ENDPOINT) {
            responder(exchange, 404, '{"erro":"Rota não encontrada"}')
            return
        }

        String method = exchange.getRequestMethod()
        String responseText
        int status

        if (method == "GET") {
            try {
                List<Map> candidatos = candidatoController.listarCandidatos()

                def dados = candidatos.collect { candidato ->
                    candidato + [
                            dataNascimento: candidato.dataNascimento?.toString()
                    ]
                }

                responder(exchange, 200, JsonOutput.toJson(dados))
            } catch (Exception ignored) {
                responder(exchange, 500, '{"erro":"Erro ao listar candidatos"}')
            }
            return
        }

        if (method != "POST") {
            exchange.responseHeaders.set("Allow", "GET, POST")
            responder(exchange, 405, '{"erro":"Método não permitido"}')
            return
        }

        Map candidato
        try {
            candidato = new JsonSlurper().parse(exchange.requestBody) as Map
        } catch (Exception ignored) {
            responder(exchange, 400, '{"erro":"JSON inválido"}')
            return
        }

        try {
            boolean cadastrado = candidatoController.cadastrarCandidato(candidato)
            status = cadastrado ? 201 : 500
            responseText = cadastrado
                    ? '{"mensagem":"Candidato cadastrado com sucesso"}'
                    : '{"erro":"Erro ao persistir o candidato"}'
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
