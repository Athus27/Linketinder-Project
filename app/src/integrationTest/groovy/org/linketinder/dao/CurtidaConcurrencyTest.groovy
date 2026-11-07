package org.linketinder.dao

import org.linketinder.dao.curtida.CurtidaDAO
import spock.lang.Specification

import java.sql.Connection
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class CurtidaConcurrencyTest extends Specification {
    SyntheticDatabaseFixture fixture
    Connection connection

    def setup() {
        fixture = new SyntheticDatabaseFixture(
                org.linketinder.dao.connection.DatabaseConnectionFactory.getInstance()
                        .getFactoryDatabaseConnection()
        )
        connection = fixture.connectionFactory.getConnection()
    }

    def cleanup() {
        connection?.close()
        fixture?.close()
    }

    def "preserva primeiro autor e cria match nos dois sentidos sob concorrencia"() {
        given:
        int company = insertUser("Empresa", "empresa-concorrencia@example.test", "Empresa123")
        sql("INSERT INTO \"Company\" (id_empresa, cnpj, descricao) VALUES (${company}, '00000000000191', 'Sintetica')")
        int recruiterOne = insertRecruiter("r1@example.test", company, "52998224725")
        int recruiterTwo = insertRecruiter("r2@example.test", company, "11144477735")
        int candidate = insertCandidate("candidato@example.test", "52998224725")
        int firstJob = insertJob(company, "Primeira")
        int secondCandidate = insertCandidate("candidato2@example.test", "11144477735")
        int secondJob = insertJob(company, "Segunda")
        ExecutorService executor = Executors.newFixedThreadPool(2)
        CountDownLatch ready = new CountDownLatch(2)
        CountDownLatch start = new CountDownLatch(1)

        when:
        def futures = [recruiterOne, recruiterTwo].collect { int recruiter ->
            executor.submit({ ->
                ready.countDown()
                start.await()
                [idRecrutador: recruiter,
                 resultado: new CurtidaDAO(fixture.connectionFactory)
                         .empresaCurtirCandidato(company, firstJob, candidate, recruiter)]
            } as Callable<Map>)
        }
        assert ready.await(10, TimeUnit.SECONDS)
        start.countDown()
        def companyResults = futures.collect { it.get(10, TimeUnit.SECONDS) }
        assert companyResults.every { it.resultado.sucesso }
        assert companyResults.count { !it.resultado.jaCurtiu } == 1
        assert companyResults.count { it.resultado.jaCurtiu } == 1
        Map firstAuthor = companyResults.find { !it.resultado.jaCurtiu }
        Map candidateFirst = new CurtidaDAO(fixture.connectionFactory)
                .candidatoCurtirVaga(candidate, firstJob)
        int authorBeforeReplay = likeAuthor(candidate, firstJob)
        String timestampBeforeReplay = dataMatch(candidate, firstJob)
        Map replay = new CurtidaDAO(fixture.connectionFactory)
                .empresaCurtirCandidato(company, firstJob, candidate,
                        companyResults.find { it.idRecrutador != firstAuthor.idRecrutador }.idRecrutador)
        Map candidateBeforeCompany = new CurtidaDAO(fixture.connectionFactory)
                .candidatoCurtirVaga(secondCandidate, secondJob)
        Map companyAfterCandidate = new CurtidaDAO(fixture.connectionFactory)
                .empresaCurtirCandidato(company, secondJob, secondCandidate, recruiterOne)
        int authorAfterReplay = likeAuthor(candidate, firstJob)
        String timestampAfterReplay = dataMatch(candidate, firstJob)

        then:
        replay.sucesso
        authorBeforeReplay == firstAuthor.idRecrutador
        authorAfterReplay == authorBeforeReplay
        timestampBeforeReplay == timestampAfterReplay
        candidateFirst.deuMatch
        !candidateBeforeCompany.deuMatch
        companyAfterCandidate.deuMatch
        countLikes(candidate, firstJob) == 1
        countLikes(secondCandidate, secondJob) == 1
        likeAuthor(candidate, firstJob) in [recruiterOne, recruiterTwo]
        dataMatch(candidate, firstJob) != null
        dataMatch(secondCandidate, secondJob) != null

        cleanup:
        executor?.shutdownNow()
        executor?.awaitTermination(5, TimeUnit.SECONDS)
    }

    private int insertUser(String name, String email, String password) {
        def statement = connection.prepareStatement(
                '''INSERT INTO "User" (nome, email, "país", cep, senha)
                   VALUES (?, ' ' || ?, 'Brasil', '01000-000', crypt(?, gen_salt('bf')))
                   RETURNING id'''
        )
        statement.setString(1, name)
        statement.setString(2, email)
        statement.setString(3, password)
        def result = statement.executeQuery()
        result.next()
        int id = result.getInt(1)
        result.close()
        statement.close()
        id
    }

    private int insertRecruiter(String email, int company, String cpf) {
        int id = insertUser("Recrutador", email, "Recruiter123")
        def statement = connection.prepareStatement(
                '''INSERT INTO "Recruiter"
                   (id_recrutador, id_empresa, area, descricao, cpf, data_nascimento)
                   VALUES (?, ?, 'Tecnologia', 'Sintetico', ?, DATE '1990-01-01')'''
        )
        statement.setInt(1, id)
        statement.setInt(2, company)
        statement.setString(3, cpf)
        statement.executeUpdate()
        statement.close()
        id
    }

    private int insertCandidate(String email, String cpf) {
        int id = insertUser("Candidato", email, "Candidate123")
        def statement = connection.prepareStatement(
                '''INSERT INTO "Candidate"
                   (id_candidato, sobrenome, data_nascimento, cpf, descricao, formacao)
                   VALUES (?, 'Teste', DATE '1995-01-01', ?, 'Sintetico', 'Tecnologia')'''
        )
        statement.setInt(1, id)
        statement.setString(2, cpf)
        statement.executeUpdate()
        statement.close()
        id
    }

    private int insertJob(int company, String title) {
        def statement = connection.prepareStatement(
                '''INSERT INTO "Vaga" (id_empresa, titulo, descricao, local_vaga)
                   VALUES (?, ?, 'Sintetica', 'Remoto') RETURNING id'''
        )
        statement.setInt(1, company)
        statement.setString(2, title)
        def result = statement.executeQuery()
        result.next()
        int id = result.getInt(1)
        result.close()
        statement.close()
        id
    }

    private void sql(String value) {
        connection.createStatement().execute(value)
    }

    private int countLikes(int candidate, int job) {
        def statement = connection.prepareStatement(
                'SELECT count(*) FROM "Curtida" WHERE id_candidato = ? AND id_vaga = ?'
        )
        statement.setInt(1, candidate)
        statement.setInt(2, job)
        def result = statement.executeQuery()
        result.next()
        int count = result.getInt(1)
        result.close()
        statement.close()
        count
    }

    private int likeAuthor(int candidate, int job) {
        def statement = connection.prepareStatement(
                'SELECT id_recrutador_autor FROM "Curtida" WHERE id_candidato = ? AND id_vaga = ?'
        )
        statement.setInt(1, candidate)
        statement.setInt(2, job)
        def result = statement.executeQuery()
        result.next()
        int author = result.getInt(1)
        result.close()
        statement.close()
        author
    }

    private String dataMatch(int candidate, int job) {
        def statement = connection.prepareStatement(
                'SELECT data_match::text FROM "Curtida" WHERE id_candidato = ? AND id_vaga = ?'
        )
        statement.setInt(1, candidate)
        statement.setInt(2, job)
        def result = statement.executeQuery()
        result.next()
        String timestamp = result.getString(1)
        result.close()
        statement.close()
        timestamp
    }
}
