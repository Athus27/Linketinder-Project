package org.linketinder.dao.connection

import java.sql.Connection

interface ConnectionFactory {
    Connection getConnection()
}