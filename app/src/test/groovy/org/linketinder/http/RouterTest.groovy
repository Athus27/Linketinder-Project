package org.linketinder.http

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

        Router router = new Router(
                server,
                candidateHandler,
                companyHandler,
                vagaHandler
        )

        when:
        router.registrarRotas()

        then:
        1 * server.createContext("/api/candidatos", candidateHandler)
        1 * server.createContext("/api/empresas", companyHandler)
        1 * server.createContext("/api/vagas", vagaHandler)
    }
}
