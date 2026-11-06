package org.linketinder.dao.user


import org.linketinder.dao.competence.CandidateCompetenceDAO
import org.linketinder.dao.competence.CompetenceDAO
import org.linketinder.dao.connection.DatabaseConnectionFactory
import org.linketinder.dao.connection.PostgresConnectionFactory
import org.linketinder.dao.connection.ConnectionFactory
import org.linketinder.auth.AuthPrincipal

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.time.LocalDate
import java.time.format.DateTimeFormatter


class CandidateDAO {
    private final ConnectionFactory connectionFactory

    CandidateDAO() {
        this(null)
    }

    CandidateDAO(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory
    }

    private Connection openConnection() {
        (connectionFactory ?:
                DatabaseConnectionFactory.getInstance().getFactoryDatabaseConnection()).getConnection()
    }

    List<Map> listarPublicos(Integer idEmpresa = null) {
        Connection connection = openConnection()
        try {
            String match = idEmpresa == null ? "FALSE" : '''
                EXISTS (
                  SELECT 1 FROM "Curtida" l
                  JOIN "Vaga" v ON v.id = l.id_vaga
                  WHERE l.id_candidato = c.id_candidato
                    AND l.curtida_candidato AND l.curtida_empresa
                    AND v.id_empresa = ?
                )
            '''
            PreparedStatement statement = connection.prepareStatement("""
                SELECT c.id_candidato AS id, c.formacao, c.descricao, u.nome,
                       c.sobrenome, u.email, ${match} AS matched
                FROM "Candidate" c JOIN "User" u ON u.id = c.id_candidato
                ORDER BY c.id_candidato
            """)
            if (idEmpresa != null) statement.setInt(1, idEmpresa)
            ResultSet rs = statement.executeQuery()
            try {
                CandidateCompetenceDAO competenceDAO = new CandidateCompetenceDAO(connectionFactory)
                List<Map> result = []
                while (rs.next()) {
                    boolean matched = rs.getBoolean("matched")
                    Map item = [id: rs.getInt("id"),
                                competencias: competenceDAO.listarCompetenciasDoCandidato(rs.getInt("id")),
                                formacao: rs.getString("formacao")]
                    if (matched) {
                        item.putAll(nome: rs.getString("nome"), sobrenome: rs.getString("sobrenome"),
                                email: rs.getString("email"), descricao: rs.getString("descricao"))
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

    Map buscarPublico(int idCandidato, Integer idEmpresa = null) {
        listarPublicos(idEmpresa).find { it.id == idCandidato }
    }

    Map listarCandidatos() {
        Connection connection = null
        PreparedStatement statement = null
        ResultSet resultSet = null

        def candidatos = []

        try {
            connection = openConnection()
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

            def competenceDAO = connectionFactory ?
                    new CandidateCompetenceDAO(connectionFactory) : new CandidateCompetenceDAO()

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
            println("Erro ao listar candidatos (${e.class.simpleName})")
        } finally {
            try {
                if (resultSet != null) resultSet.close()
                if (statement != null) statement.close()
                if (connection != null) connection.close()
            } catch (Exception e) {
                println("Erro ao fechar recursos (${e.class.simpleName})")
            }
        }
        return [candidatos: candidatos]
    }

    List<Map> feedCandidatos() {
        listarPublicos()
    }

    Map buscarPorId(int idCandidato) {
        Connection connection = null
        PreparedStatement statement = null
        ResultSet resultSet = null

        Map candidato = null

        try {
            connection = openConnection()
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
                        WHERE candidate.id_candidato = ?
                    """
            )
            statement.setInt(1, idCandidato)
            resultSet = statement.executeQuery()

            if (resultSet.next()) {
                CandidateCompetenceDAO competenceDAO = connectionFactory ?
                        new CandidateCompetenceDAO(connectionFactory) : new CandidateCompetenceDAO()
                List<String> competencias = competenceDAO.listarCompetenciasDoCandidato(idCandidato)

                candidato = [
                        id            : resultSet.getInt("id"),
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
            println("Erro ao buscar candidato por ID (${e.class.simpleName})")
        } finally {
            try {
                if (resultSet != null) resultSet.close()
                if (statement != null) statement.close()
                if (connection != null) connection.close()
            } catch (Exception e) {
                println("Erro ao fechar recursos (${e.class.simpleName})")
            }
        }
        return candidato
    }
    boolean adicionarCandidato(Map candidato) {
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
            statement.setString(1, candidato.nome)
            statement.setString(2, candidato.email)
            statement.setString(3, candidato.pais)
            statement.setString(4, candidato.cep)
            statement.setString(5, candidato.senha)

            def resultSet = statement.executeQuery()
            if (!resultSet.next()) {
                throw new SQLException("banco n retornou id")
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

            DateTimeFormatter formatoData =
                    DateTimeFormatter.ofPattern("dd/MM/yyyy")
            LocalDate dataNascimento =
                    LocalDate.parse(candidato.dataNascimento, formatoData)
            statement.setDate(3, java.sql.Date.valueOf(dataNascimento))

            statement.setString(4, candidato.cpf)
            statement.setString(5, candidato.descricao)
            statement.setString(6, candidato.formacao)

            int rowsAffected = statement.executeUpdate()
            sucesso = rowsAffected > 0

            if (sucesso) {
                CompetenceDAO competenceDAO = connectionFactory ?
                        new CompetenceDAO(connectionFactory) : new CompetenceDAO()
                CandidateCompetenceDAO candidateCompetenceDAO = connectionFactory ?
                        new CandidateCompetenceDAO(connectionFactory) : new CandidateCompetenceDAO()

                List<String> nomesCompetencias = candidato.competencias ?: []

                nomesCompetencias.each { String nomeCompetencia ->
                    Map competenciaSalva = competenceDAO.buscarOuCriarCompetencia(connection, nomeCompetencia)

                    boolean vinculada = candidateCompetenceDAO.adicionarCompetenciaAoCandidato(connection, userId,competenciaSalva.id as int)

                    if (!vinculada) {
                        throw new SQLException(
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
            boolean cadastroDuplicado = e instanceof SQLException && e.SQLState == "23505"
            if (cadastroDuplicado) {
                println("Cadastro do candidato rejeitado por conflito de dado único.")
            } else {
                println("Erro ao adicionar candidato (${e.class.simpleName})")
            }
            try {
                if (connection != null) {
                    connection.rollback()
                    println("Rolando de volta devido a erro.")
                }
            } catch (Exception rollbackEx) {
                println("Erro ao tentar rolar de volta (${rollbackEx.class.simpleName})")
            }
            if (cadastroDuplicado) {
                throw new SQLException("Cadastro já existente", "23505")
            }
        } finally {
            try {
                if (statement != null) statement.close()
                if (connection != null) connection.close()
            } catch (Exception e) {
                println("Erro ao fechar recursos (${e.class.simpleName})")
            }
        }
        return sucesso
    }

    boolean atualizarCandidato(Map candidato) {
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
            statement.setString(1, candidato.nome)
            statement.setString(2, candidato.email)
            statement.setString(3, candidato.pais)
            statement.setString(4, candidato.cep)
            statement.setString(5, candidato.senha)
            statement.setInt(6, candidato.id as int)

            if (statement.executeUpdate() != 1) {
                throw new SQLException("Candidato não encontrado")
            }

            statement.close()
            statement = connection.prepareStatement(
                    """
                        UPDATE "Candidate"
                        SET sobrenome = ?, data_nascimento = ?, cpf = ?,
                            descricao = ?, formacao = ?
                        WHERE id_candidato = ?
                    """
            )
            statement.setString(1, candidato.sobrenome)

            DateTimeFormatter formatoData =
                    DateTimeFormatter.ofPattern("dd/MM/yyyy")
            LocalDate dataNascimento =
                    LocalDate.parse(candidato.dataNascimento, formatoData)
            statement.setDate(2, java.sql.Date.valueOf(dataNascimento))

            statement.setString(3, candidato.cpf)
            statement.setString(4, candidato.descricao)
            statement.setString(5, candidato.formacao)
            statement.setInt(6, candidato.id as int)

            if (statement.executeUpdate() != 1) {
                throw new SQLException("Dados do candidato não encontrados")
            }

            CandidateCompetenceDAO candidateCompetenceDAO = connectionFactory ?
                    new CandidateCompetenceDAO(connectionFactory) : new CandidateCompetenceDAO()
            CompetenceDAO competenceDAO = connectionFactory ?
                    new CompetenceDAO(connectionFactory) : new CompetenceDAO()

            candidateCompetenceDAO.removerCompetenciasDoCandidato(
                    connection,
                    candidato.id as int
            )

            List<String> competencias = candidato.competencias ?: []
            competencias.each { String nomeCompetencia ->
                Map competencia = competenceDAO.buscarOuCriarCompetencia(
                        connection,
                        nomeCompetencia
                )

                if (!candidateCompetenceDAO.adicionarCompetenciaAoCandidato(
                        connection,
                        candidato.id as int,
                        competencia.id as int
                )) {
                    throw new SQLException(
                            "Não foi possível vincular '${nomeCompetencia}' ao candidato"
                    )
                }
            }

            connection.commit()
            sucesso = true
            println("Candidato atualizado com sucesso!")
        } catch (Exception e) {
            println("Erro ao atualizar candidato (${e.class.simpleName})")
            if (connection != null) connection.rollback()
        } finally {
            if (statement != null) statement.close()
            if (connection != null) connection.close()
        }

        return sucesso
    }

    boolean removerCandidato(int idCandidato) {
        Connection connection = null
        boolean sucesso = false

        try {
            connection = openConnection()
            connection.setAutoCommit(false)

            List<String> comandos = [
                    'DELETE FROM "Curtida" WHERE id_candidato = ?',
                    'DELETE FROM "CandidatoCompetencia" WHERE id_candidato = ?',
                    'DELETE FROM "Candidate" WHERE id_candidato = ?',
                    'DELETE FROM "User" WHERE id = ?'
            ]

            comandos.eachWithIndex { String sql, int indice ->
                PreparedStatement statement = connection.prepareStatement(sql)
                try {
                    statement.setInt(1, idCandidato)
                    int linhasAfetadas = statement.executeUpdate()
                    if (indice >= 2 && linhasAfetadas != 1) {
                        throw new SQLException("Candidato não encontrado")
                    }
                } finally {
                    statement.close()
                }
            }

            connection.commit()
            sucesso = true
            println("Candidato removido com sucesso!")
        } catch (Exception e) {
            println("Erro ao remover candidato (${e.class.simpleName})")
            if (connection != null) connection.rollback()
        } finally {
            if (connection != null) connection.close()
        }

        return sucesso
    }

}
