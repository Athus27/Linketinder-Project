package org.linketinder.http

import com.sun.net.httpserver.HttpServer
import org.linketinder.ApiApp
import org.linketinder.dao.SyntheticDatabaseFixture
import org.linketinder.dao.connection.DatabaseConnectionFactory
import spock.lang.Specification

import java.net.InetSocketAddress
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class ApiHttpProjectionTest extends Specification {
    SyntheticDatabaseFixture fixture
    HttpServer server
    HttpClient client
    URI base

    def setup() {
        fixture = new SyntheticDatabaseFixture(
                DatabaseConnectionFactory.getInstance().getFactoryDatabaseConnection()
        )
        insertFixtures()
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0)
        ApiApp.buildRouter(server, fixture.connectionFactory, new org.linketinder.auth.SessionStore())
                .registrarRotas()
        server.start()
        base = URI.create("http://127.0.0.1:${server.address.port}")
        client = HttpClient.newHttpClient()
    }

    def cleanup() {
        server?.stop(0)
        fixture?.close()
    }

    def "GETs públicos retornam 200 e projeções sem identidade"() {
        expect:
        HttpResponse<String> candidates = get("/api/candidatos")
        HttpResponse<String> companies = get("/api/empresas")
        HttpResponse<String> jobs = get("/api/vagas")
        candidates.statusCode() == 200
        companies.statusCode() == 200
        jobs.statusCode() == 200
        def candidateRows = parseJson(candidates.body())
        def companyRows = parseJson(companies.body())
        def jobRows = parseJson(jobs.body())
        candidateRows.size() == 1
        companyRows.size() == 1
        jobRows.size() == 1
        candidateRows.first().keySet() == ["id", "competencias", "formacao"] as Set
        companyRows.first().keySet() == ["id"] as Set
        jobRows.first().keySet() == ["id", "idEmpresa", "titulo", "descricao", "competencias"] as Set
    }

    def "Authorization malformado não vira visitante"() {
        when:
        HttpResponse<String> response = request("/api/candidatos", "NotBearer token")

        then:
        response.statusCode() == 401
        response.body().contains('"codigo":"UNAUTHENTICATED"')
    }

    private HttpResponse<String> get(String path) {
        request(path, null)
    }

    private HttpResponse<String> request(String path, String authorization) {
        def builder = HttpRequest.newBuilder(base.resolve(path)).GET()
        if (authorization != null) builder.header("Authorization", authorization)
        client.send(builder.build(), HttpResponse.BodyHandlers.ofString())
    }

    private static Object parseJson(String body) {
        def parser = Class.forName("groovy.json.JsonSlurper")
                .getDeclaredConstructor().newInstance()
        parser.parseText(body)
    }

    private void insertFixtures() {
        def connection = fixture.connectionFactory.getConnection()
        try {
            int candidate = insertUser(connection, "Candidato HTTP", "candidato-http@example.test")
            execute(connection, '''INSERT INTO "Candidate"
                (id_candidato, sobrenome, data_nascimento, cpf, descricao, formacao)
                VALUES (?, 'Teste', DATE '1995-01-01', '12345678901', 'Descricao privada', 'Tecnologia')''',
                    candidate)
            int company = insertUser(connection, "Empresa HTTP", "empresa-http@example.test")
            execute(connection, '''INSERT INTO "Company" (id_empresa, cnpj, descricao)
                VALUES (?, '12345678000199', 'Descricao empresarial')''', company)
            execute(connection, '''INSERT INTO "Vaga" (id_empresa, titulo, descricao, local_vaga)
                VALUES (?, 'Vaga HTTP', 'Descricao vaga', 'Remoto')''', company)
        } finally {
            connection.close()
        }
    }

    private int insertUser(java.sql.Connection connection, String name, String email) {
        def statement = connection.prepareStatement(
                '''INSERT INTO "User" (nome, email, "país", cep, senha)
                   VALUES (?, ?, 'Brasil', '01000-000', crypt('Password123', gen_salt('bf')))
                   RETURNING id'''
        )
        statement.setString(1, name)
        statement.setString(2, email)
        def result = statement.executeQuery()
        result.next()
        int id = result.getInt(1)
        result.close()
        statement.close()
        id
    }

    private void execute(java.sql.Connection connection, String sql, int id) {
        def statement = connection.prepareStatement(sql)
        statement.setInt(1, id)
        statement.executeUpdate()
        statement.close()
    }
}
