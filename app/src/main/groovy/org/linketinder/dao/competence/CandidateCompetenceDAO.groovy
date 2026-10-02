package org.linketinder.dao.competence

import org.linketinder.dao.ConexaoDB

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet

class CandidateCompetenceDAO {
    List<String> listarCompetenciasDoCandidato(int idCandidato) {
        Connection connection = null
        PreparedStatement statement = null
        ResultSet resultSet = null

        List<String> competencias = []

        try {
            connection = ConexaoDB.getConnection()

            statement = connection.prepareStatement(
                    """
            SELECT competencia.competencia
            FROM "CandidatoCompetencia" AS candidato_competencia
            JOIN "Competencias" AS competencia
                ON competencia.id = candidato_competencia.id_competencia
            WHERE candidato_competencia.id_candidato = ?
            ORDER BY competencia.competencia
        """
            )

            statement.setInt(1, idCandidato)

            resultSet = statement.executeQuery()

            while (resultSet.next()) {
                competencias << resultSet.getString("competencia")
            }
        } finally {
            if (resultSet != null) resultSet.close()
            if (statement != null) statement.close()
            if (connection != null) connection.close()
        }

        return competencias
    }

    boolean adicionarCompetenciaAoCandidato(Connection connection, int idCandidato, int idCompetencia) {
        PreparedStatement statement = null

        try {
            statement = connection.prepareStatement(
                    """
                            INSERT INTO "CandidatoCompetencia"
                                (id_candidato,id_competencia)
                            VALUES (?,?)
                        """
            )
            statement.setInt(1,idCandidato)
            statement.setInt(2,idCompetencia)

            int linhasAfetadas =  statement.executeUpdate() // lembrando q retorna 1,0 ou erro

            return linhasAfetadas == 1
        } finally{
            if (statement != null) statement.close()
        }
    }
}
