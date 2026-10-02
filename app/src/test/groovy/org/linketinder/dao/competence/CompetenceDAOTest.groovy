package org.linketinder.dao.competence

import org.linketinder.controller.CompetenceController
import org.linketinder.dao.ConexaoDB
import spock.lang.Specification

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet

class CompetenceDAOTest extends Specification {
    def "ListarCompetencias"() {
        given: "dado uma resposta simulada do BD"
        //Simulando conexão c BD
        Connection connection = Mock()
        PreparedStatement statement = Mock()
        ResultSet resultSet = Mock()

        GroovyMock(ConexaoDB, global: true)

        //Simulando query
        ConexaoDB.getConnection() >> connection
        connection.prepareStatement(_) >> statement
        statement.executeQuery() >> resultSet

        // >>> faz retornar valores diferentes em chamadas sucessivas
        resultSet.next() >>> [true, true, true, true, false]
        resultSet.getInt("id") >>> [1, 2, 3, 4]
        resultSet.getString("competencia") >>> ["Competencia1", "Competencia2", "Competencia3", "Competencia4"]

        CompetenceDAO competenceDAO = new CompetenceDAO()

        when: "listamos competencias"
        Map resultado = competenceDAO.listarCompetencias()

        then: "todas as comps são retornadas"
        resultado.competencias == [[id: 1, nome: 'Competencia1'],
                                   [id: 2, nome: 'Competencia2'],
                                   [id: 3, nome: 'Competencia3'],
                                   [id: 4, nome: 'Competencia4']]


        and: "recursos do BD são fechados"
        //1* met          espeera q metodo seja chamado 1 vez
        1 * resultSet.close()
        1 * statement.close()
        1 * connection.close()

    }

    def "retorna competencia existente usando a conexao recebida"() {
        given:
        Connection connection = Mock()
        PreparedStatement statement = Mock()
        ResultSet resultSet = Mock()

        //Simulando query
        connection.prepareStatement(_) >> statement
        statement.executeQuery() >> resultSet

        resultSet.next() >> true
        resultSet.getInt("id") >> 2
        resultSet.getString("competencia") >> "Groovy"

        CompetenceDAO dao = new CompetenceDAO()

        when:
        Map resultado = dao.buscarOuCriarCompetencia(connection, "Groovy")

        then:
        resultado == [id: 2, nome: "Groovy"]

        and:
        1 * statement.setString(1, "Groovy")
        1 * resultSet.close()
        1 * statement.close()
        0 * connection.close()

    }

    def "deve criar e retornar competencia inexistente"() {
        given:
        Connection connection = Mock()

        PreparedStatement selectStatement = Mock()
        PreparedStatement insertStatement = Mock()

        ResultSet selectResultSet = Mock()
        ResultSet insertResultSet = Mock()

        connection.prepareStatement(_) >>> [selectStatement, insertStatement]

        selectStatement.executeQuery() >> selectResultSet
        insertStatement.executeQuery() >> insertResultSet

        selectResultSet.next() >> false

        insertResultSet.next() >> true
        insertResultSet.getInt("id") >> 17
        insertResultSet.getString("competencia") >> "Docker"

        CompetenceDAO dao = new CompetenceDAO()

        when:
        Map resultado = dao.buscarOuCriarCompetencia(connection, "Docker")

        then:
        resultado == [id: 17, nome: "Docker"]

        and:
        1 * selectStatement.setString(1, "Docker")
        1 * insertStatement.setString(1, "Docker")

        and:
        1 * selectResultSet.close()
        1 * insertResultSet.close()
        1 * selectStatement.close()
        1 * insertStatement.close()
        0 * connection.close()
    }
}
