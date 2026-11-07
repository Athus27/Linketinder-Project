package org.linketinder.dao

import org.linketinder.dao.connection.DatabaseConnectionFactory
import spock.lang.Specification

class SchemaBootstrapTest extends Specification {
    SyntheticDatabaseFixture fresh
    SyntheticDatabaseFixture upgraded

    def cleanup() {
        fresh?.close()
        upgraded?.close()
    }

    def "schema completo equivale ao legado com V1 e V2 incluindo constraints indices e trigger"() {
        given:
        def base = DatabaseConnectionFactory.instance.factoryDatabaseConnection
        fresh = new SyntheticDatabaseFixture(base)
        upgraded = new SyntheticDatabaseFixture(base, true)

        expect:
        columns(fresh) == columns(upgraded)
        constraints(fresh) == constraints(upgraded)
        indexes(fresh) == indexes(upgraded)
        triggers(fresh) == triggers(upgraded)
        columns(fresh).any { it[0] == 'Recruiter' && it[1] == 'ativo' }
        columns(fresh).any { it[0] == 'Curtida' && it[1] == 'id_recrutador_autor' }
    }

    private List columns(SyntheticDatabaseFixture fixture) {
        query(fixture, '''SELECT c.relname, a.attname, format_type(a.atttypid, a.atttypmod),
                a.attnotnull, a.attidentity, pg_get_expr(d.adbin, d.adrelid)
            FROM pg_class c JOIN pg_namespace n ON n.oid = c.relnamespace
            JOIN pg_attribute a ON a.attrelid = c.oid
            LEFT JOIN pg_attrdef d ON d.adrelid = c.oid AND d.adnum = a.attnum
            WHERE n.nspname = ? AND c.relkind = 'r' AND a.attnum > 0 AND NOT a.attisdropped
            ORDER BY c.relname, a.attname''')
    }

    private List constraints(SyntheticDatabaseFixture fixture) {
        query(fixture, '''SELECT c.relname, k.conname, pg_get_constraintdef(k.oid),
                k.condeferrable, k.condeferred
            FROM pg_constraint k JOIN pg_class c ON c.oid = k.conrelid
            JOIN pg_namespace n ON n.oid = c.relnamespace
            WHERE n.nspname = ? ORDER BY c.relname, k.conname''')
    }

    private List indexes(SyntheticDatabaseFixture fixture) {
        query(fixture, '''SELECT tablename, indexname, indexdef FROM pg_indexes
            WHERE schemaname = ? ORDER BY tablename, indexname''')
    }

    private List triggers(SyntheticDatabaseFixture fixture) {
        query(fixture, '''SELECT c.relname, t.tgname, pg_get_triggerdef(t.oid),
                pg_get_functiondef(t.tgfoid)
            FROM pg_trigger t JOIN pg_class c ON c.oid = t.tgrelid
            JOIN pg_namespace n ON n.oid = c.relnamespace
            WHERE n.nspname = ? AND NOT t.tgisinternal ORDER BY c.relname, t.tgname''')
    }

    private List query(SyntheticDatabaseFixture fixture, String sql) {
        fixture.connectionFactory.connection.withCloseable { connection ->
            connection.prepareStatement(sql).withCloseable { statement ->
                statement.setString(1, fixture.schema)
                statement.executeQuery().withCloseable { rows ->
                    List result = []
                    while (rows.next()) {
                        result << (1..rows.metaData.columnCount).collect { column ->
                            rows.getString(column)?.replace(fixture.schema, '<schema>')
                        }
                    }
                    result
                }
            }
        }
    }
}
