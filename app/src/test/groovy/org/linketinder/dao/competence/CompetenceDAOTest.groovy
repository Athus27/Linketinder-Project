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
        resultSet.getInt("id") >>> [1,2,3,4]
        resultSet.getString("competencia")>>>["Competencia1","Competencia2","Competencia3","Competencia4"]

        CompetenceDAO competenceDAO = new CompetenceDAO()

        when: "listamos competencias"
        Map resultado = competenceDAO.listarCompetencias()

        then: "todas as comps são retornadas"
        resultado.competencias ==
            [
                    [id:1,nome: 'Competencia1'],
                    [id:2,nome: 'Competencia2'],
                    [id:3,nome: 'Competencia3'],
                    [id:4,nome: 'Competencia4']
            ]


        and: "recursos do BD são fechados"
        //1* met          espeera q metodo seja chamado 1 vez
        1*resultSet.close()
        1*statement.close()
        1*connection.close()

    }
}
