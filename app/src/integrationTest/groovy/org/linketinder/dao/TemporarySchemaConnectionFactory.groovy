package org.linketinder.dao

import org.linketinder.dao.connection.ConnectionFactory

import java.sql.Connection

class TemporarySchemaConnectionFactory implements ConnectionFactory {
    private static final String SCHEMA_PATTERN = '^[a-z][a-z0-9_]{0,62}$'
    private final ConnectionFactory base
    private final String schema

    TemporarySchemaConnectionFactory(ConnectionFactory base, String schema) {
        this.base = base
        this.schema = validate(schema)
    }

    @Override
    Connection getConnection() {
        Connection connection = base.getConnection()
        connection.createStatement().execute("SET search_path TO \"${schema}\", public")
        connection
    }

    String getSchema() {
        schema
    }

    private static String validate(String value) {
        if (value == null || !(value ==~ SCHEMA_PATTERN)) {
            throw new IllegalArgumentException("Invalid temporary schema name")
        }
        value
    }
}
