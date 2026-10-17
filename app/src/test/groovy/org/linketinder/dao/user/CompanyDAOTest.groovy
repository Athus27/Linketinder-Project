package org.linketinder.dao.user

import spock.lang.Specification

import java.sql.Connection
import java.sql.Date
import java.sql.ResultSet
import java.sql.PreparedStatement

class CompanyDAOTest extends Specification {
    def "deve criar uma instancia de CompanyDAO com sucesso"() {
        given: "uma classe CompanyDAO"
        CompanyDAO companyDAO = new CompanyDAO()

        when: "testamos a conexão"
        boolean conexaoEstabelecida = companyDAO.testarConexao()

        then: "a instancia deve ser criada e nao ser nula"
        conexaoEstabelecida == true
    }
}
