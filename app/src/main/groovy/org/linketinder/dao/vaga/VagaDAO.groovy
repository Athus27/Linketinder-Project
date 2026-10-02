package org.linketinder.dao.vaga

import org.linketinder.dao.ConexaoDB
import org.linketinder.dao.competence.CompetenceDAO
import org.linketinder.dao.competence.VagaCompetenceDAO

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException

class VagaDAO {

    Map listarVagas() {
        Connection connection = null
        PreparedStatement statement = null
        ResultSet resultSet = null
        List<Map> vagas = []

        try {
            connection = ConexaoDB.getConnection()
            statement = connection.prepareStatement(
                    """
                        SELECT
                            vaga.id AS id,
                            vaga.id_empresa AS id_empresa,
                            user_data.nome AS empresa,
                            vaga.titulo AS titulo,
                            vaga.descricao AS descricao,
                            vaga.local_vaga AS local_vaga
                        FROM "Vaga" AS vaga
                        JOIN "Company" AS company
                            ON company.id_empresa = vaga.id_empresa
                        JOIN "User" AS user_data
                            ON user_data.id = company.id_empresa
                        ORDER BY vaga.id
                    """
            )

            resultSet = statement.executeQuery()
            VagaCompetenceDAO vagaCompetenceDAO =
                    new VagaCompetenceDAO()

            while (resultSet.next()) {
                int idVaga = resultSet.getInt("id")

                vagas << [
                        id          : idVaga,
                        idEmpresa   : resultSet.getInt("id_empresa"),
                        empresa     : resultSet.getString("empresa"),
                        titulo      : resultSet.getString("titulo"),
                        descricao   : resultSet.getString("descricao"),
                        localVaga   : resultSet.getString("local_vaga"),
                        competencias: vagaCompetenceDAO
                                .listarCompetenciasDaVaga(idVaga)
                ]
            }
        } catch (Exception e) {
            println("Erro ao listar vagas: ${e.message}")
        } finally {
            if (resultSet != null) resultSet.close()
            if (statement != null) statement.close()
            if (connection != null) connection.close()
        }

        return [vagas: vagas]
    }

    boolean adicionarVaga(Map vaga) {
        Connection connection = null
        PreparedStatement statement = null
        ResultSet resultSet = null
        boolean sucesso = false

        try {
            connection = ConexaoDB.getConnection()
            connection.setAutoCommit(false)

            statement = connection.prepareStatement(
                    """
                        INSERT INTO "Vaga"
                            (id_empresa, titulo, descricao, local_vaga)
                        VALUES (?, ?, ?, ?)
                        RETURNING id
                    """
            )

            statement.setInt(1, vaga.idEmpresa as int)
            statement.setString(2, vaga.titulo)
            statement.setString(3, vaga.descricao)
            statement.setString(4, vaga.localVaga)

            resultSet = statement.executeQuery()

            if (!resultSet.next()) {
                throw new SQLException("O banco não retornou o ID da vaga")
            }

            int idVaga = resultSet.getInt("id")

            resultSet.close()
            resultSet = null
            statement.close()
            statement = null

            CompetenceDAO competenceDAO = new CompetenceDAO()
            VagaCompetenceDAO vagaCompetenciaDAO =
                    new VagaCompetenceDAO()

            List<String> competencias = vaga.competencias ?: []

            competencias.each { String nomeCompetencia ->
                Map competencia = competenceDAO.buscarOuCriarCompetencia(
                        connection,
                        nomeCompetencia
                )

                boolean vinculada =
                        vagaCompetenciaDAO.adicionarCompetenciaAVaga(
                                connection,
                                idVaga,
                                competencia.id as int
                        )

                if (!vinculada) {
                    throw new SQLException(
                            "Não foi possível vincular '${nomeCompetencia}' à vaga"
                    )
                }
            }

            connection.commit()
            sucesso = true
            println("Vaga adicionada com sucesso!")

        } catch (Exception e) {
            println("Erro ao adicionar vaga: ${e.message}")

            if (connection != null) {
                connection.rollback()
            }
        } finally {
            if (resultSet != null) resultSet.close()
            if (statement != null) statement.close()
            if (connection != null) connection.close()
        }

        return sucesso
    }

    boolean atualizarVaga(Map vaga) {
        Connection connection = null
        PreparedStatement statement = null
        boolean sucesso = false

        try {
            connection = ConexaoDB.getConnection()
            connection.setAutoCommit(false)

            statement = connection.prepareStatement(
                    """
                        UPDATE "Vaga"
                        SET id_empresa = ?, titulo = ?, descricao = ?, local_vaga = ?
                        WHERE id = ?
                    """
            )
            statement.setInt(1, vaga.idEmpresa as int)
            statement.setString(2, vaga.titulo)
            statement.setString(3, vaga.descricao)
            statement.setString(4, vaga.localVaga)
            statement.setInt(5, vaga.id as int)

            if (statement.executeUpdate() != 1) {
                throw new SQLException("Vaga não encontrada")
            }

            VagaCompetenceDAO vagaCompetenceDAO = new VagaCompetenceDAO()
            CompetenceDAO competenceDAO = new CompetenceDAO()

            vagaCompetenceDAO.removerCompetenciasDaVaga(
                    connection,
                    vaga.id as int
            )

            List<String> competencias = vaga.competencias ?: []
            competencias.each { String nomeCompetencia ->
                Map competencia = competenceDAO.buscarOuCriarCompetencia(
                        connection,
                        nomeCompetencia
                )

                if (!vagaCompetenceDAO.adicionarCompetenciaAVaga(
                        connection,
                        vaga.id as int,
                        competencia.id as int
                )) {
                    throw new SQLException(
                            "Não foi possível vincular '${nomeCompetencia}' à vaga"
                    )
                }
            }

            connection.commit()
            sucesso = true
            println("Vaga atualizada com sucesso!")
        } catch (Exception e) {
            println("Erro ao atualizar vaga: ${e.message}")
            if (connection != null) connection.rollback()
        } finally {
            if (statement != null) statement.close()
            if (connection != null) connection.close()
        }

        return sucesso
    }

    boolean removerVaga(int idVaga) {
        Connection connection = null
        boolean sucesso = false

        try {
            connection = ConexaoDB.getConnection()
            connection.setAutoCommit(false)

            List<String> comandos = [
                    'DELETE FROM "Curtida" WHERE id_vaga = ?',
                    'DELETE FROM "VagaCompetencia" WHERE id_vaga = ?',
                    'DELETE FROM "Vaga" WHERE id = ?'
            ]

            comandos.eachWithIndex { String sql, int indice ->
                PreparedStatement statement = connection.prepareStatement(sql)
                try {
                    statement.setInt(1, idVaga)
                    int linhasAfetadas = statement.executeUpdate()
                    if (indice == 2 && linhasAfetadas != 1) {
                        throw new SQLException("Vaga não encontrada")
                    }
                } finally {
                    statement.close()
                }
            }

            connection.commit()
            sucesso = true
            println("Vaga removida com sucesso!")
        } catch (Exception e) {
            println("Erro ao remover vaga: ${e.message}")
            if (connection != null) connection.rollback()
        } finally {
            if (connection != null) connection.close()
        }

        return sucesso
    }
}
