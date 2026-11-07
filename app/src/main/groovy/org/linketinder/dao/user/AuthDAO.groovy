package org.linketinder.dao.user

import org.linketinder.auth.AuthPrincipal
import org.linketinder.dao.connection.ConnectionFactory
import org.linketinder.dao.connection.DatabaseConnectionFactory

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet

class AuthDAO {
    private final ConnectionFactory connectionFactory

    AuthDAO() {
        this(null)
    }

    AuthDAO(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory
    }

    Map autenticar(String email, String senha) {
        withConnection { Connection connection ->
            PreparedStatement statement = connection.prepareStatement(
                    '''
                        SELECT usuario.id AS id, usuario.nome AS nome,
                               CASE
                                   WHEN recruiter.id_recrutador IS NOT NULL THEN 'recruiter'
                                   WHEN candidate.id_candidato IS NOT NULL THEN 'candidate'
                                   WHEN company.id_empresa IS NOT NULL THEN 'company'
                               END AS tipo,
                               recruiter.id_empresa AS id_empresa
                        FROM "User" usuario
                        LEFT JOIN "Recruiter" recruiter
                            ON recruiter.id_recrutador = usuario.id
                           AND recruiter.ativo = true
                        LEFT JOIN "Candidate" candidate
                            ON candidate.id_candidato = usuario.id
                        LEFT JOIN "Company" company
                            ON company.id_empresa = usuario.id
                        WHERE lower(trim(usuario.email)) = lower(trim(?))
                          AND usuario.senha IS NOT NULL
                          AND crypt(?, usuario.senha) = usuario.senha
                          AND (
                              candidate.id_candidato IS NOT NULL
                              OR company.id_empresa IS NOT NULL
                              OR recruiter.id_recrutador IS NOT NULL
                          )
                    '''
            )
            statement.setString(1, email)
            statement.setString(2, senha)
            ResultSet resultSet = statement.executeQuery()
            try {
                if (!resultSet.next()) {
                    return null
                }
                String tipo = resultSet.getString("tipo")
                if (!tipo) {
                    return null
                }
                Map response = [
                        id: resultSet.getInt("id"),
                        nome: resultSet.getString("nome"),
                        tipo: tipo
                ]
                Integer idEmpresa = resultSet.getObject("id_empresa") as Integer
                if (tipo == "recruiter") {
                    response.idEmpresa = idEmpresa
                } else if (tipo == "company") {
                    response.idEmpresa = resultSet.getInt("id")
                }
                return response
            } finally {
                resultSet.close()
                statement.close()
            }
        }
    }

    AuthPrincipal validarPrincipal(AuthPrincipal principal) {
        if (principal == null) {
            return null
        }
        withConnection { Connection connection ->
            PreparedStatement statement = connection.prepareStatement(
                    '''
                        SELECT u.id, u.nome,
                               CASE
                                   WHEN r.id_recrutador IS NOT NULL THEN 'recruiter'
                                   WHEN c.id_candidato IS NOT NULL THEN 'candidate'
                                   WHEN e.id_empresa IS NOT NULL THEN 'company'
                               END AS tipo,
                               r.id_empresa
                        FROM "User" u
                        LEFT JOIN "Recruiter" r
                          ON r.id_recrutador = u.id AND r.ativo = true
                        LEFT JOIN "Candidate" c ON c.id_candidato = u.id
                        LEFT JOIN "Company" e ON e.id_empresa = u.id
                        WHERE u.id = ?
                    '''
            )
            statement.setInt(1, principal.id)
            ResultSet resultSet = statement.executeQuery()
            try {
                if (!resultSet.next()) {
                    return null
                }
                String tipo = resultSet.getString("tipo")
                Integer empresa = tipo == "recruiter"
                        ? resultSet.getObject("id_empresa") as Integer
                        : (tipo == "company" ? resultSet.getInt("id") : null)
                if (tipo != principal.tipo || empresa != principal.idEmpresa) {
                    return null
                }
                return new AuthPrincipal(
                        resultSet.getInt("id"),
                        resultSet.getString("nome"),
                        tipo,
                        empresa
                )
            } finally {
                resultSet.close()
                statement.close()
            }
        }
    }

    private <T> T withConnection(Closure<T> operation) {
        ConnectionFactory factory = connectionFactory ?:
                DatabaseConnectionFactory.getInstance().getFactoryDatabaseConnection()
        Connection connection = factory.getConnection()
        try {
            return operation.call(connection)
        } finally {
            connection.close()
        }
    }
}
