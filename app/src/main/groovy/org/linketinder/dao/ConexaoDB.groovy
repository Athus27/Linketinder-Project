package org.linketinder.dao

import groovy.transform.PackageScope

import java.sql.Connection
import java.sql.DriverManager

class ConexaoDB {
    private static Properties props = new Properties()

    static {
        def resourceName = "db.properties"
        def classLoader = Thread.currentThread().getContextClassLoader()
        def resourceStream = classLoader.getResourceAsStream(resourceName)

        if (resourceStream) {
            props.load(resourceStream)
        } else {
            throw new FileNotFoundException("Arquivo de propriedades '${resourceName}' não encontrado no classpath.")
        }
    }

    static Connection getConnection() {
        return DriverManager.getConnection(
                props.getProperty("db.url"),
                props.getProperty("db.user"),
                props.getProperty("db.password")
        )
    }

    @PackageScope
    boolean mostrarTabelas() {
        Connection connection = null
        java.sql.Statement statement = null
        java.sql.ResultSet resultSet = null
        boolean result = false

        try {
            connection = getConnection()
            statement = connection.createStatement()

            // Comando SQL correto para listar as tabelas públicas no SQL
            String sql = "SELECT table_name FROM information_schema.tables WHERE table_schema = 'public'"
            resultSet = statement.executeQuery(sql)

            println "--- Tabelas do Sistema ---"
            while (resultSet.next()) {
                // Pega o valor da coluna "table_name"
                println resultSet.getString("table_name")
            }

            result = true

        } catch (Exception e) {
            println "Erro ao mostrar tabelas: ${e.message}"
            result = false
        } finally {
            // É necessário verificar se não são nulos antes de fechar
            if (resultSet != null) resultSet.close()
            if (statement != null) statement.close()
            if (connection != null) connection.close()
        }

        return result
    }
}