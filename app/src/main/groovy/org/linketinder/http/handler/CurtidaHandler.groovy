package org.linketinder.http.handler

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.linketinder.auth.AuthPrincipal
import org.linketinder.auth.AuthSupport
import org.linketinder.auth.SessionStore
import org.linketinder.controller.AuthController
import org.linketinder.controller.CurtidaController
import org.linketinder.http.HttpLogger

class CurtidaHandler implements HttpHandler {
    private final CurtidaController controller
    private final AuthController auth
    private final SessionStore sessions

    CurtidaHandler(CurtidaController controller, AuthController auth, SessionStore sessions) {
        this.controller = controller; this.auth = auth; this.sessions = sessions
    }

    void handle(HttpExchange exchange) throws IOException {
        long start = HttpLogger.iniciar()
        try {
            AuthPrincipal principal = required(exchange)
            if (exchange.requestURI.path == "/api/curtidas" && exchange.requestMethod == "POST") {
                post(exchange, principal, start); return
            }
            if (exchange.requestURI.path == "/api/matches" && exchange.requestMethod == "GET") {
                String type = principal.tipo
                if (!(type in ["candidate", "company", "recruiter"])) {
                    respond(exchange, 403, error("Papel não permitido", "FORBIDDEN"), start); return
                }
                respond(exchange, 200, controller.listarMatches(principal.id, principal.idEmpresa, type), start); return
            }
            respond(exchange, 404, error("Rota não encontrada", "ROUTE_NOT_FOUND"), start)
        } catch (AuthFailure failure) {
            respond(exchange, failure.status, error(failure.message, failure.code), start)
        } catch (Exception ignored) {
            respond(exchange, 500, error("Erro interno do servidor", "INTERNAL_ERROR"), start)
        }
    }

    private void post(HttpExchange exchange, AuthPrincipal principal, long start) {
        Map data
        try {
            Object parsed = new JsonSlurper().parse(exchange.requestBody)
            if (!(parsed instanceof Map)) throw new IllegalArgumentException()
            data = parsed as Map
        } catch (Exception ignored) {
            respond(exchange, 400, error("JSON inválido", "INVALID_JSON"), start); return
        }
        Integer vaga = positive(data.idVaga)
        Integer candidate = positive(data.idCandidato)
        if (!vaga || (principal.tipo != "candidate" && !candidate)) {
            respond(exchange, 400, error("IDs inválidos", "INVALID_ID"), start); return
        }
        if (principal.tipo == "candidate") {
            if (data.containsKey("idCandidato") && !sameId(data.idCandidato, principal.id)) {
                respond(exchange, 403, error("Candidato não autorizado", "FORBIDDEN"), start); return
            }
            if (data.containsKey("idEmpresa") || data.containsKey("idRecrutador")) {
                respond(exchange, 403, error("Ator não autorizado", "FORBIDDEN"), start); return
            }
            Map result = controller.candidatoCurtirVaga(principal.id, vaga)
            complete(exchange, result, start); return
        }
        if (data.containsKey("idEmpresa") && !sameId(data.idEmpresa, principal.idEmpresa)) {
            respond(exchange, 403, error("Empresa não autorizada", "FORBIDDEN"), start); return
        }
        if (principal.tipo == "company" && data.containsKey("idRecrutador") ||
                principal.tipo == "recruiter" && data.containsKey("idRecrutador") &&
                !sameId(data.idRecrutador, principal.id)) {
            respond(exchange, 403, error("Recrutador não autorizado", "FORBIDDEN"), start); return
        }
        Map result = principal.tipo == "recruiter"
                ? controller.empresaCurtirCandidato(principal.idEmpresa, vaga, candidate, principal.id)
                : controller.empresaCurtirCandidato(principal.idEmpresa, vaga, candidate)
        complete(exchange, result, start)
    }

    private void complete(HttpExchange exchange, Map result, long start) {
        if (result.codigo == "NOT_FOUND") {
            respond(exchange, 404, error("Recurso não encontrado", "NOT_FOUND"), start); return
        }
        if (!result.sucesso) {
            respond(exchange, 500, error("Erro interno do servidor", "INTERNAL_ERROR"), start); return
        }
        respond(exchange, 200, [sucesso: true, jaCurtiu: result.jaCurtiu, deuMatch: result.deuMatch], start)
    }

    private AuthPrincipal required(HttpExchange exchange) {
        String token = AuthSupport.bearerToken(exchange)
        def session = sessions.resolve(token)
        if (!session.present) throw new AuthFailure(401, "Autenticação necessária", "UNAUTHENTICATED")
        AuthPrincipal principal = auth.validar(session.get())
        if (!principal) {
            sessions.revoke(token)
            throw new AuthFailure(401, "Sessão inválida", "UNAUTHENTICATED")
        }
        principal
    }
    private static Integer positive(Object value) {
        if (!(value instanceof Integer || value instanceof Long || value instanceof Short || value instanceof Byte)) return null
        long number = value.longValue()
        number > 0 && number <= Integer.MAX_VALUE ? number as int : null
    }
    private static boolean sameId(Object value, int expected) {
        Integer id = positive(value); id != null && id == expected
    }
    private static Map error(String message, String code) { [erro: message, codigo: code] }
    private static void respond(HttpExchange exchange, int status, Object body, long start) {
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
