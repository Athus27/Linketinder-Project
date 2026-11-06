package org.linketinder.dao.user

import org.linketinder.dao.connection.DatabaseConnectionFactory
import org.linketinder.dao.connection.PostgresConnectionFactory
import org.linketinder.dao.connection.ConnectionFactory

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Types

class CompanyDAO {
    private final ConnectionFactory connectionFactory

    CompanyDAO() {
        this(null)
    }

    CompanyDAO(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory
    }

    private Connection openConnection() {
        (connectionFactory ?:
                DatabaseConnectionFactory.getInstance().getFactoryDatabaseConnection()).getConnection()
    }

    List<Map> listarPublicos(Integer idCandidato = null) {
        Connection connection = openConnection()
        try {
            PreparedStatement statement = connection.prepareStatement('''
                SELECT e.id_empresa AS id, u.nome, u.email,
                       EXISTS (
                         SELECT 1 FROM "Curtida" l
                         JOIN "Vaga" v ON v.id = l.id_vaga
                         WHERE v.id_empresa = e.id_empresa
                           AND l.id_candidato = ?
                           AND l.curtida_candidato AND l.curtida_empresa
                       ) AS matched
                FROM "Company" e JOIN "User" u ON u.id = e.id_empresa
                ORDER BY e.id_empresa
            ''')
            if (idCandidato == null) {
                statement.setNull(1, Types.INTEGER)
            } else {
                statement.setInt(1, idCandidato)
            }
            ResultSet rs = statement.executeQuery()
            try {
                List<Map> result = []
                while (rs.next()) {
                    Map item = [id: rs.getInt("id")]
                    if (rs.getBoolean("matched")) {
                        item.putAll(nome: rs.getString("nome"), email: rs.getString("email"))
                    }
                    result << item
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
    boolean adicionarEmpresa(Map empresa) {
        Connection connection = null
        PreparedStatement statement = null
        boolean sucesso = false

        try {
            connection = openConnection()
            connection.setAutoCommit(false)

            // Inserindo dados na tabela User
            statement = connection.prepareStatement(
                    """
                        INSERT INTO "User" (nome, email, "país", cep,senha)
                        VALUES (?, lower(trim(?)), ?, ?,crypt(?, gen_salt('bf')))
                        RETURNING id
                    """
            )
            statement.setString(1, empresa.nome)
            statement.setString(2, empresa.email)
            statement.setString(3, empresa.pais)
            statement.setString(4, empresa.cep)
            statement.setString(5, empresa.senha)

            def resultSet = statement.executeQuery()
            if (!resultSet.next()) {
                throw new java.sql.SQLException("banco n retornou id")
            }

            int userId = resultSet.getInt("id")

            resultSet.close()
            statement.close()

            statement = connection.prepareStatement(
                    """
                        INSERT INTO "Company"(id_empresa,cnpj,descricao)
                        VALUES (?,?,?)
                    """
            )
            statement.setInt(1, userId)
            statement.setString(2, empresa.cnpj)
            statement.setString(3, empresa.descricao)

            int linhasAfetadas = statement.executeUpdate()
            sucesso = linhasAfetadas == 1

            if (sucesso) {
                connection.commit()
                println("Empresa adicionada com sucesso!")
            } else {
                connection.rollback()
            }

        } catch (Exception e) {
            sucesso = false
            boolean cadastroDuplicado = e instanceof java.sql.SQLException && e.SQLState == "23505"
            if (cadastroDuplicado) {
                println("Cadastro da empresa rejeitado por conflito de dado único.")
            } else {
                println("Erro ao adicionar empresa (${e.class.simpleName})")
            }

            if (connection != null) {
                connection.rollback()
            }

            if (cadastroDuplicado) {
                throw new java.sql.SQLException("Cadastro já existente", "23505")
            }
        } finally {
            if (statement != null) statement.close()
            if (connection != null) connection.close()
        }

        return sucesso
    }

    Map listarEmpresas() {
        Connection connection = null
        PreparedStatement statement = null
        ResultSet resultSet = null

        def empresas = []
        try {
            connection = openConnection()
            statement = connection.prepareStatement(
                    """
                        SELECT
                            empresa.id_empresa AS id,
                            user_data.nome AS nome,
                            empresa.cnpj AS cnpj,
                            user_data.email AS email,
                            empresa.descricao AS descricao,
                            user_data."país" AS pais,
                            user_data.cep AS cep
                        FROM "Company" AS empresa
                        JOIN "User" AS user_data
                            ON user_data.id = empresa.id_empresa
                    """
            )
            resultSet = statement.executeQuery()

            while (resultSet.next()) {
                empresas << [
                        id       : resultSet.getInt("id"),
                        nome     : resultSet.getString("nome"),
                        cnpj     : resultSet.getString("cnpj"),
                        descricao: resultSet.getString("descricao"),
                        email    : resultSet.getString("email"),
                        pais     : resultSet.getString("pais"),
                        cep      : resultSet.getString("cep")
                ]
            }

        } catch (Exception e) {
            println("Erro ao listar empresas (${e.class.simpleName})")
        } finally {
            try {
                if (resultSet != null) resultSet.close()
                if (statement != null) statement.close()
                if (connection != null) connection.close()
            } catch (Exception e) {
                println("Erro ao fechar recursos (${e.class.simpleName})")
            }
        }
        return [empresas: empresas]
    }

    boolean atualizarEmpresa(Map empresa) {
        Connection connection = null
        PreparedStatement statement = null
        boolean sucesso = false

        try {
            connection = openConnection()
            connection.setAutoCommit(false)

            statement = connection.prepareStatement(
                    """
                        UPDATE "User"
                        SET nome = ?, email = lower(trim(?)), "país" = ?, cep = ?,
                            senha = crypt(?, gen_salt('bf'))
                        WHERE id = ?
                    """
            )
            statement.setString(1, empresa.nome)
            statement.setString(2, empresa.email)
            statement.setString(3, empresa.pais)
            statement.setString(4, empresa.cep)
            statement.setString(5, empresa.senha)
            statement.setInt(6, empresa.id as int)

            if (statement.executeUpdate() != 1) {
                throw new java.sql.SQLException("Empresa não encontrada")
            }

            statement.close()
            statement = connection.prepareStatement(
                    """
                        UPDATE "Company"
                        SET cnpj = ?, descricao = ?
                        WHERE id_empresa = ?
                    """
            )
            statement.setString(1, empresa.cnpj)
            statement.setString(2, empresa.descricao)
            statement.setInt(3, empresa.id as int)

            if (statement.executeUpdate() != 1) {
                throw new java.sql.SQLException("Dados da empresa não encontrados")
            }

            connection.commit()
            sucesso = true
            println("Empresa atualizada com sucesso!")
        } catch (Exception e) {
            println("Erro ao atualizar empresa (${e.class.simpleName})")
            if (connection != null) connection.rollback()
        } finally {
            if (statement != null) statement.close()
            if (connection != null) connection.close()
        }

        return sucesso
    }

    boolean removerEmpresa(int idEmpresa) {
        Connection connection = null
        boolean sucesso = false

        try {
            connection = openConnection()
            connection.setAutoCommit(false)

            List<String> comandos = [
                    'DELETE FROM "Curtida" WHERE id_vaga IN (SELECT id FROM "Vaga" WHERE id_empresa = ?)',
                    'DELETE FROM "VagaCompetencia" WHERE id_vaga IN (SELECT id FROM "Vaga" WHERE id_empresa = ?)',
                    'DELETE FROM "Vaga" WHERE id_empresa = ?',
                    'DELETE FROM "Company" WHERE id_empresa = ?',
                    'DELETE FROM "User" WHERE id = ?'
            ]

            comandos.eachWithIndex { String sql, int indice ->
                PreparedStatement statement = connection.prepareStatement(sql)
                try {
                    statement.setInt(1, idEmpresa)
                    int linhasAfetadas = statement.executeUpdate()
                    if (indice >= 3 && linhasAfetadas != 1) {
                        throw new java.sql.SQLException("Empresa não encontrada")
                    }
                } finally {
                    statement.close()
                }
            }

            connection.commit()
            sucesso = true
            println("Empresa removida com sucesso!")
        } catch (Exception e) {
            println("Erro ao remover empresa (${e.class.simpleName})")
            if (connection != null) connection.rollback()
        } finally {
            if (connection != null) connection.close()
        }

        return sucesso
    }

}
