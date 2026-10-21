package org.linketinder.dao.user

import org.linketinder.dao.connection.ConnectionFactory
import org.linketinder.dao.connection.DatabaseConnectionFactory
import spock.lang.Specification

import java.sql.Connection
import java.sql.ResultSet
import java.sql.PreparedStatement

class CompanyDAOTest extends Specification {

    private DatabaseConnectionFactory gerenciadorMock
    private ConnectionFactory fabricaMock

    private CompanyDAO companyDAO
    private Connection connection
    private PreparedStatement statement
    private ResultSet resultSet

    private Map empresa

    def setup() {
        empresa = [
                id       : 1,
                nome     : "Empresa1",
                cnpj     : "12.345.678/0001-00",
                descricao: "desc1",
                email    : "empresa1@gmail.com",
                pais     : "Brasil",
                cep      : "39660-000"
        ]

        companyDAO = new CompanyDAO()
        connection = Mock(Connection)
        statement = Mock(PreparedStatement)
        resultSet = Mock(ResultSet)

        fabricaMock = Mock(ConnectionFactory)
        gerenciadorMock = Mock(DatabaseConnectionFactory)

        GroovyMock(DatabaseConnectionFactory, global: true)
        DatabaseConnectionFactory.getInstance() >> gerenciadorMock

        gerenciadorMock.getFactoryDatabaseConnection() >> fabricaMock
        fabricaMock.getConnection() >> connection
    }

    def "deve adicionar uma empresa com sucesso"() {
        given: "as dependencias simuladas do banco"


        def userStatement = Mock(PreparedStatement)
        def companyStatement = Mock(PreparedStatement)

        connection.prepareStatement(_) >>> [userStatement, companyStatement]

        and: "resultado do insert User"
        userStatement.executeQuery() >> resultSet
        resultSet.next() >> true

        resultSet.getInt("id") >> 1

        and: "resultado do insert Company"
        companyStatement.executeUpdate() >> 1

        when: "adicionamos uma empresa"
        boolean result = companyDAO.adicionarEmpresa(
                [
                        nome     : "Empresa Teste",
                        descricao: "Descrição da Empresa Teste",
                        email    : "empresateste@mail.com",
                        pais     : "Brasil",
                        cep      : "12345-678",
                        cnpj     : "12.345.678/0001-00",
                        senha    : "senhaSegura"
                ]
        )
        then: "o resultado deve ser verdadeiro"
        result == true
    }

    def "lista empresa deve retornar lista de empresas"() {
        given: "uma empresa no banco"
        connection.prepareStatement(_) >> statement
        statement.executeQuery() >> resultSet

        and: "resultado do select"
        resultSet.next() >>> [true, false]
        resultSet.getInt("id") >> empresa.id
        resultSet.getString("nome") >> empresa.nome
        resultSet.getString("cnpj") >> empresa.cnpj
        resultSet.getString("descricao") >> empresa.descricao
        resultSet.getString("email") >> empresa.email
        resultSet.getString("pais") >> empresa.pais
        resultSet.getString("cep") >> empresa.cep

        expect: "a lista de empresas deve conter a empresa adicionada"
        companyDAO.listarEmpresas().empresas == [empresa]
    }

    def "lança exceção dado uma consulta com falha, trata e fecha antes da respota"() {
        given: "uma consulta preparada"
        prepararConsultaEmpresa()
        statement.executeQuery() >> { throw new Exception("Falha na consulta") }

        when: "chamamos o método listarEmpresas"
        def resultado = companyDAO.listarEmpresas()

        then: "o DAO trata o erro sem repassá-lo"
        notThrown(Exception)

        and: "fecha os recursos que conseguiu obter"
        1 * statement.close()
        1 * connection.close()
        0 * resultSet.close()
    }

    def "atualizar empresa deve retornar verdadeiro quando a empresa é atualizada com sucesso"() {
        given: "uma empresa no banco"
        prepararConsultaEmpresa()
        statement.executeUpdate() >> 1

        when: "atualizamos a empresa"
        boolean result = companyDAO.atualizarEmpresa(empresa)

        then: "o resultado deve ser verdadeiro"
        result == true
    }

    def "remover empresa deve retornar verdadeiro quando a empresa é removida com sucesso"() {
        given: "uma empresa no banco"
        prepararConsultaEmpresa()
        statement.executeUpdate() >> 1

        when: "removemos a empresa"
        boolean result = companyDAO.removerEmpresa(empresa.id)

        then: "o resultado deve ser verdadeiro"
        result == true

    }

    private void prepararConsultaEmpresa() {
        DatabaseConnectionFactory.getInstance().factoryDatabaseConnection.getConnection() >> connection
        connection.prepareStatement(_) >> statement
    }
}
