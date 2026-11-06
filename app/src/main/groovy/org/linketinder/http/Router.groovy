package org.linketinder.http

import com.sun.net.httpserver.HttpServer
import org.linketinder.http.handler.CandidateHandler
import org.linketinder.http.handler.CompanyHandler
import org.linketinder.http.handler.VagaHandler
import org.linketinder.http.handler.AuthHandler
import org.linketinder.http.handler.RecruiterHandler

class Router {
    private final HttpServer server
    private final CandidateHandler candidateHandler
    private final CompanyHandler companyHandler
    private final VagaHandler vagaHandler
    private final AuthHandler authHandler
    private final RecruiterHandler recruiterHandler

    Router(
            HttpServer server,
            CandidateHandler candidateHandler,
            CompanyHandler companyHandler,
            VagaHandler vagaHandler,
            AuthHandler authHandler
    ) {
        this(server, candidateHandler, companyHandler, vagaHandler, authHandler, null)
    }

    Router(
            HttpServer server,
            CandidateHandler candidateHandler,
            CompanyHandler companyHandler,
            VagaHandler vagaHandler,
            AuthHandler authHandler,
            RecruiterHandler recruiterHandler
    ) {
        this.server = server
        this.candidateHandler = candidateHandler
        this.companyHandler = companyHandler
        this.vagaHandler = vagaHandler
        this.authHandler = authHandler
        this.recruiterHandler = recruiterHandler
    }

    void registrarRotas() {
        String frontendOrigin = System.getenv("FRONTEND_ORIGIN") ?: "http://localhost:5173"

        server.createContext(
                "/api/candidatos",
                candidateHandler
        ).filters.add(new CorsFilter(frontendOrigin, ["GET", "POST", "DELETE"]))
        server.createContext(
                "/api/empresas",
                companyHandler
        ).filters.add(new CorsFilter(frontendOrigin, ["GET", "POST"]))
        server.createContext(
                "/api/vagas",
                vagaHandler
        ).filters.add(new CorsFilter(frontendOrigin, ["GET", "POST"]))
        server.createContext(
                "/api/auth/login",
                authHandler
        ).filters.add(new CorsFilter(frontendOrigin, ["POST"]))
        if (recruiterHandler != null) {
            server.createContext(
                    "/api/auth/logout",
                    authHandler
            ).filters.add(new CorsFilter(frontendOrigin, ["POST"]))
            server.createContext(
                    "/api/recrutadores",
                    recruiterHandler
            ).filters.add(new CorsFilter(frontendOrigin, ["GET", "POST", "DELETE"]))
        }
    }
}
