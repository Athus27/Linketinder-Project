package org.linketinder.dao.competence

import org.linketinder.dao.ConexaoDB

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet

class CompetenceDAO {
    Map listarCompetencias() {
        Connection connection = null
        PreparedStatement statement = null
        ResultSet resultSet = null

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
        Connection connection = null
        PreparedStatement statement = null
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
        Connection connection = null
        PreparedStatement statement = null
        ResultSet resultSet = null

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
        Connection connection = null
        PreparedStatement statement = null
        ResultSet resultSet = null

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

    Map buscarOuCriarCompetencia(Connection connection, String competencia) {
        PreparedStatement selectStatement = null
        PreparedStatement insertStatement = null
        ResultSet selectResultSet = null
        ResultSet insertResultSet = null

        try {
            selectStatement = connection.prepareStatement(
                    """
                    SELECT id, competencia
                    FROM "Competencias"
                    WHERE LOWER(competencia) = LOWER(?)
                """
            )

            selectStatement.setString(1, competencia)
            selectResultSet = selectStatement.executeQuery()

            if (selectResultSet.next()) {
                return [
                        id  : selectResultSet.getInt("id"),
                        nome: selectResultSet.getString("competencia")
                ]
            }

            insertStatement = connection.prepareStatement(
                    """
                    INSERT INTO "Competencias" (competencia)
                    VALUES (?)
                    RETURNING id, competencia
                """
            )

            insertStatement.setString(1, competencia)
            insertResultSet = insertStatement.executeQuery()

            if (!insertResultSet.next()) {
                throw new java.sql.SQLException(
                        "O banco não retornou a competência criada"
                )
            }

            return [
                    id  : insertResultSet.getInt("id"),
                    nome: insertResultSet.getString("competencia")
            ]
        } finally {
            if (insertResultSet != null) insertResultSet.close()
            if (insertStatement != null) insertStatement.close()
            if (selectResultSet != null) selectResultSet.close()
            if (selectStatement != null) selectStatement.close()
        }
    }

    boolean removerCompetencia(int idCompetencia) {
        Connection connection = null
        boolean sucesso = false

        try {
            connection = ConexaoDB.getConnection()
            connection.setAutoCommit(false)

            List<String> comandos = [
                    'DELETE FROM "CandidatoCompetencia" WHERE id_competencia = ?',
                    'DELETE FROM "VagaCompetencia" WHERE id_competencia = ?',
                    'DELETE FROM "Competencias" WHERE id = ?'
            ]

            comandos.eachWithIndex { String sql, int indice ->
                PreparedStatement statement = connection.prepareStatement(sql)
                try {
                    statement.setInt(1, idCompetencia)
                    int linhasAfetadas = statement.executeUpdate()
                    if (indice == 2 && linhasAfetadas != 1) {
                        throw new java.sql.SQLException("Competência não encontrada")
                    }
                } finally {
                    statement.close()
                }
            }

            connection.commit()
            sucesso = true
            println("Competência removida com sucesso!")
        } catch (Exception e) {
            println("Erro ao remover competência: ${e.message}")
            if (connection != null) connection.rollback()
        } finally {
            if (connection != null) connection.close()
        }

        return sucesso
    }
}
