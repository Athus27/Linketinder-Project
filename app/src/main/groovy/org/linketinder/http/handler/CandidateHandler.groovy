package org.linketinder.http.handler

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.linketinder.auth.AuthPrincipal
import org.linketinder.auth.AuthSupport
import org.linketinder.auth.SessionStore
import org.linketinder.controller.AuthController
import org.linketinder.controller.CandidatoController
import org.linketinder.http.HttpLogger
import java.util.Objects

class CandidateHandler implements HttpHandler {
    private static final String ENDPOINT = "/api/candidatos"
    private final CandidatoController controller
    private final AuthController auth
    private final SessionStore sessions

    CandidateHandler(CandidatoController controller, AuthController auth, SessionStore sessions) {
        Objects.requireNonNull(controller)
        Objects.requireNonNull(auth)
        Objects.requireNonNull(sessions)
        this.controller = controller
        this.auth = auth
        this.sessions = sessions
    }

    @Override
    void handle(HttpExchange exchange) throws IOException {
        long start = HttpLogger.iniciar()
        String path = exchange.requestURI.path
        if (!(path == ENDPOINT || path.startsWith(ENDPOINT + "/"))) {
            respond(exchange, 404, error("Rota não encontrada", "ROUTE_NOT_FOUND"), start); return
        }
        Integer id = null
        if (path.startsWith(ENDPOINT + "/")) {
            try { id = positiveId(path.substring((ENDPOINT + "/").length())) }
            catch (Exception ignored) {
                respond(exchange, 400, error("ID inválido", "INVALID_ID"), start); return
            }
        }
        if (exchange.requestMethod == "GET") {
            try {
                AuthPrincipal principal = optionalPrincipal(exchange)
                Integer company = principal?.tipo in ["company", "recruiter"] ? principal.idEmpresa : null
                Object body = id == null
                        ? controller.listarCandidatosPublicos(company)
                        : controller.buscarCandidatoPublico(id, company)
                if (body == null || (body instanceof List && body.isEmpty() && id != null)) {
                    respond(exchange, 404, error("Candidato não encontrado", "NOT_FOUND"), start); return
                }
                respond(exchange, 200, body, start); return
            } catch (AuthFailure failure) {
                respond(exchange, failure.status, error(failure.message, failure.code), start); return
            } catch (Exception ignored) {
                respond(exchange, 500, error("Erro interno do servidor", "INTERNAL_ERROR"), start); return
            }
        }
        if (exchange.requestMethod == "DELETE") {
            if (id == null) {
                exchange.responseHeaders.set("Allow", "GET, POST")
                respond(exchange, 405, error("DELETE exige o ID do candidato", "METHOD_NOT_ALLOWED"), start); return
            }
            try {
                AuthPrincipal principal = requiredPrincipal(exchange)
                if (principal.tipo != "candidate" || principal.id != id) {
                    respond(exchange, 403, error("Ação não permitida", "FORBIDDEN"), start); return
                }
                if (!controller.removerCandidato(id)) {
                    respond(exchange, 404, error("Candidato não encontrado", "NOT_FOUND"), start); return
                }
                exchange.sendResponseHeaders(204, -1); exchange.close()
                HttpLogger.registrar("DELETE", path, 204, start); return
            } catch (AuthFailure failure) {
                respond(exchange, failure.status, error(failure.message, failure.code), start); return
            } catch (Exception ignored) {
                respond(exchange, 500, error("Erro interno do servidor", "INTERNAL_ERROR"), start); return
            }
        }
        if (exchange.requestMethod != "POST" || id != null) {
            exchange.responseHeaders.set("Allow", id == null ? "GET, POST" : "GET, DELETE")
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
            if (!controller.cadastrarCandidato(data)) {
                respond(exchange, 500, error("Erro interno do servidor", "INTERNAL_ERROR"), start); return
            }
            respond(exchange, 201, [mensagem: "Candidato cadastrado com sucesso"], start)
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
        if (principal == null) {
            sessions.revoke(token)
            throw new AuthFailure(401, "Sessão inválida", "UNAUTHENTICATED")
        }
        principal
    }

    private static int positiveId(String text) {
        if (!(text ==~ /[0-9]+/)) throw new NumberFormatException()
        long value = Long.parseLong(text)
        if (value <= 0 || value > Integer.MAX_VALUE) throw new NumberFormatException()
        value as int
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
