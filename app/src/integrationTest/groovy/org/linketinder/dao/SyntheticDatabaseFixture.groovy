package org.linketinder.dao

import org.linketinder.dao.connection.ConnectionFactory

import java.nio.file.Files
import java.nio.file.Path
import java.sql.Connection

class SyntheticDatabaseFixture implements AutoCloseable {
    private final ConnectionFactory base
    private final String schema
    private final TemporarySchemaConnectionFactory factory

    SyntheticDatabaseFixture(ConnectionFactory base, boolean upgradeLegacy = false) {
        this.base = base
        this.schema = "lt_test_${UUID.randomUUID().toString().replace('-', '')}"
        this.factory = new TemporarySchemaConnectionFactory(base, schema)
        createAndMigrate(upgradeLegacy)
    }

    ConnectionFactory getConnectionFactory() {
        factory
    }

    String getSchema() {
        schema
    }

    @Override
    void close() {
        Connection connection
        try {
            connection = base.getConnection()
            connection.createStatement().execute("DROP SCHEMA IF EXISTS \"${schema}\" CASCADE")
        } finally {
            connection?.close()
        }
    }

    private void createAndMigrate(boolean upgradeLegacy) {
        Connection connection
        try {
            connection = base.getConnection()
            connection.createStatement().execute("CREATE SCHEMA \"${schema}\"")
            connection.close()
            connection = factory.getConnection()
            if (upgradeLegacy) {
                def resource = getClass().getResourceAsStream('/schema/Linketinder_pre_V2.sql')
                if (resource == null) throw new FileNotFoundException('Historical test schema missing')
                resource.withCloseable { stream ->
                    connection.createStatement().withCloseable { it.execute(stream.getText('UTF-8')) }
                }
                executeSql(connection, sqlFile("migrations/V1__unique_user_email_normalized.sql"))
                executeSql(connection, sqlFile("migrations/V2__recruiter_and_like_author.sql"))
            } else {
                executeSql(connection, sqlFile("Linketinder.sql"))
            }
        } catch (Exception failure) {
            try {
                connection?.close()
            } finally {
                close()
            }
            throw failure
        } finally {
            connection?.close()
        }
    }

    private static void executeSql(Connection connection, Path file) {
        connection.createStatement().execute(Files.readString(file))
    }

    private static Path sqlFile(String relativePath) {
        Path current = Path.of(System.getProperty("user.dir")).toAbsolutePath().normalize()
        Path root = current.fileName?.toString() == "app" ? current.parent : current
        root.resolve("sql").resolve(relativePath)
    }
}
