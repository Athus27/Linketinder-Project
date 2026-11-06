package org.linketinder.dao.vaga


import org.linketinder.dao.competence.CompetenceDAO
import org.linketinder.dao.competence.VagaCompetenceDAO
import org.linketinder.dao.connection.DatabaseConnectionFactory
import org.linketinder.dao.connection.PostgresConnectionFactory
import org.linketinder.dao.connection.ConnectionFactory

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException

class VagaDAO {
    private final ConnectionFactory connectionFactory

    VagaDAO() {
        this(null)
    }

    VagaDAO(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory
    }

    private Connection openConnection() {
        (connectionFactory ?:
                DatabaseConnectionFactory.getInstance().getFactoryDatabaseConnection()).getConnection()
    }

    List<Map> listarPublicas(Integer idEmpresa = null) {
        Connection connection = openConnection()
        try {
            String sql = '''
                SELECT id, id_empresa, titulo, descricao, local_vaga
                FROM "Vaga"
            ''' + (idEmpresa == null ? '' : ' WHERE id_empresa = ?') + ' ORDER BY id'
            PreparedStatement statement = connection.prepareStatement(sql)
            if (idEmpresa != null) statement.setInt(1, idEmpresa)
            ResultSet rs = statement.executeQuery()
            try {
                VagaCompetenceDAO competenceDAO = new VagaCompetenceDAO(connectionFactory)
                List<Map> result = []
                while (rs.next()) {
                    result << [id: rs.getInt("id"), idEmpresa: rs.getInt("id_empresa"),
                               titulo: rs.getString("titulo"), descricao: rs.getString("descricao"),
                               competencias: competenceDAO.listarCompetenciasDaVaga(rs.getInt("id"))]
                }
                return result
            } finally {
                rs.close()
                statement.close()
            }
        } finally {
            connection.close()
        }
    }

    boolean adicionarVaga(Map vaga, int idEmpresa) {
        Map data = new LinkedHashMap(vaga)
        data.idEmpresa = idEmpresa
        adicionarVaga(data)
    }

    Map listarVagas() {
        Connection connection = null
        PreparedStatement statement = null
        ResultSet resultSet = null
        List<Map> vagas = []

        try {
            connection = openConnection()
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
            VagaCompetenceDAO vagaCompetenceDAO = connectionFactory ?
                    new VagaCompetenceDAO(connectionFactory) : new VagaCompetenceDAO()

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
            println("Erro ao listar vagas (${e.class.simpleName})")
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
            connection = openConnection()
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

            CompetenceDAO competenceDAO = connectionFactory ?
                    new CompetenceDAO(connectionFactory) : new CompetenceDAO()
            VagaCompetenceDAO vagaCompetenciaDAO = connectionFactory ?
                    new VagaCompetenceDAO(connectionFactory) : new VagaCompetenceDAO()

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
            println("Erro ao adicionar vaga (${e.class.simpleName})")

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
            connection = openConnection()
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

            VagaCompetenceDAO vagaCompetenceDAO = connectionFactory ?
                    new VagaCompetenceDAO(connectionFactory) : new VagaCompetenceDAO()
            CompetenceDAO competenceDAO = connectionFactory ?
                    new CompetenceDAO(connectionFactory) : new CompetenceDAO()

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
            println("Erro ao atualizar vaga (${e.class.simpleName})")
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
            connection = openConnection()
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
            println("Erro ao remover vaga (${e.class.simpleName})")
            if (connection != null) connection.rollback()
        } finally {
            if (connection != null) connection.close()
        }

        return sucesso
    }
}
