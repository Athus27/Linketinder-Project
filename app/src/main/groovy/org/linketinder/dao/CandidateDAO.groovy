package org.linketinder.dao


class CandidateDAO {
    boolean testarConexao() {
        def connectoin = ConexaoDB.getConnection()

        if (connectoin != null && !connectoin.isClosed()) {
            println("Conexão estabelecida com sucesso!")
            connectoin.close()
            return true
        } else {
            println("Falha ao estabelecer conexão.")
            return false
        }
    }

    Map listarCandidatos() {
        java.sql.Connection connection = null
        java.sql.PreparedStatement statement = null
        java.sql.ResultSet resultSet = null

        def candidatos = []


        try {
            connection = ConexaoDB.getConnection()
            statement = connection.prepareStatement(
                    """
                        SELECT
                            candidate.id_candidato AS id,
                            user_data.nome AS nome,
                            user_data.email AS email
                        FROM "Candidate" AS candidate
                        JOIN "User" AS user_data
                            ON user_data.id = candidate.id_candidato
                    """
            )
            resultSet = statement.executeQuery()

            while (resultSet.next()) {
                candidatos << [id   : resultSet.getInt("id"),
                               nome : resultSet.getString("nome"),
                               email: resultSet.getString("email")]
            }

        } catch (Exception e) {
            println("Erro ao listar candidatos: ${e.message}")
        } finally {
            try {
                if (resultSet != null) resultSet.close()
                if (statement != null) statement.close()
                if (connection != null) connection.close()
            } catch (Exception e) {
                println("Erro ao fechar recursos: ${e.message}")
            }
        }
        return [candidatos: candidatos]
    }
}
