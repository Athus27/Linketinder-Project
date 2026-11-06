package org.linketinder.dao.user

import org.linketinder.dao.connection.DatabaseConnectionFactory

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet

class AuthDAO {
    Map autenticar(String email, String senha) {
        Connection connection = null
        PreparedStatement statement = null
        ResultSet resultSet = null

        try {
            connection = DatabaseConnectionFactory.getInstance()
                    .getFactoryDatabaseConnection().getConnection()
            statement = connection.prepareStatement(
                    '''
                        SELECT
                            usuario.id AS id,
                            usuario.nome AS nome,
                            CASE
                                WHEN EXISTS (
                                    SELECT 1 FROM "Candidate" candidato
                                    WHERE candidato.id_candidato = usuario.id
                                ) THEN 'candidate'
                                WHEN EXISTS (
                                    SELECT 1 FROM "Company" empresa
                                    WHERE empresa.id_empresa = usuario.id
                                ) THEN 'company'
                            END AS tipo
                        FROM "User" usuario
                        WHERE lower(trim(usuario.email)) = lower(trim(?))
                          AND usuario.senha IS NOT NULL
                          AND crypt(?, usuario.senha) = usuario.senha
                          AND (
                              EXISTS (SELECT 1 FROM "Candidate" c WHERE c.id_candidato = usuario.id)
                              OR EXISTS (SELECT 1 FROM "Company" e WHERE e.id_empresa = usuario.id)
                          )
                    '''
            )
            statement.setString(1, email)
            statement.setString(2, senha)
            resultSet = statement.executeQuery()

            if (!resultSet.next()) {
                return null
            }

            String tipo = resultSet.getString("tipo")
            if (!tipo) {
                return null
            }

            return [
                    id  : resultSet.getInt("id"),
                    nome: resultSet.getString("nome"),
                    tipo: tipo
            ]
        } finally {
            if (resultSet != null) resultSet.close()
            if (statement != null) statement.close()
            if (connection != null) connection.close()
        }
    }
}
