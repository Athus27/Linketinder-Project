package org.linketinder

import com.sun.net.httpserver.HttpServer
import org.linketinder.config.HttpServerConfig
import org.linketinder.controller.CandidatoController
import org.linketinder.controller.CompanyController
import org.linketinder.controller.VagaController
import org.linketinder.dao.user.CandidateDAO
import org.linketinder.dao.user.CompanyDAO
import org.linketinder.dao.vaga.VagaDAO
import org.linketinder.http.Router
import org.linketinder.http.handler.CandidateHandler
import org.linketinder.http.handler.CompanyHandler
import org.linketinder.http.handler.VagaHandler

class ApiApp {

    static void main(String[] args) {
        HttpServerConfig serverConfig = new HttpServerConfig()
        HttpServer server = serverConfig.criarServidor()

        CandidateDAO candidateDAO = new CandidateDAO()

        CandidatoController candidatoController =
                new CandidatoController(candidateDAO)

        CandidateHandler candidateHandler =
                new CandidateHandler(candidatoController)

        CompanyDAO companyDAO = new CompanyDAO()
        CompanyController companyController =
                new CompanyController(companyDAO)
        CompanyHandler companyHandler =
                new CompanyHandler(companyController)

        VagaDAO vagaDAO = new VagaDAO()
        VagaController vagaController =
                new VagaController(vagaDAO)
        VagaHandler vagaHandler =
                new VagaHandler(vagaController)

        Router router =
                new Router(
                        server,
                        candidateHandler,
                        companyHandler,
                        vagaHandler
                )

        router.registrarRotas()
        server.start()

        println("API rodando em http://${serverConfig.host}:${serverConfig.port}")
    }
}
