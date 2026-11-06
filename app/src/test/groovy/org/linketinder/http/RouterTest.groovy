package org.linketinder.http

import com.sun.net.httpserver.HttpContext
import com.sun.net.httpserver.HttpServer
import org.linketinder.http.handler.CandidateHandler
import org.linketinder.http.handler.CompanyHandler
import org.linketinder.http.handler.VagaHandler
import spock.lang.Specification

class RouterTest extends Specification {

    def "deve registrar os três endpoints obrigatórios"() {
        given:
        HttpServer server = Mock()
        CandidateHandler candidateHandler = Mock()
        CompanyHandler companyHandler = Mock()
        VagaHandler vagaHandler = Mock()

        def candidateFilters = []
        def companyFilters = []
        def vagaFilters = []
        HttpContext candidateContext = Stub() { getFilters() >> candidateFilters }
        HttpContext companyContext = Stub() { getFilters() >> companyFilters }
        HttpContext vagaContext = Stub() { getFilters() >> vagaFilters }

        Router router = new Router(
                server,
                candidateHandler,
                companyHandler,
                vagaHandler
        )

        when:
        router.registrarRotas()

        then:
        1 * server.createContext("/api/candidatos", candidateHandler) >> candidateContext
        1 * server.createContext("/api/empresas", companyHandler) >> companyContext
        1 * server.createContext("/api/vagas", vagaHandler) >> vagaContext
        candidateFilters.size() == 1 && candidateFilters[0] instanceof CorsFilter
        companyFilters.size() == 1 && companyFilters[0] instanceof CorsFilter
        vagaFilters.size() == 1 && vagaFilters[0] instanceof CorsFilter
    }
}
