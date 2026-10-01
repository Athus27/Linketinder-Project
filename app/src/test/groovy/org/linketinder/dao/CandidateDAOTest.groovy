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
}
