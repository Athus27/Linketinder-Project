package org.linketinder.dao

import spock.lang.Specification

import java.sql.Connection

class ConexaoDBTest extends Specification {
    def "deve estabelecer conexao com o banco de dados com sucesso"() {
        when: "solicitamos uma conexao"
        Connection conexao = ConexaoDB.getConnection()

        then: "a conexao deve ser retornada e nao ser nula"
        conexao != null

        and: "a conexao deve estar aberta (valida)"
        !conexao.isClosed()

        cleanup: "garante que a conexao seja fechada apos o teste"
        if (conexao != null && !conexao.isClosed()) {
            conexao.close()
        }
    }

    def "mostrarTabelas deve listar todas as tabelas do banco de dados"() {
        given: "uma instancia de ConexaoDB"
        ConexaoDB conexaoDB = new ConexaoDB()

        when: "chamamos o metodo mostrarTabelas"
        boolean resultado = conexaoDB.mostrarTabelas()

        then: "o metodo deve retornar true indicando que as tabelas foram listadas com sucesso"
        resultado == true
    }
}
