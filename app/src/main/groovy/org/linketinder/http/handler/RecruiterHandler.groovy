package org.linketinder.http.handler

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.linketinder.auth.AuthPrincipal
import org.linketinder.auth.AuthSupport
import org.linketinder.auth.SessionStore
import org.linketinder.controller.AuthController
import org.linketinder.controller.RecruiterController
import org.linketinder.http.HttpLogger

import java.sql.SQLException
import java.util.Objects
import java.math.BigInteger

class RecruiterHandler implements HttpHandler {
    private static final String ENDPOINT = "/api/recrutadores"
    private final RecruiterController recruiterController
    private final AuthController authController
    private final SessionStore sessionStore

    RecruiterHandler(
            RecruiterController recruiterController,
            AuthController authController,
            SessionStore sessionStore
    ) {
        this.recruiterController = recruiterController
        this.authController = authController
            this.sessionStore = Objects.requireNonNull(sessionStore, "sessionStore")
    }

    @Override
    void handle(HttpExchange exchange) throws IOException {
        long start = HttpLogger.iniciar()
        String path = exchange.requestURI.path
        if (!(path == ENDPOINT || path.startsWith(ENDPOINT + "/"))) {
            respond(exchange, 404, error("Rota não encontrada", "ROUTE_NOT_FOUND"), start)
            return
        }
        try {
            String token = AuthSupport.bearerToken(exchange)
            Optional<AuthPrincipal> session = sessionStore.resolve(token)
            if (!session.present) {
                respond(exchange, 401, error("Autenticação necessária", "UNAUTHENTICATED"), start)
                return
            }
            AuthPrincipal principal = authController.validar(session.get())
            if (principal == null) {
                sessionStore.revoke(token)
                respond(exchange, 401, error("Sessão inválida", "UNAUTHENTICATED"), start)
                return
            }
            if (principal.tipo != "company") {
                respond(exchange, 403, error("Papel não permitido", "FORBIDDEN"), start)
                return
            }
            if (path == ENDPOINT && exchange.requestMethod == "GET") {
                respond(exchange, 200, recruiterController.listar(principal.idEmpresa), start)
                return
            }
            if (path == ENDPOINT && exchange.requestMethod == "POST") {
                create(exchange, principal, start)
                return
            }
            if (path.startsWith(ENDPOINT + "/") && exchange.requestMethod == "DELETE") {
                delete(exchange, path.substring((ENDPOINT + "/").length()), principal, start)
                return
            }
            exchange.responseHeaders.set("Allow", path == ENDPOINT ? "GET, POST" : "DELETE")
            respond(exchange, 405, error("Método não permitido", "METHOD_NOT_ALLOWED"), start)
        } catch (Exception ignored) {
            respond(exchange, 500, error("Erro interno do servidor", "INTERNAL_ERROR"), start)
        }
    }

    private void create(HttpExchange exchange, AuthPrincipal principal, long start) {
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
        if (data.containsKey("idEmpresa")) {
            Object submitted = data.idEmpresa
            if (!isExactPositiveInteger(submitted) ||
                    submitted.longValue() != principal.idEmpresa) {
                respond(exchange, 403, error("Empresa não autorizada", "FORBIDDEN"), start)
                return
            }
            data.remove("idEmpresa")
        }
        try {
            respond(exchange, 201, recruiterController.criar(data, principal.idEmpresa), start)
        } catch (IllegalArgumentException ignored) {
            respond(exchange, 400, error("Dados inválidos", "INVALID_INPUT"), start)
        } catch (SQLException exception) {
            if (exception.SQLState == "23505") {
                respond(exchange, 409, error("Cadastro já existente", "CONFLICT"), start)
            } else {
                respond(exchange, 500, error("Erro interno do servidor", "INTERNAL_ERROR"), start)
            }
        } catch (Exception ignored) {
            respond(exchange, 500, error("Erro interno do servidor", "INTERNAL_ERROR"), start)
        }
    }

    private void delete(
            HttpExchange exchange, String textId, AuthPrincipal principal, long start
    ) {
        int id
        try {
            id = Integer.parseInt(textId)
            if (id <= 0) {
                throw new NumberFormatException()
            }
        } catch (NumberFormatException ignored) {
            respond(exchange, 400, error("ID inválido", "INVALID_ID"), start)
            return
        }
        try {
            if (!recruiterController.desativar(id, principal.idEmpresa)) {
                respond(exchange, 404, error("Recrutador não encontrado", "NOT_FOUND"), start)
                return
            }
            exchange.sendResponseHeaders(204, -1)
            exchange.close()
            HttpLogger.registrar(exchange.requestMethod, exchange.requestURI.path, 204, start)
        } catch (Exception ignored) {
            respond(exchange, 500, error("Erro interno do servidor", "INTERNAL_ERROR"), start)
        }
    }

    private static Map error(String message, String code) {
        [erro: message, codigo: code]
    }

    private static boolean isExactPositiveInteger(Object value) {
        if (value instanceof BigInteger) {
            return value.signum() > 0
        }
        value instanceof Integer || value instanceof Long ||
                value instanceof Short || value instanceof Byte
                ? value.longValue() > 0
                : false
    }

    private static void respond(HttpExchange exchange, int status, Object body, long start) {
        byte[] bytes = JsonOutput.toJson(body).getBytes("UTF-8")
        exchange.responseHeaders.set("Content-Type", "application/json; charset=UTF-8")
        exchange.sendResponseHeaders(status, bytes.length)
        exchange.responseBody.withStream { it.write(bytes) }
        HttpLogger.registrar(exchange.requestMethod, exchange.requestURI.path, status, start)
    }
}
