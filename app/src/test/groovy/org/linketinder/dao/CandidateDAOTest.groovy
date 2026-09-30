package org.linketinder.dao

import spock.lang.Specification

import java.sql.Connection
import java.sql.ResultSet
import java.sql.PreparedStatement

class CandidateDAOTest extends Specification {
    /**
     * teste de integração para verificar se a classe CandidateDAO é instanciada corretamente e se a conexão com o banco de dados é estabelecida com sucesso.
     */
    def "deve criar uma instancia de CandidateDAO com sucesso"() {
        given: "uma classe CandidateDAO"
        CandidateDAO candidateDAO = new CandidateDAO()

        when: "testamos a conexão"
        boolean conexaoEstabelecida = candidateDAO.testarConexao()

        then: "a instancia deve ser criada e nao ser nula"
        conexaoEstabelecida == true
    }

    def "deve listar candidatos com sucesso"() {
        given: "as dependencias simuladas do banco"
        def connection = Mock(Connection)
        def statement = Mock(PreparedStatement)
        def resultSet = Mock(ResultSet)

        GroovyMock(ConexaoDB, global: true)
        ConexaoDB.getConnection() >> connection

        connection.prepareStatement(_) >> statement
        statement.executeQuery() >> resultSet

        resultSet.next() >>> [true, true, false]

        resultSet.getInt("id") >>> [1, 2]
        resultSet.getString("nome") >>> ["Fulano", "Ciclano"]
        resultSet.getString("email") >>>
                ["fulano@email.com", "ciclano@email.com"]

        and: "um DAO"
        CandidateDAO candidateDAO = new CandidateDAO()

        when: "listamos os candidatos"
        Map resultado = candidateDAO.listarCandidatos()

        then: "o DAO transforma o retorno do ResultSet na lista esperada"
        resultado.candidatos == [
                [id: 1, nome: "Fulano", email: "fulano@email.com"],
                [id: 2, nome: "Ciclano", email: "ciclano@email.com"]
        ]

        and: "os recursos sao fechados"
        1 * resultSet.close()
        1 * statement.close()
        1 * connection.close()
    }
}
