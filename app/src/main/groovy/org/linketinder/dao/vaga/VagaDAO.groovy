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
}
