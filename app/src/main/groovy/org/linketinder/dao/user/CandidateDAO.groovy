package org.linketinder.dao.user

import org.linketinder.dao.ConexaoDB

import org.linketinder.dao.competence.CandidateCompetenceDAO
import org.linketinder.dao.competence.CompetenceDAO


class CandidateDAO {
    boolean testarConexao() {
        def connectoin = ConexaoDB.getConnection()

        if (connectoin != null && !connectoin.isClosed()) {
            println("Conexão estabelecida com sucesso!")
            connectoin.close()
            return true
        } else {
            println("Falha ao estabelecer conexão.")
            return false
        }
    }

    Map listarCandidatos() {
        java.sql.Connection connection = null
        java.sql.PreparedStatement statement = null
        java.sql.ResultSet resultSet = null

        def candidatos = []


        try {
            connection = ConexaoDB.getConnection()
            statement = connection.prepareStatement(
                    """
                        SELECT
                            candidate.id_candidato AS id,
                            user_data.nome AS nome,
                            candidate.sobrenome AS sobrenome,
                            candidate.data_nascimento AS data_nascimento,
                            candidate.cpf AS cpf,
                            candidate.descricao AS descricao,
                            user_data.email AS email,
                            candidate.formacao AS formacao,
                            user_data."país" AS pais,
                            user_data.cep AS cep
                        FROM "Candidate" AS candidate
                        JOIN "User" AS user_data
                            ON user_data.id = candidate.id_candidato
                    """
            )
            resultSet = statement.executeQuery()

            def competenceDAO = new CandidateCompetenceDAO()

            while (resultSet.next()) {
                int idCandidato = resultSet.getInt("id")

                //Listando as competências do candidato usando o CandidateCompetenceDAO(MOSTRE O SQL)
                List<String> competencias = competenceDAO.listarCompetenciasDoCandidato(idCandidato)

                candidatos << [id            : idCandidato,
                               nome          : resultSet.getString("nome"),
                               sobrenome     : resultSet.getString("sobrenome"),
                               dataNascimento: resultSet.getDate("data_nascimento"),
                               cpf           : resultSet.getString("cpf"),
                               descricao     : resultSet.getString("descricao"),
                               email         : resultSet.getString("email"),
                               formacao      : resultSet.getString("formacao"),
                               pais          : resultSet.getString("pais"),
                               cep           : resultSet.getString("cep"),
                               competencias  : competencias
                ]
            }

        } catch (Exception e) {
            println("Erro ao listar candidatos: ${e.message}")
        } finally {
            try {
                if (resultSet != null) resultSet.close()
                if (statement != null) statement.close()
                if (connection != null) connection.close()
            } catch (Exception e) {
                println("Erro ao fechar recursos: ${e.message}")
            }
        }
        return [candidatos: candidatos]
    }

    List<Map> feedCandidatos() {
        List<Map> candidatos = listarCandidatos().candidatos

        return candidatos.collect { candidato ->
            [
                    descricao   : candidato.descricao,
                    formacao    : candidato.formacao,
                    competencias: candidato.competencias
            ]
        }
    }

    boolean adicionarCandidato(Map candidato) {
        java.sql.Connection connection = null
        java.sql.PreparedStatement statement = null
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
            statement.setString(1, candidato.nome)
            statement.setString(2, candidato.email)
            statement.setString(3, candidato.pais)
            statement.setString(4, candidato.cep)
            statement.setString(5, candidato.senha)

            def resultSet = statement.executeQuery()
            if (!resultSet.next()) {
                throw new java.sql.SQLException("banco n retornou id")
            }
            int userId = resultSet.getInt("id")

            resultSet.close()
            statement.close()

            // Inserindo dados na tabela Candidate
            statement = connection.prepareStatement(
                    """
                        INSERT INTO "Candidate" (id_candidato, sobrenome, data_nascimento, cpf, descricao, formacao)
                        VALUES (?, ?, ?, ?, ?, ?)
                    """
            )
            statement.setInt(1, userId)
            statement.setString(2, candidato.sobrenome)

            java.time.format.DateTimeFormatter formatoData =
                    java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")
            java.time.LocalDate dataNascimento =
                    java.time.LocalDate.parse(candidato.dataNascimento, formatoData)
            statement.setDate(3, java.sql.Date.valueOf(dataNascimento))

            statement.setString(4, candidato.cpf)
            statement.setString(5, candidato.descricao)
            statement.setString(6, candidato.formacao)

            int rowsAffected = statement.executeUpdate()
            sucesso = rowsAffected > 0

            if (sucesso) {
                CompetenceDAO competenceDAO = new CompetenceDAO()
                CandidateCompetenceDAO candidateCompetenceDAO =
                        new CandidateCompetenceDAO()

                List<String> nomesCompetencias = candidato.competencias ?: []

                nomesCompetencias.each { String nomeCompetencia ->
                    Map competenciaSalva = competenceDAO.buscarOuCriarCompetencia(connection, nomeCompetencia)

                    boolean vinculada = candidateCompetenceDAO.adicionarCompetenciaAoCandidato(connection, userId,competenciaSalva.id as int)

                    if (!vinculada) {
                        throw new java.sql.SQLException(
                                "Não foi possível vincular '${nomeCompetencia}' ao candidato"
                        )
                    }
                }

                connection.commit()
                println("Candidato adicionado com sucesso!")
            } else {
                connection.rollback()
                println("Falha ao adicionar candidato. Rolando de volta.")
            }

        } catch (Exception e) {
            println("Erro ao adicionar candidato: ${e.message}")
            try {
                if (connection != null) {
                    connection.rollback()
                    println("Rolando de volta devido a erro.")
                }
            } catch (Exception rollbackEx) {
                println("Erro ao tentar rolar de volta: ${rollbackEx.message}")
            }
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

}
