package org.linketinder

import com.sun.net.httpserver.HttpServer
import org.linketinder.config.HttpServerConfig
import org.linketinder.controller.CandidatoController
import org.linketinder.controller.CompanyController
import org.linketinder.controller.VagaController
import org.linketinder.controller.AuthController
import org.linketinder.dao.user.CandidateDAO
import org.linketinder.dao.user.CompanyDAO
import org.linketinder.dao.user.AuthDAO
import org.linketinder.dao.user.RecruiterDAO
import org.linketinder.dao.connection.ConnectionFactory
import org.linketinder.dao.connection.DatabaseConnectionFactory
import org.linketinder.auth.SessionStore
import org.linketinder.controller.RecruiterController
import org.linketinder.controller.CurtidaController
import org.linketinder.dao.curtida.CurtidaDAO
import org.linketinder.http.handler.CurtidaHandler
import org.linketinder.dao.vaga.VagaDAO
import org.linketinder.http.Router
import org.linketinder.http.handler.CandidateHandler
import org.linketinder.http.handler.CompanyHandler
import org.linketinder.http.handler.VagaHandler
import org.linketinder.http.handler.AuthHandler
import org.linketinder.http.handler.RecruiterHandler

class ApiApp {

    static void main(String[] args) {
        HttpServerConfig serverConfig = new HttpServerConfig()
        HttpServer server = serverConfig.criarServidor()
        ConnectionFactory connectionFactory =
                DatabaseConnectionFactory.getInstance().getFactoryDatabaseConnection()
        Router router = buildRouter(server, connectionFactory, new SessionStore())
        router.registrarRotas()
        server.start()

        println("API rodando em http://${serverConfig.host}:${serverConfig.port}")
    }

    static Router buildRouter(
            HttpServer server,
            ConnectionFactory connectionFactory,
            SessionStore sessionStore
    ) {
        CandidateDAO candidateDAO = new CandidateDAO(connectionFactory)

        CandidatoController candidatoController =
                new CandidatoController(candidateDAO)

        CompanyDAO companyDAO = new CompanyDAO(connectionFactory)
        CompanyController companyController =
                new CompanyController(companyDAO)

        VagaDAO vagaDAO = new VagaDAO(connectionFactory)
        VagaController vagaController =
                new VagaController(vagaDAO)

        AuthDAO authDAO = new AuthDAO(connectionFactory)
        AuthController authController = new AuthController(authDAO)
        AuthHandler authHandler = new AuthHandler(authController, sessionStore)
        CandidateHandler secureCandidateHandler =
                new CandidateHandler(candidatoController, authController, sessionStore)
        CompanyHandler secureCompanyHandler =
                new CompanyHandler(companyController, authController, sessionStore)
        VagaHandler secureVagaHandler =
                new VagaHandler(vagaController, authController, sessionStore)
        CurtidaHandler curtidaHandler = new CurtidaHandler(
                new CurtidaController(new CurtidaDAO(connectionFactory)),
                authController, sessionStore)
        RecruiterDAO recruiterDAO = new RecruiterDAO(connectionFactory)
        RecruiterController recruiterController = new RecruiterController(recruiterDAO)
        RecruiterHandler recruiterHandler =
                new RecruiterHandler(recruiterController, authController, sessionStore)

        return new Router(
                server,
        secureCandidateHandler,
        secureCompanyHandler,
        secureVagaHandler,
                authHandler,
        recruiterHandler,
        curtidaHandler
        )
    }
}
