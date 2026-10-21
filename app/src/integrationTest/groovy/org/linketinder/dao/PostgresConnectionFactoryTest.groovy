package org.linketinder.dao


import org.linketinder.dao.connection.PostgresConnectionFactory
import spock.lang.Specification

import java.sql.Connection

class PostgresConnectionFactoryTest extends Specification {
    def "deve estabelecer conexao com o banco de dados com sucesso"() {
        when: "solicitamos uma conexao"
        PostgresConnectionFactory conexaoDB = new PostgresConnectionFactory()
        Connection conexao = conexaoDB.getConnection()

        then: "a conexao deve ser retornada e nao ser nula"
        conexao != null

        and: "a conexao deve estar aberta (valida)"
        !conexao.isClosed()

        cleanup: "garante que a conexao seja fechada apos o teste"
        if (conexao != null &&
            !conexao.isClosed()) {
            conexao.close()
        }
    }

    def "mostrarTabelas deve listar todas as tabelas do banco de dados"() {
        given: "uma instancia de PostgresConnectionFactory"
        PrintStream saidaOriginal = System.out
        ByteArrayOutputStream saidaCapturada = new ByteArrayOutputStream()
        System.setOut(new PrintStream(saidaCapturada))

        PostgresConnectionFactory conexaoDB = new PostgresConnectionFactory()

        when: "chamamos o metodo mostrarTabelas"
        boolean resultado = conexaoDB.mostrarTabelas()

        then: "o metodo deve retornar true indicando que as tabelas foram listadas com sucesso"
        resultado == true &&
        saidaCapturada.toString().readLines().contains("User") &&
        saidaCapturada.toString().readLines().contains("Candidate") &&
        saidaCapturada.toString().readLines().contains("Company") &&
        saidaCapturada.toString().readLines().contains("Vaga") &&
        saidaCapturada.toString().readLines().contains("Curtida") &&
        saidaCapturada.toString().readLines().contains("Competencias") &&
        saidaCapturada.toString().readLines().contains("CandidatoCompetencia")
        saidaCapturada.toString().readLines().contains("VagaCompetencia")

        cleanup:
        System.setOut(saidaOriginal)
    }
}
