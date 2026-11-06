package org.linketinder.http.handler

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.linketinder.controller.CompanyController
import org.linketinder.http.HttpLogger

class CompanyHandler implements HttpHandler {
    private static final String ENDPOINT = "/api/empresas"
    private final CompanyController companyController

    CompanyHandler(CompanyController companyController) {
        this.companyController = companyController
    }

    @Override
    void handle(HttpExchange exchange) throws IOException {
        long inicio = HttpLogger.iniciar()
        if (exchange.requestURI.path != ENDPOINT) {
            responder(exchange, 404, '{"erro":"Rota não encontrada"}',inicio)
            return
        }

        if (exchange.requestMethod == "GET") {
            try {
                List<Map> registros = companyController.listarEmpresas()
                responder(exchange, 200, JsonOutput.toJson(registros),inicio)
            } catch (Exception ignored) {
                responder(exchange, 500, '{"erro":"Erro ao listar empresas"}',inicio)
            }
            return
        }

        if (exchange.requestMethod != "POST") {
            exchange.responseHeaders.set("Allow", "GET, POST")
            responder(exchange, 405, '{"erro":"Método não permitido"}',inicio)
            return
        }

        Map empresa
        try {
            empresa = new JsonSlurper().parse(exchange.requestBody) as Map
        } catch (Exception ignored) {
            responder(exchange, 400, '{"erro":"JSON inválido"}',inicio)
            return
        }

        String responseText
        int status

        try {
            boolean cadastrada = companyController.cadastrarEmpresa(empresa)
            status = cadastrada ? 201 : 500
            responseText = cadastrada
                    ? '{"mensagem":"Empresa cadastrada com sucesso"}'
                    : '{"erro":"Erro ao persistir a empresa"}'
        } catch (Exception ignored) {
            status = 500
            responseText = '{"erro":"Erro interno do servidor"}'
        }

        responder(exchange, status, responseText,inicio)
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
