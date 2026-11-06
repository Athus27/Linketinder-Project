package org.linketinder.dao

import org.linketinder.dao.connection.DatabaseConnectionFactory
import spock.lang.Specification

import java.sql.Connection
import java.sql.PreparedStatement

class NormalizedEmailUniquenessTest extends Specification {
    def "banco rejeita e-mails que diferem por caixa e espaços externos"() {
        Connection connection
        PreparedStatement duplicate
        String email = "email-unique-${UUID.randomUUID()}@example.test"

        given:
        connection = DatabaseConnectionFactory.getInstance()
                .getFactoryDatabaseConnection().getConnection()
        connection.autoCommit = false
        PreparedStatement first = connection.prepareStatement(
                'INSERT INTO "User" (nome, email, "país", cep, senha) VALUES (?, ?, ?, ?, ?)'
        )
        first.setString(1, "Teste de unicidade")
        first.setString(2, email)
        first.setString(3, "Brasil")
        first.setString(4, "00000-000")
        first.setString(5, "hash-de-teste")
        first.executeUpdate()

        duplicate = connection.prepareStatement(
                'INSERT INTO "User" (nome, email, "país", cep, senha) VALUES (?, ?, ?, ?, ?)'
        )
        duplicate.setString(1, "Outro perfil")
        duplicate.setString(2, "  ${email.toUpperCase()}  ")
        duplicate.setString(3, "Brasil")
        duplicate.setString(4, "00000-000")
        duplicate.setString(5, "hash-de-teste")

        when:
        duplicate.executeUpdate()

        then:
        thrown(java.sql.SQLException)

        cleanup:
        connection?.rollback()
        connection?.close()
    }
}
