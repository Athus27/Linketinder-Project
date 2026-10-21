package org.linketinder.dao.connection

import spock.lang.Specification

class DatabaseConnectionFactoryTest extends Specification {
    def "Dado nenhuma instancia, ele permite a criada apenas uma, e na proxima criação deve retornar a unica"(){
        given:"criando instancia seguindo o singleton"
        DatabaseConnectionFactory databaseConnectionFactory = DatabaseConnectionFactory.getInstance()
        when:"ao tentar criar outra instancia"
        DatabaseConnectionFactory databaseConnectionFactoryOutra = databaseConnectionFactory.getInstance()
        then:"os dois objetos devem ser o mesmo, mesmo & na memória"
        databaseConnectionFactory===databaseConnectionFactoryOutra
    }
    def "Ao criar uma factory Exclusiva para certo tipo de BD, ele cria somente uma instancia"(){
        given:"uma instancia de uma db factory"
        DatabaseConnectionFactory databaseConnectionFactory = DatabaseConnectionFactory.getInstance()
        ConnectionFactory conexao1 = databaseConnectionFactory.getFactoryDatabaseConnection()
        when:"ao tentarmos criar outra"
        ConnectionFactory conexao2 = databaseConnectionFactory.getFactoryDatabaseConnection()
        then:"deve retornar a mesma instancia, e não criar outra"
        conexao1==conexao2
    }
}
