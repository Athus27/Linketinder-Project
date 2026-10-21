package org.linketinder.dao

import org.linketinder.dao.connection.ConnectionFactory
import org.linketinder.dao.connection.DatabaseConnectionFactory
import org.linketinder.dao.connection.PostgresConnectionFactory
import spock.lang.Specification

import java.sql.Connection

class PostgresConnectionFactoryTest extends Specification {
    private PostgresConnectionFactory conexaoDB
    private Connection conexao

    private static Properties DbProps = new Properties()

    static {
        def resourceName = "db.properties"
        def classLoader = Thread.currentThread().getContextClassLoader()
        def resourceStream = classLoader.getResourceAsStream(resourceName)

        if (resourceStream) {
            try {
                DbProps.load(resourceStream)
            } finally {
                resourceStream.close()
            }
        } else {
            throw new FileNotFoundException("Arquivo de propriedades '${resourceName}' não encontrado no classpath.")
        }

    }

    def setup (){
        conexaoDB = new PostgresConnectionFactory(DbProps)
        conexao = conexaoDB.getConnection()
    }
    def "deve estabelecer conexao com o banco de dados com sucesso"() {
        expect: "quando solicitamos uma conexao, a conexao deve ser retornada e nao ser nula"
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
