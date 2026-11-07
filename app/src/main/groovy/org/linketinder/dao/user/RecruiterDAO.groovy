package org.linketinder.dao.user

import org.linketinder.dao.connection.ConnectionFactory
import org.linketinder.dao.connection.DatabaseConnectionFactory

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException

class RecruiterDAO {
    private final ConnectionFactory connectionFactory

    RecruiterDAO() {
        this(null)
    }

    RecruiterDAO(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory
    }

    List<Map> listar(int idEmpresa) {
        withConnection { Connection connection ->
            PreparedStatement statement = connection.prepareStatement(
                    '''
                        SELECT r.id_recrutador AS id, u.nome, u.email,
                               r.area, r.descricao, r.id_empresa, r.ativo
                        FROM "Recruiter" r
                        JOIN "User" u ON u.id = r.id_recrutador
                        WHERE r.id_empresa = ?
                        ORDER BY r.id_recrutador
                    '''
            )
            statement.setInt(1, idEmpresa)
            ResultSet resultSet = statement.executeQuery()
            try {
                List<Map> result = []
                while (resultSet.next()) {
                    result << [
                            id: resultSet.getInt("id"),
                            nome: resultSet.getString("nome"),
                            email: resultSet.getString("email"),
                            area: resultSet.getString("area"),
                            descricao: resultSet.getString("descricao"),
                            idEmpresa: resultSet.getInt("id_empresa"),
                            ativo: resultSet.getBoolean("ativo")
                    ]
                }
                return result
            } finally {
                resultSet.close()
                statement.close()
            }
        }
    }

    Map criar(Map dados, int idEmpresa) {
        Connection connection = connection()
        try {
            connection.autoCommit = false
            int id
            PreparedStatement userStatement = connection.prepareStatement(
                    '''
                        INSERT INTO "User" (nome, email, "país", cep, senha)
                        VALUES (?, lower(trim(?)), NULL, NULL, crypt(?, gen_salt('bf')))
                        RETURNING id
                    '''
            )
            try {
                userStatement.setString(1, dados.nome)
                userStatement.setString(2, dados.email)
                userStatement.setString(3, dados.senha)
                ResultSet resultSet = userStatement.executeQuery()
                try {
                    if (!resultSet.next()) {
                        throw new SQLException("Cadastro nao criado")
                    }
                    id = resultSet.getInt("id")
                } finally {
                    resultSet.close()
                }
            } finally {
                userStatement.close()
            }

            PreparedStatement recruiterStatement = connection.prepareStatement(
                    '''
                        INSERT INTO "Recruiter"
                            (id_recrutador, id_empresa, area, descricao, cpf, data_nascimento)
                        VALUES (?, ?, ?, ?, ?, ?)
                    '''
            )
            try {
                recruiterStatement.setInt(1, id)
                recruiterStatement.setInt(2, idEmpresa)
                recruiterStatement.setString(3, dados.area)
                recruiterStatement.setString(4, dados.descricao)
                recruiterStatement.setString(5, dados.cpf)
                recruiterStatement.setDate(6, java.sql.Date.valueOf(dados.dataNascimento))
                if (recruiterStatement.executeUpdate() != 1) {
                    throw new SQLException("Cadastro nao criado")
                }
            } finally {
                recruiterStatement.close()
            }
            connection.commit()
            return [
                    id: id,
                    nome: dados.nome,
                    email: dados.email.toLowerCase().trim(),
                    area: dados.area,
                    descricao: dados.descricao,
                    idEmpresa: idEmpresa,
                    ativo: true
            ]
        } catch (SQLException exception) {
            connection.rollback()
            if (exception.SQLState == "23505") {
                throw new SQLException("Conflito de cadastro", "23505")
            }
            throw exception
        } catch (Exception exception) {
            connection.rollback()
            throw exception
        } finally {
            connection.close()
        }
    }

    boolean desativar(int idRecrutador, int idEmpresa) {
        withConnection { Connection connection ->
            PreparedStatement statement = connection.prepareStatement(
                    'UPDATE "Recruiter" SET ativo = false WHERE id_recrutador = ? AND id_empresa = ?'
            )
            try {
                statement.setInt(1, idRecrutador)
                statement.setInt(2, idEmpresa)
                return statement.executeUpdate() == 1
            } finally {
                statement.close()
            }
        }
    }

    private Connection connection() {
        ConnectionFactory factory = connectionFactory ?:
                DatabaseConnectionFactory.getInstance().getFactoryDatabaseConnection()
        factory.getConnection()
    }

    private <T> T withConnection(Closure<T> operation) {
        Connection connection = connection()
        try {
            return operation.call(connection)
        } finally {
            connection.close()
        }
    }
}
