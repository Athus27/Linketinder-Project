package org.linketinder.http.handler

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.linketinder.auth.AuthPrincipal
import org.linketinder.auth.AuthSupport
import org.linketinder.auth.SessionStore
import org.linketinder.controller.AuthController
import org.linketinder.controller.CompanyController
import org.linketinder.http.HttpLogger
import java.util.Objects

class CompanyHandler implements HttpHandler {
    private static final String ENDPOINT = "/api/empresas"
    private final CompanyController controller
    private final AuthController auth
    private final SessionStore sessions

    CompanyHandler(CompanyController controller, AuthController auth, SessionStore sessions) {
        Objects.requireNonNull(controller)
        Objects.requireNonNull(auth)
        Objects.requireNonNull(sessions)
        this.controller = controller; this.auth = auth; this.sessions = sessions
    }

    void handle(HttpExchange exchange) throws IOException {
        long start = HttpLogger.iniciar()
        if (exchange.requestURI.path != ENDPOINT) {
            respond(exchange, 404, error("Rota não encontrada", "ROUTE_NOT_FOUND"), start); return
        }
        if (exchange.requestMethod == "GET") {
            try {
                AuthPrincipal principal = optionalPrincipal(exchange)
                Integer candidate = principal?.tipo == "candidate" ? principal.id : null
                respond(exchange, 200, controller.listarEmpresasPublicas(candidate), start)
            } catch (AuthFailure failure) {
                respond(exchange, failure.status, error(failure.message, failure.code), start)
            } catch (Exception ignored) {
                respond(exchange, 500, error("Erro interno do servidor", "INTERNAL_ERROR"), start)
            }
            return
        }
        if (exchange.requestMethod != "POST") {
            exchange.responseHeaders.set("Allow", "GET, POST")
            respond(exchange, 405, error("Método não permitido", "METHOD_NOT_ALLOWED"), start); return
        }
        Map data
        try {
            Object parsed = new JsonSlurper().parse(exchange.requestBody)
            if (!(parsed instanceof Map)) throw new IllegalArgumentException()
            data = parsed as Map
        } catch (Exception ignored) {
            respond(exchange, 400, error("JSON inválido", "INVALID_JSON"), start); return
        }
        if (!(data.senha instanceof String) || data.senha.length() < 6) {
            respond(exchange, 400, error("A senha deve ter pelo menos 6 caracteres", "INVALID_INPUT"), start); return
        }
        if (!(data.pais instanceof String) || !data.pais.trim() ||
                !(data.cep instanceof String) || !data.cep.trim()) {
            respond(exchange, 400, error("País e CEP são obrigatórios", "INVALID_INPUT"), start); return
        }
        try {
            if (!controller.cadastrarEmpresa(data)) {
                respond(exchange, 500, error("Erro interno do servidor", "INTERNAL_ERROR"), start); return
            }
            respond(exchange, 201, [mensagem: "Empresa cadastrada com sucesso"], start)
        } catch (java.sql.SQLException exception) {
            respond(exchange, exception.SQLState == "23505" ? 409 : 500,
                    error(exception.SQLState == "23505" ? "Cadastro já existente" : "Erro interno do servidor",
                            exception.SQLState == "23505" ? "CONFLICT" : "INTERNAL_ERROR"), start)
        } catch (Exception ignored) {
            respond(exchange, 500, error("Erro interno do servidor", "INTERNAL_ERROR"), start)
        }
    }

    private AuthPrincipal optionalPrincipal(HttpExchange exchange) {
        String token = AuthSupport.bearerToken(exchange)
        if (!token) {
            if (exchange.requestHeaders.getFirst("Authorization") != null) {
                throw new AuthFailure(401, "Sessão inválida", "UNAUTHENTICATED")
            }
            return null
        }
        requiredPrincipal(exchange)
    }
    private AuthPrincipal requiredPrincipal(HttpExchange exchange) {
        String token = AuthSupport.bearerToken(exchange)
        if (!token) throw new AuthFailure(401, "Autenticação necessária", "UNAUTHENTICATED")
        def session = sessions.resolve(token)
        if (!session.present) throw new AuthFailure(401, "Sessão inválida", "UNAUTHENTICATED")
        AuthPrincipal principal = auth.validar(session.get())
        if (!principal) {
            sessions.revoke(token)
            throw new AuthFailure(401, "Sessão inválida", "UNAUTHENTICATED")
        }
        principal
    }
    private static Map error(String message, String code) { [erro: message, codigo: code] }
    private void respond(HttpExchange exchange, int status, Object body, long start) {
        byte[] bytes = JsonOutput.toJson(body).getBytes("UTF-8")
        exchange.responseHeaders.set("Content-Type", "application/json; charset=UTF-8")
        exchange.sendResponseHeaders(status, bytes.length)
        exchange.responseBody.withStream { it.write(bytes) }
        HttpLogger.registrar(exchange.requestMethod, exchange.requestURI.path, status, start)
    }
    private static class AuthFailure extends RuntimeException {
        final int status; final String code
        AuthFailure(int status, String message, String code) { super(message); this.status = status; this.code = code }
    }
}
