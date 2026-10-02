package org.linketinder.dao.competence

import spock.lang.Specification

import java.sql.Connection
import java.sql.PreparedStatement

class CandidateCompetenceDAOTest extends Specification {
    def "adicionarCompetenciaAoCandidato"() {
        given: "Simulando Mock de bd"

        //Simulando conexão c BD
        Connection connection = Mock()
        PreparedStatement statement = Mock()

        connection.prepareStatement(_) >> statement
        statement.executeUpdate() >> 1

        CandidateCompetenceDAO candidateCompetenceDAO = new CandidateCompetenceDAO()

        when: "add relação"
        boolean resultado = candidateCompetenceDAO.adicionarCompetenciaAoCandidato(connection, 1, 2)

        then: "relação inserida"
        resultado

        and: "IDs enviados corretamente"
        1 * statement.setInt(1, 1)
        1 * statement.setInt(2, 2)

        and: "statement fechado, mas conexão n"
        1 * statement.close()
        0 * connection.close()
    }
}
