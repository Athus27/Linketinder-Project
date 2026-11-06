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
        String frontendOrigin = System.getenv("FRONTEND_ORIGIN") ?: "http://localhost:5173"

        server.createContext(
                "/api/candidatos",
                candidateHandler
        ).filters.add(new CorsFilter(frontendOrigin, ["GET", "POST"]))
        server.createContext(
                "/api/empresas",
                companyHandler
        ).filters.add(new CorsFilter(frontendOrigin, ["GET", "POST"]))
        server.createContext(
                "/api/vagas",
                vagaHandler
        ).filters.add(new CorsFilter(frontendOrigin, ["GET", "POST"]))
    }
}
