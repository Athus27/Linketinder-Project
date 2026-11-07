package org.linketinder.dao

import org.linketinder.dao.connection.DatabaseConnectionFactory
import spock.lang.Specification

import java.sql.Connection

class RecruiterMigrationTest extends Specification {
    private SyntheticDatabaseFixture fixture
    private Connection connection

    def setup() {
        fixture = new SyntheticDatabaseFixture(
                DatabaseConnectionFactory.getInstance().getFactoryDatabaseConnection(), true
        )
        connection = fixture.connectionFactory.getConnection()
    }

    def cleanup() {
        connection?.close()
        fixture?.close()
    }

    def "migration keeps legacy users and permits null country and postal code for new profiles"() {
        when:
        insertUser(1, "Legado", "legado@example.test", "Brasil", "00000-000")
        insertUser(2, "Recrutador", "recrutador@example.test", null, null)

        then:
        row("SELECT \"país\", cep FROM \"User\" WHERE id = 1").getString(1) == "Brasil"
        row("SELECT \"país\", cep FROM \"User\" WHERE id = 2").getString(1) == null
    }

    def "recruiter has one user, belongs to one company, and a company accepts multiple recruiters"() {
        given:
        insertUser(10, "Empresa", "empresa@example.test", "Brasil", "11111-111")
        insertUser(11, "Um", "um@example.test", null, null)
        insertUser(12, "Dois", "dois@example.test", null, null)
        sql('INSERT INTO "Company" (id_empresa, cnpj, descricao) VALUES (10, \'00000000000191\', \'Empresa sintetica\')')

        when:
        insertRecruiter(11, 10, "Tech", "Descricao um", "123.456.789-09")
        insertRecruiter(12, 10, "People", "Descricao dois", "987.654.321-00")

        then:
        row('SELECT count(*) FROM "Recruiter" WHERE id_empresa = 10').getInt(1) == 2
        row('SELECT ativo FROM "Recruiter" WHERE id_recrutador = 11').getBoolean(1)

        when:
        insertRecruiter(11, 999, "Outra", "Nao deve persistir", "111.222.333-44")

        then:
        thrown(java.sql.SQLException)
    }

    def "cpf e normalizado antes da unicidade e recrutador pode ser desativado"() {
        given:
        insertUser(20, "Empresa", "empresa20@example.test", null, null)
        insertUser(21, "Recrutador", "recrutador20@example.test", null, null)
        sql('INSERT INTO "Company" (id_empresa, cnpj, descricao) VALUES (20, \'00000000000272\', \'Empresa sintetica\')')

        when:
        insertRecruiter(21, 20, "Tech", "Descricao", "123.456.789-09")

        then:
        row('SELECT cpf FROM "Recruiter" WHERE id_recrutador = 21').getString(1) == "12345678909"

        when:
        insertUser(22, "Duplicado", "duplicado@example.test", null, null)
        insertRecruiter(22, 20, "Tech", "Duplicado", "12345678909")

        then:
        thrown(java.sql.SQLException)

        when:
        sql('UPDATE "Recruiter" SET ativo = false WHERE id_recrutador = 21')

        then:
        !row('SELECT ativo FROM "Recruiter" WHERE id_recrutador = 21').getBoolean(1)
    }

    def "curtida aceita autor nulo ou recrutador existente e rejeita autor inexistente"() {
        given:
        insertUser(30, "Empresa", "empresa30@example.test", null, null)
        insertUser(31, "Candidato", "candidato30@example.test", "Brasil", "22222-222")
        insertUser(32, "Recrutador", "recrutador30@example.test", null, null)
        sql('INSERT INTO "Company" (id_empresa, cnpj, descricao) VALUES (30, \'00000000000353\', \'Empresa sintetica\')')
        sql('INSERT INTO "Candidate" (id_candidato, cpf, sobrenome, data_nascimento, descricao) VALUES (31, \'11111111111\', \'Teste\', \'1990-01-01\', \'Perfil sintetico\')')
        sql('INSERT INTO "Vaga" (id, id_empresa, titulo, descricao, local_vaga) VALUES (301, 30, \'Vaga sintetica\', \'Descricao\', \'Remoto\')')
        insertRecruiter(32, 30, "Tech", "Descricao", "222.333.444-55")

        when:
        sql('INSERT INTO "Curtida" (id_candidato, id_vaga, id_recrutador_autor) VALUES (31, 301, NULL)')
        sql('UPDATE "Curtida" SET id_recrutador_autor = 32 WHERE id_candidato = 31 AND id_vaga = 301')

        then:
        row('SELECT id_recrutador_autor FROM "Curtida" WHERE id_candidato = 31 AND id_vaga = 301').getInt(1) == 32

        when:
        sql('INSERT INTO "Curtida" (id_candidato, id_vaga, id_recrutador_autor) VALUES (31, 301, 999)')

        then:
        thrown(java.sql.SQLException)
    }

    def "fixture prepara conexoes independentes no mesmo schema para T3"() {
        expect:
        fixture.connectionFactory.getConnection().withCloseable { first ->
            fixture.connectionFactory.getConnection().withCloseable { second ->
                first.createStatement().executeQuery("SELECT current_schema()").withCloseable { result ->
                    result.next()
                    assert result.getString(1) == fixture.schema
                }
                second.createStatement().executeQuery("SELECT current_schema()").withCloseable { result ->
                    result.next()
                    assert result.getString(1) == fixture.schema
                }
                true
            }
            true
        }
    }

    def "fixture stores only a hash for synthetic user password"() {
        when:
        sql("INSERT INTO \"User\" (id, nome, email, \"país\", cep, senha) VALUES (40, 'Hash', 'hash@example.test', NULL, NULL, crypt('Senha123', gen_salt('bf')))")

        then:
        row('SELECT senha FROM "User" WHERE id = 40').getString(1) != "Senha123"
        row('SELECT senha FROM "User" WHERE id = 40').getString(1).startsWith('$2')
    }

    private void insertUser(int id, String name, String email, String country, String cep) {
        def statement = connection.prepareStatement(
                'INSERT INTO "User" (id, nome, email, "país", cep, senha) VALUES (?, ?, ?, ?, ?, ?)'
        )
        statement.setInt(1, id)
        statement.setString(2, name)
        statement.setString(3, email)
        statement.setString(4, country)
        statement.setString(5, cep)
        statement.setString(6, "hash-de-teste")
        statement.executeUpdate()
    }

    private void insertRecruiter(int id, int company, String area, String description, String cpf) {
        def statement = connection.prepareStatement(
                'INSERT INTO "Recruiter" (id_recrutador, id_empresa, area, descricao, cpf, data_nascimento) VALUES (?, ?, ?, ?, ?, ?)'
        )
        statement.setInt(1, id)
        statement.setInt(2, company)
        statement.setString(3, area)
        statement.setString(4, description)
        statement.setString(5, cpf)
        statement.setDate(6, java.sql.Date.valueOf("1990-01-01"))
        statement.executeUpdate()
    }

    private void sql(String statement) {
        connection.createStatement().execute(statement)
    }

    private java.sql.ResultSet row(String statement) {
        def result = connection.createStatement().executeQuery(statement)
        assert result.next()
        result
    }
}
