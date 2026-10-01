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
                        SELECT id AS id, competencia
                        FROM "Competencias"
                        order by competencia
                    """
            )
            resultSet = statement.executeQuery()

            while (resultSet.next()) {
                competencias << [id  : resultSet.getInt("id"),
                                 nome: resultSet.getString("competencia")]
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

    boolean adicionarCompetencia(String nome) {
        java.sql.Connection connection = null
        java.sql.PreparedStatement statement = null
        boolean sucesso = false

        if (checarCompetenciaExistente(nome)) {
            println("Competência '${nome}' já existe no banco de dados.")
            return false
        }

        try {
            connection = ConexaoDB.getConnection()
            statement = connection.prepareStatement(
                    """
                        INSERT INTO "Competencias" (competencia)
                        VALUES (?)
                    """
            )
            statement.setString(1, nome)
            int rowsAffected = statement.executeUpdate()
            sucesso = rowsAffected > 0

        } catch (Exception e) {
            println("Erro ao adicionar competência: ${e.message}")
        } finally {
            try {
                if (statement != null) statement.close()
                if (connection != null) connection.close()
            } catch (Exception e) {
                println("Erro ao fechar recursos: ${e.message}")
            }
        }
        return sucesso
    }

    boolean checarCompetenciaExistente(String nome) {
        java.sql.Connection connection = null
        java.sql.PreparedStatement statement = null
        java.sql.ResultSet resultSet = null

        boolean existe = false

        try {
            connection = ConexaoDB.getConnection()
            statement = connection.prepareStatement(
                    """
                        SELECT 1
                        FROM "Competencias"
                        WHERE LOWER(competencia) = LOWER(?)
                        """
            )
            statement.setString(1, nome)
            resultSet = statement.executeQuery()

            existe = resultSet.next()

        } catch (Exception e) {
            println("Erro ao verificar competência existente: ${e.message}")
        } finally {
            try {
                if (resultSet != null) resultSet.close()
                if (statement != null) statement.close()
                if (connection != null) connection.close()
            } catch (Exception e) {
                println("Erro ao fechar recursos: ${e.message}")
            }
        }
        return existe
    }

    Map getCompetenciaPorNome(String nome) {
        java.sql.Connection connection = null
        java.sql.PreparedStatement statement = null
        java.sql.ResultSet resultSet = null

        Map competencia = null

        try {
            connection = ConexaoDB.getConnection()
            statement = connection.prepareStatement(
                    """
                        SELECT id, competencia
                        FROM "Competencias"
                        WHERE LOWER(competencia) = LOWER(?)
                    """
            )
            statement.setString(1, nome)
            resultSet = statement.executeQuery()

            if (resultSet.next()) {
                competencia = [id  : resultSet.getInt("id"),
                               nome: resultSet.getString("competencia")]
            }

        } catch (Exception e) {
            println("Erro ao buscar competência por nome: ${e.message}")
        } finally {
            try {
                if (resultSet != null) resultSet.close()
                if (statement != null) statement.close()
                if (connection != null) connection.close()
            } catch (Exception e) {
                println("Erro ao fechar recursos: ${e.message}")
            }
        }
        return competencia
    }
}
