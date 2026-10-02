package org.linketinder.dao.user

import org.linketinder.dao.ConexaoDB

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet

class CompanyDAO {
    boolean adicionarEmpresa(Map empresa) {
        Connection connection = null
        PreparedStatement statement = null
        boolean sucesso = false

        try {
            connection = ConexaoDB.getConnection()
            connection.setAutoCommit(false)

            // Inserindo dados na tabela User
            statement = connection.prepareStatement(
                    """
                        INSERT INTO "User" (nome, email, "país", cep,senha)
                        VALUES (?, ?, ?, ?,crypt(?, gen_salt('bf')))
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
            println("Erro ao adicionar empresa: ${e.message}")

            if (connection != null) {
                connection.rollback()
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
            connection = ConexaoDB.getConnection()
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
            println("Erro ao listar empresas: ${e.message}")
        } finally {
            try {
                if (resultSet != null) resultSet.close()
                if (statement != null) statement.close()
                if (connection != null) connection.close()
            } catch (Exception e) {
                println("Erro ao fechar recursos: ${e.message}")
            }
        }
        return [empresas: empresas]
    }

    boolean atualizarEmpresa(Map empresa) {
        Connection connection = null
        PreparedStatement statement = null
        boolean sucesso = false

        try {
            connection = ConexaoDB.getConnection()
            connection.setAutoCommit(false)

            statement = connection.prepareStatement(
                    """
                        UPDATE "User"
                        SET nome = ?, email = ?, "país" = ?, cep = ?,
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
            println("Erro ao atualizar empresa: ${e.message}")
            if (connection != null) connection.rollback()
        } finally {
            if (statement != null) statement.close()
            if (connection != null) connection.close()
        }

        return sucesso
    }

}
