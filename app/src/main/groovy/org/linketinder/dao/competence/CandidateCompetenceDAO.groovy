package org.linketinder.dao.competence

import org.linketinder.dao.ConexaoDB

class CandidateCompetenceDAO {
    List<String> listarCompetenciasDoCandidato(int idCandidato) {
        java.sql.Connection connection = null
        java.sql.PreparedStatement statement = null
        java.sql.ResultSet resultSet = null

        List<String> competencias = []

        try {
            connection = ConexaoDB.getConnection()

            statement = connection.prepareStatement("""
            SELECT competencia.competencia
            FROM "CandidatoCompetencia" AS candidato_competencia
            JOIN "Competencias" AS competencia
                ON competencia.id = candidato_competencia.id_competencia
            WHERE candidato_competencia.id_candidato = ?
            ORDER BY competencia.competencia
        """)

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
}
