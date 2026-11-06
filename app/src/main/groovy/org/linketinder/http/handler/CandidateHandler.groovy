package org.linketinder.http.handler

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import org.linketinder.http.HttpLogger
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.linketinder.controller.CandidatoController
import java.sql.SQLException

class CandidateHandler implements HttpHandler {
    private static final String ENDPOINT = "/api/candidatos"
    private final CandidatoController candidatoController

    CandidateHandler(CandidatoController candidatoController) {
        this.candidatoController = candidatoController
    }

    @Override
    void handle(HttpExchange exchange) throws IOException {
        long inicio = HttpLogger.iniciar()

        String path = exchange.requestURI.path
        boolean rotaBase = path == ENDPOINT
        boolean rotaComId = path.startsWith("${ENDPOINT}/")

        if (!rotaBase && !rotaComId) {
            responder(exchange, 404, '{"erro":"Rota não encontrada"}', inicio)
            return
        }

        Integer id = null

        if (rotaComId) {
            String idTexto = path.substring("${ENDPOINT}/".length())

            try {
                id = Integer.parseInt(idTexto)
            } catch (NumberFormatException ignored) {
                responder(exchange, 400, '{"erro":"ID inválido"}', inicio)
                return
            }
        }

        String method = exchange.getRequestMethod()
        String responseText
        int status

        if (method == "GET") {
            try {
                if (id != null) {
                    Map candidato = candidatoController.buscarCandidatoPorId(id)

                    if (candidato == null) {
                        responder(
                                exchange,
                                404,
                                '{"erro":"Candidato não encontrado"}',
                                inicio
                        )
                        return
                    }

                    candidato.dataNascimento =
                            candidato.dataNascimento?.toString()

                    responder(
                            exchange,
                            200,
                            JsonOutput.toJson(candidato),
                            inicio
                    )
                    return
                }

                List<Map> candidatos = candidatoController.listarCandidatos()

                def dados = candidatos.collect { candidato ->
                    candidato + [
                            dataNascimento: candidato.dataNascimento?.toString()
                    ]
                }

                responder(
                        exchange,
                        200,
                        JsonOutput.toJson(dados),
                        inicio
                )
            } catch (Exception ignored) {
                responder(
                        exchange,
                        500,
                        '{"erro":"Erro ao listar candidatos"}',
                        inicio
                )
            }
            return
        }

        if (method == "DELETE") {
            if (id == null) {
                exchange.responseHeaders.set("Allow", "GET, POST")
                responder(exchange, 405, '{"erro":"DELETE exige o ID do candidato"}', inicio)
                return
            }

            Map candidato
            try {
                candidato = candidatoController.buscarCandidatoPorId(id)
            } catch (Exception ignored) {
                responder(
                        exchange,
                        500,
                        '{"erro":"Erro ao excluir candidato"}',
                        inicio
                )
                return
            }

            if (candidato == null) {
                responder(
                        exchange,
                        404,
                        '{"erro":"Candidato não encontrado"}',
                        inicio
                )
                return
            }

            boolean removido
            try {
                removido = candidatoController.removerCandidato(id)
            } catch (Exception ignored) {
                removido = false
            }

            if (!removido) {
                responder(
                        exchange,
                        500,
                        '{"erro":"Erro ao excluir candidato"}',
                        inicio
                )
                return
            }

            exchange.sendResponseHeaders(204, -1)
            exchange.close()
            HttpLogger.registrar(method, path, 204, inicio)
            return
        }

        if (method != "POST") {
            String allowedMethods = id == null ? "GET, POST" : "GET, DELETE"
            exchange.responseHeaders.set("Allow", allowedMethods)
            responder(exchange, 405, '{"erro":"Método não permitido"}', inicio)
            return
        }

        if (id != null) {
            exchange.responseHeaders.set("Allow", "GET, DELETE")
            responder(exchange, 405, '{"erro":"POST não permitido nesta rota"}', inicio)
            return
        }

        Map candidato

        try {
            candidato = new JsonSlurper().parse(exchange.requestBody) as Map
        } catch (Exception ignored) {
            responder(exchange, 400, '{"erro":"JSON inválido"}', inicio)
            return
        }

        if (!(candidato.senha instanceof String) || candidato.senha.length() < 6) {
            responder(exchange, 400, '{"erro":"A senha deve ter pelo menos 6 caracteres"}', inicio)
            return
        }

        try {
            boolean cadastrado = candidatoController.cadastrarCandidato(candidato)
            status = cadastrado ? 201 : 500
            responseText = cadastrado
                    ? '{"mensagem":"Candidato cadastrado com sucesso"}'
                    : '{"erro":"Erro ao persistir o candidato"}'
        } catch (SQLException exception) {
            if (exception.SQLState == "23505") {
                status = 409
                responseText = '{"erro":"Já existe um cadastro com esses dados"}'
            } else {
                status = 500
                responseText = '{"erro":"Erro interno do servidor"}'
            }
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
