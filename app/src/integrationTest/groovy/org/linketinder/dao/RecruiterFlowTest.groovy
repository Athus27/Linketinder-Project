package org.linketinder.dao

import org.linketinder.auth.AuthPrincipal
import org.linketinder.dao.user.AuthDAO
import org.linketinder.dao.user.RecruiterDAO
import spock.lang.Specification

class RecruiterFlowTest extends Specification {
    SyntheticDatabaseFixture fixture
    java.sql.Connection connection

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

    def "cria lista desativa e bloqueia login de recrutador no schema temporario"() {
        given:
        int company = insertUser("Empresa", "empresa@example.test", "Empresa123")
        sql('INSERT INTO "Company" (id_empresa, cnpj, descricao) VALUES (' +
                "${company}, '00000000000191', 'Empresa sintetica')")
        int otherCompany = insertUser("Outra", "outra@example.test", "Outra123")
        sql('INSERT INTO "Company" (id_empresa, cnpj, descricao) VALUES (' +
                "${otherCompany}, '00000000000272', 'Outra sintetica')")
        def dao = new RecruiterDAO(fixture.connectionFactory)
        def auth = new AuthDAO(fixture.connectionFactory)

        when:
        Map created = dao.criar([
                nome: "Recrutador", email: "recrutador@example.test",
                senha: "Senha123", area: "Tech", descricao: "Sintetico",
                cpf: "529.982.247-25", dataNascimento: java.time.LocalDate.of(1990, 1, 1)
        ], company)

        then:
        dao.listar(company)*.id == [created.id]
        dao.listar(otherCompany).isEmpty()
        auth.autenticar("RECRUTADOR@EXAMPLE.TEST", "Senha123").with {
            id == created.id && tipo == "recruiter" && idEmpresa == company
        }

        when:
        def principal = new AuthPrincipal(
                created.id as Integer, "Recrutador", "recruiter", company
        )
        boolean deactivated = dao.desativar(created.id as int, company)

        then:
        deactivated
        auth.autenticar("recrutador@example.test", "Senha123") == null
        auth.validarPrincipal(principal) == null
        !dao.desativar(created.id as int, otherCompany)
        dao.listar(company)[0].ativo == false
    }

    def "rollback completo em CPF duplicado e conflito de email normalizado"() {
        given:
        int company = insertUser("Empresa", "empresa-duplicados@example.test", "Empresa123")
        sql('INSERT INTO "Company" (id_empresa, cnpj, descricao) VALUES (' +
                "${company}, '00000000000353', 'Empresa sintetica')")
        def dao = new RecruiterDAO(fixture.connectionFactory)
        Map base = [
                nome: "Recrutador", email: "base@example.test", senha: "Senha123",
                area: "Tech", descricao: "Sintetico", cpf: "529.982.247-25",
                dataNascimento: java.time.LocalDate.of(1990, 1, 1)
        ]
        dao.criar(base, company)

        when:
        dao.criar(base + [nome: "Outro", email: "novo-cpf@example.test"], company)

        then:
        def cpfError = thrown(java.sql.SQLException)
        cpfError.SQLState == "23505"
        countUsers("novo-cpf@example.test") == 0

        when:
        dao.criar(base + [email: " BASE@EXAMPLE.TEST ", cpf: "123.456.789-09"], company)

        then:
        def emailError = thrown(java.sql.SQLException)
        emailError.SQLState == "23505"
        !emailError.message.contains("BASE@EXAMPLE.TEST")
        countUsers("base@example.test") == 1
    }

    private int insertUser(String name, String email, String password) {
        def statement = connection.prepareStatement(
                '''INSERT INTO "User" (nome, email, "país", cep, senha)
                   VALUES (?, ?, NULL, NULL, crypt(?, gen_salt('bf'))) RETURNING id'''
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

    private void sql(String value) {
        connection.createStatement().execute(value)
    }

    private int countUsers(String email) {
        def statement = connection.prepareStatement(
                'SELECT count(*) FROM "User" WHERE lower(trim(email)) = lower(trim(?))'
        )
        statement.setString(1, email)
        def result = statement.executeQuery()
        result.next()
        int count = result.getInt(1)
        result.close()
        statement.close()
        count
    }
}
