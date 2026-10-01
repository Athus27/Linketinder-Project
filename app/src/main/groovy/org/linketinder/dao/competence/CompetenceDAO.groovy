package org.linketinder.dao.competence

import org.linketinder.dao.ConexaoDB

class CompetenceDAO {
    Map listarCompetencias() {
        java.sql.Connection connection = null
        java.sql.PreparedStatement statement = null
        java.sql.ResultSet resultSet = null

        def competencias = []

        try {
            connection = ConexaoDB.getConnection()
            statement = connection.prepareStatement(
                    """
                        SELECT id_competencia AS id, nome AS nome
                        FROM "Competence"
                    """
            )
            resultSet = statement.executeQuery()

            while (resultSet.next()) {
                competencias << [id  : resultSet.getInt("id"),
                                 nome: resultSet.getString("nome")]
            }

        } catch (Exception e) {
            println("Erro ao listar competências: ${e.message}")
        } finally {
            try {
                if (resultSet != null) resultSet.close()
                if (statement != null) statement.close()
                if (connection != null) connection.close()
            } catch (Exception e) {
                println("Erro ao fechar recursos: ${e.message}")
            }
        }
        return [competencias: competencias]
    }
}
