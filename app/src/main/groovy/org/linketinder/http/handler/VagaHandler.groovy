package org.linketinder.http.handler

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.linketinder.auth.AuthPrincipal
import org.linketinder.auth.AuthSupport
import org.linketinder.auth.SessionStore
import org.linketinder.controller.AuthController
import org.linketinder.controller.VagaController
import org.linketinder.http.HttpLogger
import java.util.Objects

class VagaHandler implements HttpHandler {
    private static final String ENDPOINT = "/api/vagas"
    private final VagaController controller
    private final AuthController auth
    private final SessionStore sessions

    VagaHandler(VagaController controller, AuthController auth, SessionStore sessions) {
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
                boolean own = exchange.requestURI.query?.split("&")?.any { it == "minhas=true" }
                if (own) {
                    if (!principal || !(principal.tipo in ["company", "recruiter"])) {
                        respond(exchange, 401, error("Autenticação necessária", "UNAUTHENTICATED"), start); return
                    }
                    respond(exchange, 200, controller.listarVagasPublicas(principal.idEmpresa), start)
                } else {
                    respond(exchange, 200, controller.listarVagasPublicas(), start)
                }
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
        try {
            AuthPrincipal principal = requiredPrincipal(exchange)
            if (principal.tipo != "company") {
                respond(exchange, 403, error("Papel não permitido", "FORBIDDEN"), start); return
            }
            Map data
            try {
                Object parsed = new JsonSlurper().parse(exchange.requestBody)
                if (!(parsed instanceof Map)) throw new IllegalArgumentException()
                data = parsed as Map
            } catch (Exception ignored) {
                respond(exchange, 400, error("JSON inválido", "INVALID_JSON"), start); return
            }
            if (data.containsKey("idEmpresa") && !positiveInteger(data.idEmpresa, principal.idEmpresa)) {
                respond(exchange, 403, error("Empresa não autorizada", "FORBIDDEN"), start); return
            }
            if (!(data.titulo instanceof String) || !data.titulo.trim() ||
                    !(data.descricao instanceof String) || !data.descricao.trim() ||
                    !(data.localVaga instanceof String) || !data.localVaga.trim() ||
                    !(data.competencias instanceof Collection) ||
                    data.competencias.any { !(it instanceof String) || !it.trim() }) {
                respond(exchange, 400, error("Campos obrigatórios inválidos", "INVALID_INPUT"), start); return
            }
            if (!controller.cadastrarVaga(data, principal.idEmpresa)) {
                respond(exchange, 500, error("Erro interno do servidor", "INTERNAL_ERROR"), start); return
            }
            respond(exchange, 201, [mensagem: "Vaga cadastrada com sucesso"], start)
        } catch (AuthFailure failure) {
            respond(exchange, failure.status, error(failure.message, failure.code), start)
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
    private static boolean positiveInteger(Object value, int expected) {
        value instanceof Integer || value instanceof Long || value instanceof Short || value instanceof Byte
                ? value.longValue() > 0 && value.longValue() == expected : false
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
