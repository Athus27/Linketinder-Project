package org.linketinder.dao.curtida

import org.linketinder.dao.ConexaoDB

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException

class CurtidaDAO {

    Map candidatoCurtirVaga(int idCandidato, int idVaga) {
        return registrarCurtida(idCandidato, idVaga, true)
    }

    Map empresaCurtirCandidato(int idEmpresa, int idVaga, int idCandidato) {
        Connection connection = null
        PreparedStatement statement = null
        ResultSet resultSet = null

        try {
            connection = ConexaoDB.getConnection()
            statement = connection.prepareStatement(
                    'SELECT 1 FROM "Vaga" WHERE id = ? AND id_empresa = ?'
            )
            statement.setInt(1, idVaga)
            statement.setInt(2, idEmpresa)
            resultSet = statement.executeQuery()

            if (!resultSet.next()) {
                return [sucesso: false, mensagem: "A vaga não pertence à empresa selecionada."]
            }
        } catch (Exception e) {
            return [sucesso: false, mensagem: e.message]
        } finally {
            if (resultSet != null) resultSet.close()
            if (statement != null) statement.close()
            if (connection != null) connection.close()
        }

        return registrarCurtida(idCandidato, idVaga, false)
    }

    private Map registrarCurtida(int idCandidato, int idVaga, boolean curtidaDoCandidato) {
        Connection connection = null
        PreparedStatement selectStatement = null
        PreparedStatement saveStatement = null
        ResultSet resultSet = null

        try {
            connection = ConexaoDB.getConnection()
            connection.setAutoCommit(false)

            selectStatement = connection.prepareStatement(
                    """
                        SELECT curtida_candidato, curtida_empresa, data_match
                        FROM "Curtida"
                        WHERE id_candidato = ? AND id_vaga = ?
                        FOR UPDATE
                    """
            )
            selectStatement.setInt(1, idCandidato)
            selectStatement.setInt(2, idVaga)
            resultSet = selectStatement.executeQuery()

            if (!resultSet.next()) {
                resultSet.close()
                resultSet = null

                saveStatement = connection.prepareStatement(
                        """
                            INSERT INTO "Curtida"
                                (id_candidato, id_vaga, curtida_candidato, curtida_empresa)
                            VALUES (?, ?, ?, ?)
                        """
                )
                saveStatement.setInt(1, idCandidato)
                saveStatement.setInt(2, idVaga)
                saveStatement.setBoolean(3, curtidaDoCandidato)
                saveStatement.setBoolean(4, !curtidaDoCandidato)

                if (saveStatement.executeUpdate() != 1) {
                    throw new SQLException("Não foi possível registrar a curtida")
                }

                connection.commit()
                return [sucesso: true, jaCurtiu: false, deuMatch: false]
            }

            boolean candidatoJaCurtiu = resultSet.getBoolean("curtida_candidato")
            boolean empresaJaCurtiu = resultSet.getBoolean("curtida_empresa")
            boolean jaCurtiu = curtidaDoCandidato ? candidatoJaCurtiu : empresaJaCurtiu

            if (jaCurtiu) {
                connection.commit()
                return [
                        sucesso : true,
                        jaCurtiu: true,
                        deuMatch: candidatoJaCurtiu && empresaJaCurtiu
                ]
            }

            resultSet.close()
            resultSet = null

            String coluna = curtidaDoCandidato ? "curtida_candidato" : "curtida_empresa"
            boolean deuMatch = curtidaDoCandidato ? empresaJaCurtiu : candidatoJaCurtiu
            saveStatement = connection.prepareStatement(
                    """
                        UPDATE "Curtida"
                        SET ${coluna} = true,
                            data_match = CASE
                                WHEN ? THEN COALESCE(data_match, CURRENT_TIMESTAMP)
                                ELSE data_match
                            END
                        WHERE id_candidato = ? AND id_vaga = ?
                    """
            )
            saveStatement.setBoolean(1, deuMatch)
            saveStatement.setInt(2, idCandidato)
            saveStatement.setInt(3, idVaga)

            if (saveStatement.executeUpdate() != 1) {
                throw new SQLException("Não foi possível atualizar a curtida")
            }

            connection.commit()
            return [sucesso: true, jaCurtiu: false, deuMatch: deuMatch]
        } catch (Exception e) {
            if (connection != null) connection.rollback()
            return [sucesso: false, mensagem: e.message]
        } finally {
            if (resultSet != null) resultSet.close()
            if (selectStatement != null) selectStatement.close()
            if (saveStatement != null) saveStatement.close()
            if (connection != null) connection.close()
        }
    }
}
