package org.linketinder.http

import com.sun.net.httpserver.HttpServer
import org.linketinder.http.handler.CandidateHandler
import org.linketinder.http.handler.CompanyHandler
import org.linketinder.http.handler.VagaHandler

class Router {
    private final HttpServer server
    private final CandidateHandler candidateHandler
    private final CompanyHandler companyHandler
    private final VagaHandler vagaHandler

    Router(
            HttpServer server,
            CandidateHandler candidateHandler,
            CompanyHandler companyHandler,
            VagaHandler vagaHandler
    ) {
        this.server = server
        this.candidateHandler = candidateHandler
        this.companyHandler = companyHandler
        this.vagaHandler = vagaHandler
    }

    void registrarRotas() {
        server.createContext(
                "/api/candidatos",
                candidateHandler
        )
        server.createContext(
                "/api/empresas",
                companyHandler
        )
        server.createContext(
                "/api/vagas",
                vagaHandler
        )
    }
}
