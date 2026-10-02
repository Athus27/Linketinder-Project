package org.linketinder.dao

import org.linketinder.dao.competence.CandidateCompetenceDAO
import org.linketinder.dao.user.CandidateDAO
import spock.lang.Specification

//Spy

import java.sql.Connection
import java.sql.Date
import java.sql.ResultSet
import java.sql.PreparedStatement

class CandidateDAOTest extends Specification {
    /**
     * teste de integração para verificar se a classe CandidateDAO é instanciada corretamente e se a conexão com o banco de dados é estabelecida com sucesso.
     */
    def "deve criar uma instancia de CandidateDAO com sucesso"() {
        given: "uma classe CandidateDAO"
        CandidateDAO candidateDAO = new CandidateDAO()

        when: "testamos a conexão"
        boolean conexaoEstabelecida = candidateDAO.testarConexao()

        then: "a instancia deve ser criada e nao ser nula"
        conexaoEstabelecida == true
    }

    def "deve listar candidatos com sucesso"() {
        given: "as dependencias simuladas do banco"
        def connection = Mock(Connection)
        def statement = Mock(PreparedStatement)
        def resultSet = Mock(ResultSet)
        def candidateCompetenceDAO = new CandidateCompetenceDAO()
        candidateCompetenceDAO.metaClass.listarCompetenciasDoCandidato = { int idCandidato ->
            [(1): ["Groovy", "SQL"], (2): ["Java", "PostgreSQL"]][idCandidato]
        }

        GroovyMock(ConexaoDB, global: true)
        GroovyMock(CandidateCompetenceDAO, global: true)

        ConexaoDB.getConnection() >> connection
        new CandidateCompetenceDAO() >> candidateCompetenceDAO

        connection.prepareStatement(_) >> statement
        statement.executeQuery() >> resultSet

        resultSet.next() >>> [true, true, false]

        resultSet.getInt("id") >>> [1, 2]
        resultSet.getString("nome") >>> ["Fulano", "Ciclano"]
        resultSet.getString("sobrenome") >>> ["da Silva", "de Souza"]
        resultSet.getDate("data_nascimento") >>> [Date.valueOf("1990-01-15"), Date.valueOf("1995-06-20")]
        resultSet.getString("cpf") >>> ["111.111.111-11", "222.222.222-22"]
        resultSet.getString("descricao") >>> ["Desenvolvedor backend", "Desenvolvedor frontend"]
        resultSet.getString("email") >>>
                ["fulano@email.com", "ciclano@email.com"]
        resultSet.getString("formacao") >>> ["Ciência da Computação", "Sistemas de Informação"]
        resultSet.getString("pais") >>> ["Brasil", "Brasil"]
        resultSet.getString("cep") >>> ["01010-000", "20020-000"]

        and: "um DAO"
        CandidateDAO candidateDAO = new CandidateDAO()

        when: "listamos os candidatos"
        Map resultado = candidateDAO.listarCandidatos()

        then: "o DAO transforma o retorno do ResultSet na lista esperada"
        resultado.candidatos == [
                [id          : 1, nome: "Fulano", sobrenome: "da Silva", dataNascimento: Date.valueOf("1990-01-15"),
                 cpf         : "111.111.111-11", descricao: "Desenvolvedor backend", email: "fulano@email.com",
                 formacao    : "Ciência da Computação", pais: "Brasil", cep: "01010-000",
                 competencias: ["Groovy", "SQL"]],
                [id          : 2, nome: "Ciclano", sobrenome: "de Souza", dataNascimento: Date.valueOf("1995-06-20"),
                 cpf         : "222.222.222-22", descricao: "Desenvolvedor frontend", email: "ciclano@email.com",
                 formacao    : "Sistemas de Informação", pais: "Brasil", cep: "20020-000",
                 competencias: ["Java", "PostgreSQL"]]
        ]

        and: "os recursos sao fechados"
        1 * resultSet.close()
        1 * statement.close()
        1 * connection.close()
    }

    def "deve gerar feed anônimo de candidatos"() {
        given:
        CandidateDAO candidateDAO = new CandidateDAO()

        candidateDAO.metaClass.listarCandidatos = {
            [
                    candidatos: [[
                                         id          : 1,
                                         nome        : "Fulano",
                                         cpf         : "111.111.111-11",
                                         email       : "fulano@email.com",
                                         descricao   : "Desenvolvedor backend",
                                         formacao    : "Ciência da Computação",
                                         competencias: ["Groovy", "SQL"]
                                 ]]
            ]
        }

        when:
        List<Map> resultado = candidateDAO.feedCandidatos()

        then:
        resultado == [[
                              descricao   : "Desenvolvedor backend",
                              formacao    : "Ciência da Computação",
                              competencias: ["Groovy", "SQL"]
                      ]]

        and:
        !resultado.first().containsKey("nome")
        !resultado.first().containsKey("cpf")
        !resultado.first().containsKey("email")
    }

    def "deve adicionar candidato e relacionar competencia existente"() {
        given: "uma transacao simulada para cadastrar candidato e competencia"
        Connection connection = Mock()

        PreparedStatement userStatement = Mock()
        PreparedStatement candidateStatement = Mock()
        PreparedStatement selectCompetenceStatement = Mock()
        PreparedStatement linkStatement = Mock()

        ResultSet userResultSet = Mock()
        ResultSet competenceResultSet = Mock()

        GroovyMock(ConexaoDB, global: true)
        ConexaoDB.getConnection() >> connection

        connection.prepareStatement(_) >>> [
                userStatement,
                candidateStatement,
                selectCompetenceStatement,
                linkStatement
        ]

        userStatement.executeQuery() >> userResultSet
        userResultSet.next() >> true
        userResultSet.getInt("id") >> 11

        candidateStatement.executeUpdate() >> 1

        selectCompetenceStatement.executeQuery() >> competenceResultSet
        competenceResultSet.next() >> true
        competenceResultSet.getInt("id") >> 2
        competenceResultSet.getString("competencia") >> "Groovy"

        linkStatement.executeUpdate() >> 1

        Map candidato = [
                nome          : "João",
                sobrenome     : "Silva",
                dataNascimento: "27/05/2004",
                cpf           : "123.456.789-00",
                descricao     : "Desenvolvedor backend",
                email         : "joao@gmail.com",
                formacao      : "Engenharia",
                pais          : "Brasil",
                cep           : "12345-000",
                senha         : "senha123",
                competencias  : ["Groovy"]
        ]

        CandidateDAO candidateDAO = new CandidateDAO()

        when: "o candidato é cadastrado"
        boolean resultado = candidateDAO.adicionarCandidato(candidato)

        then: "o cadastro e o vínculo são confirmados"
        resultado
        1 * candidateStatement.setInt(1, 11)
        1 * candidateStatement.setDate(3, java.sql.Date.valueOf("2004-05-27"))
        1 * selectCompetenceStatement.setString(1, "Groovy")
        1 * linkStatement.setInt(1, 11)
        1 * linkStatement.setInt(2, 2)
        1 * connection.commit()
        0 * connection.rollback()

        and: "os recursos são fechados sem o DAO auxiliar fechar a conexão"
        1 * userResultSet.close()
        1 * competenceResultSet.close()
        1 * userStatement.close()
        1 * candidateStatement.close()
        1 * selectCompetenceStatement.close()
        1 * linkStatement.close()
        1 * connection.close()
    }
}
