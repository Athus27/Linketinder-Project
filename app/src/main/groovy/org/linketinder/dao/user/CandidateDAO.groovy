package org.linketinder.dao.user

import org.linketinder.dao.ConexaoDB

import org.linketinder.dao.competence.CandidateCompetenceDAO



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
                               competencias: competencias
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

}
