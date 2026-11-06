package org.linketinder.http

import com.sun.net.httpserver.HttpContext
import com.sun.net.httpserver.Filter
import com.sun.net.httpserver.Headers
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import org.linketinder.http.handler.CandidateHandler
import org.linketinder.http.handler.CompanyHandler
import org.linketinder.http.handler.VagaHandler
import org.linketinder.http.handler.AuthHandler
import spock.lang.Specification

class RouterTest extends Specification {

    def "deve permitir preflight DELETE só na rota de candidatos"() {
        given:
        HttpServer server = Mock()
        CandidateHandler candidateHandler = Mock()
        CompanyHandler companyHandler = Mock()
        VagaHandler vagaHandler = Mock()
        AuthHandler authHandler = Mock()
        def candidateFilters = []
        def companyFilters = []
        def vagaFilters = []
        def authFilters = []
        HttpContext candidateContext = Stub() { getFilters() >> candidateFilters }
        HttpContext companyContext = Stub() { getFilters() >> companyFilters }
        HttpContext vagaContext = Stub() { getFilters() >> vagaFilters }
        HttpContext authContext = Stub() { getFilters() >> authFilters }
        Router router = new Router(server, candidateHandler, companyHandler, vagaHandler, authHandler)
        def candidateResponseHeaders = new Headers()
        HttpExchange candidateExchange = preflightExchange("DELETE", candidateResponseHeaders)
        def companyResponseHeaders = new Headers()
        HttpExchange companyExchange = preflightExchange("DELETE", companyResponseHeaders)
        def vagaResponseHeaders = new Headers()
        HttpExchange vagaExchange = preflightExchange("DELETE", vagaResponseHeaders)

        when:
        router.registrarRotas()

        then:
        1 * server.createContext("/api/candidatos", candidateHandler) >> candidateContext
        1 * server.createContext("/api/empresas", companyHandler) >> companyContext
        1 * server.createContext("/api/vagas", vagaHandler) >> vagaContext
        1 * server.createContext("/api/auth/login", authHandler) >> authContext

        when: "o navegador pede DELETE para candidatos"
        (candidateFilters[0] as Filter).doFilter(candidateExchange, Mock(Filter.Chain))

        then:
        1 * candidateExchange.sendResponseHeaders(204, -1)
        1 * candidateExchange.close()
        candidateResponseHeaders.getFirst("Access-Control-Allow-Methods") == "GET, POST, DELETE"

        when: "o navegador pede DELETE para empresas"
        (companyFilters[0] as Filter).doFilter(companyExchange, Mock(Filter.Chain))

        then:
        1 * companyExchange.sendResponseHeaders(403, -1)
        1 * companyExchange.close()
        companyResponseHeaders.getFirst("Access-Control-Allow-Methods") == null

        when: "o navegador pede DELETE para vagas"
        (vagaFilters[0] as Filter).doFilter(vagaExchange, Mock(Filter.Chain))

        then:
        1 * vagaExchange.sendResponseHeaders(403, -1)
        1 * vagaExchange.close()
        vagaResponseHeaders.getFirst("Access-Control-Allow-Methods") == null
    }

    def "deve registrar os três endpoints obrigatórios"() {
        given:
        HttpServer server = Mock()
        CandidateHandler candidateHandler = Mock()
        CompanyHandler companyHandler = Mock()
        VagaHandler vagaHandler = Mock()
        AuthHandler authHandler = Mock()

        def candidateFilters = []
        def companyFilters = []
        def vagaFilters = []
        def authFilters = []
        HttpContext candidateContext = Stub() { getFilters() >> candidateFilters }
        HttpContext companyContext = Stub() { getFilters() >> companyFilters }
        HttpContext vagaContext = Stub() { getFilters() >> vagaFilters }
        HttpContext authContext = Stub() { getFilters() >> authFilters }

        Router router = new Router(
                server,
                candidateHandler,
                companyHandler,
                vagaHandler,
                authHandler
        )

        when:
        router.registrarRotas()

        then:
        1 * server.createContext("/api/candidatos", candidateHandler) >> candidateContext
        1 * server.createContext("/api/empresas", companyHandler) >> companyContext
        1 * server.createContext("/api/vagas", vagaHandler) >> vagaContext
        1 * server.createContext("/api/auth/login", authHandler) >> authContext
        candidateFilters.size() == 1 && candidateFilters[0] instanceof CorsFilter
        companyFilters.size() == 1 && companyFilters[0] instanceof CorsFilter
        vagaFilters.size() == 1 && vagaFilters[0] instanceof CorsFilter
        authFilters.size() == 1 && authFilters[0] instanceof CorsFilter
    }

    def "deve registrar a rota de login e permitir POST via CORS"() {
        given:
        HttpServer server = Mock()
        CandidateHandler candidateHandler = Mock()
        CompanyHandler companyHandler = Mock()
        VagaHandler vagaHandler = Mock()
        AuthHandler authHandler = Mock()
        def filters = []
        HttpExchange authExchange = preflightExchange("POST", new Headers())
        HttpContext candidateContext = Stub() { getFilters() >> [] }
        HttpContext companyContext = Stub() { getFilters() >> [] }
        HttpContext vagaContext = Stub() { getFilters() >> [] }
        HttpContext authContext = Stub() { getFilters() >> filters }
        Router router = new Router(server, candidateHandler, companyHandler, vagaHandler, authHandler)

        when:
        router.registrarRotas()

        then:
        1 * server.createContext("/api/candidatos", candidateHandler) >> candidateContext
        1 * server.createContext("/api/empresas", companyHandler) >> companyContext
        1 * server.createContext("/api/vagas", vagaHandler) >> vagaContext
        1 * server.createContext("/api/auth/login", authHandler) >> authContext

        when:
        (filters[0] as Filter).doFilter(authExchange, Mock(Filter.Chain))

        then:
        1 * authExchange.sendResponseHeaders(204, -1)
        1 * authExchange.close()
    }

    private HttpExchange preflightExchange(String method, Headers responseHeaders) {
        Headers headers = new Headers()
        headers.set("Origin", "http://localhost:5173")
        headers.set("Access-Control-Request-Method", method)
        HttpExchange exchange = Mock()
        exchange.requestHeaders >> headers
        exchange.responseHeaders >> responseHeaders
        exchange.requestMethod >> "OPTIONS"
        return exchange
    }
}
