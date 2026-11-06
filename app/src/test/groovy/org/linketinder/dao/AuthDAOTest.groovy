package org.linketinder.dao

import org.linketinder.dao.connection.ConnectionFactory
import org.linketinder.dao.connection.DatabaseConnectionFactory
import org.linketinder.dao.user.AuthDAO
import spock.lang.Specification

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet

class AuthDAOTest extends Specification {
    private Connection connection
    private PreparedStatement statement
    private ResultSet resultSet
    private AuthDAO authDAO

    def setup() {
        authDAO = new AuthDAO()
        connection = Mock()
        statement = Mock()
        resultSet = Mock()
        ConnectionFactory factory = Mock()
        DatabaseConnectionFactory manager = Mock()

        GroovyMock(DatabaseConnectionFactory, global: true)
        DatabaseConnectionFactory.getInstance() >> manager
        manager.getFactoryDatabaseConnection() >> factory
        factory.getConnection() >> connection
        connection.prepareStatement(_ as String) >> statement
        statement.executeQuery() >> resultSet
    }

    def "autentica pelo e-mail sem diferenciar caixa e retorna apenas perfil público"() {
        given:
        resultSet.next() >> true
        resultSet.getInt("id") >> 7
        resultSet.getString("nome") >> "Athus"
        resultSet.getString("tipo") >> "candidate"

        when:
        Map user = authDAO.autenticar(" ATHUS@EXAMPLE.COM ", "senha123")

        then:
        1 * statement.setString(1, " ATHUS@EXAMPLE.COM ")
        1 * statement.setString(2, "senha123")
        user == [id: 7, nome: "Athus", tipo: "candidate"]
        !user.containsKey("senha")
        1 * resultSet.close()
        1 * statement.close()
        1 * connection.close()
    }

    def "retorna nulo quando não houver credenciais válidas"() {
        given:
        resultSet.next() >> false

        expect:
        authDAO.autenticar("nao-existe@example.com", "senha") == null
    }
}
