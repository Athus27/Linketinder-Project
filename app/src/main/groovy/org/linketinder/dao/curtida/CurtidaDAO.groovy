package org.linketinder.dao.curtida

import org.linketinder.dao.connection.ConnectionFactory
import org.linketinder.dao.connection.DatabaseConnectionFactory

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet

class CurtidaDAO {
    private final ConnectionFactory connectionFactory

    CurtidaDAO() {
        this(null)
    }

    CurtidaDAO(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory
    }

    Map candidatoCurtirVaga(int idCandidato, int idVaga) {
        registrar(idCandidato, idVaga, true, null)
    }

    Map empresaCurtirCandidato(int idEmpresa, int idVaga, int idCandidato) {
        Connection connection = openConnection()
        try {
            PreparedStatement statement = connection.prepareStatement(
                    'SELECT 1 FROM "Vaga" WHERE id = ? AND id_empresa = ?')
            statement.setInt(1, idVaga)
            statement.setInt(2, idEmpresa)
            ResultSet result = statement.executeQuery()
            try {
                if (!result.next()) return [sucesso: false, codigo: "NOT_FOUND"]
            } finally {
                result.close()
                statement.close()
            }
        } finally {
            connection.close()
        }
        registrar(idCandidato, idVaga, false, null)
    }

    Map empresaCurtirCandidato(int idEmpresa, int idVaga, int idCandidato, Integer recruiterId) {
        Connection connection = openConnection()
        try {
            PreparedStatement statement = connection.prepareStatement(
                    'SELECT 1 FROM "Vaga" WHERE id = ? AND id_empresa = ?')
            statement.setInt(1, idVaga)
            statement.setInt(2, idEmpresa)
            ResultSet result = statement.executeQuery()
            try {
                if (!result.next()) return [sucesso: false, codigo: "NOT_FOUND"]
            } finally {
                result.close()
                statement.close()
            }
        } finally {
            connection.close()
        }
        registrar(idCandidato, idVaga, false, recruiterId)
    }

    private Map registrar(int idCandidato, int idVaga, boolean candidato, Integer recruiterId) {
        Connection connection = openConnection()
        try {
            connection.autoCommit = false
            PreparedStatement insert = connection.prepareStatement('''
                INSERT INTO "Curtida" (id_candidato, id_vaga, curtida_candidato, curtida_empresa)
                VALUES (?, ?, false, false) ON CONFLICT (id_candidato, id_vaga) DO NOTHING
            ''')
            insert.setInt(1, idCandidato)
            insert.setInt(2, idVaga)
            insert.executeUpdate()
            insert.close()

            PreparedStatement select = connection.prepareStatement('''
                SELECT curtida_candidato, curtida_empresa, data_match, id_recrutador_autor
                FROM "Curtida" WHERE id_candidato = ? AND id_vaga = ? FOR UPDATE
            ''')
            select.setInt(1, idCandidato)
            select.setInt(2, idVaga)
            ResultSet result = select.executeQuery()
            if (!result.next()) {
                result.close()
                select.close()
                connection.rollback()
                return [sucesso: false, codigo: "NOT_FOUND"]
            }
            boolean candidateLiked = result.getBoolean("curtida_candidato")
            boolean companyLiked = result.getBoolean("curtida_empresa")
            boolean already = candidato ? candidateLiked : companyLiked
            boolean match = candidateLiked && companyLiked
            result.close()
            select.close()

            String column = candidato ? "curtida_candidato" : "curtida_empresa"
            String sql = candidato
                    ? """UPDATE "Curtida" SET curtida_candidato = true,
                         data_match = CASE WHEN curtida_empresa THEN COALESCE(data_match, CURRENT_TIMESTAMP) ELSE data_match END
                         WHERE id_candidato = ? AND id_vaga = ?"""
                    : """UPDATE "Curtida" SET curtida_empresa = true,
                         id_recrutador_autor = CASE WHEN NOT curtida_empresa THEN COALESCE(id_recrutador_autor, ?) ELSE id_recrutador_autor END,
                         data_match = CASE WHEN curtida_candidato THEN COALESCE(data_match, CURRENT_TIMESTAMP) ELSE data_match END
                         WHERE id_candidato = ? AND id_vaga = ?"""
            PreparedStatement update = connection.prepareStatement(sql)
            if (candidato) {
                update.setInt(1, idCandidato)
                update.setInt(2, idVaga)
            } else {
                update.setObject(1, recruiterId)
                update.setInt(2, idCandidato)
                update.setInt(3, idVaga)
            }
            update.executeUpdate()
            update.close()
            connection.commit()
            boolean newCandidateLiked = candidateLiked || candidato
            boolean newCompanyLiked = companyLiked || !candidato
            [sucesso: true, jaCurtiu: already,
             deuMatch: newCandidateLiked && newCompanyLiked]
        } catch (java.sql.SQLException sqlException) {
            try { connection.rollback() } catch (Exception ignoredRollback) { }
            [sucesso: false, codigo: sqlException.SQLState == "23503" ? "NOT_FOUND" : "INTERNAL_ERROR"]
        } catch (Exception ignored) {
            try { connection.rollback() } catch (Exception ignoredRollback) { }
            [sucesso: false, codigo: "INTERNAL_ERROR"]
        } finally {
            connection.close()
        }
    }

    List<Map> listarMatches(int idCandidato, Integer idEmpresa, String tipo) {
        Connection connection = openConnection()
        try {
            String sql = tipo == "candidate" ? '''
                SELECT l.id_candidato, l.id_vaga, l.data_match, e.id_empresa, u.nome, u.email
                FROM "Curtida" l JOIN "Vaga" v ON v.id = l.id_vaga
                JOIN "Company" e ON e.id_empresa = v.id_empresa
                JOIN "User" u ON u.id = e.id_empresa
                WHERE l.id_candidato = ? AND l.curtida_candidato AND l.curtida_empresa
            ''' : '''
                SELECT l.id_candidato, l.id_vaga, l.data_match, u.nome, c.sobrenome, u.email, c.descricao
                FROM "Curtida" l JOIN "Vaga" v ON v.id = l.id_vaga
                JOIN "Candidate" c ON c.id_candidato = l.id_candidato
                JOIN "User" u ON u.id = c.id_candidato
                WHERE l.curtida_candidato AND l.curtida_empresa AND v.id_empresa = ?
            '''
            PreparedStatement statement = connection.prepareStatement(sql)
            statement.setInt(1, tipo == "candidate" ? idCandidato : idEmpresa)
            ResultSet result = statement.executeQuery()
            try {
                List<Map> matches = []
                while (result.next()) {
                    Map item = [idCandidato: result.getInt("id_candidato"),
                                idVaga: result.getInt("id_vaga"),
                                dataMatch: result.getTimestamp("data_match")?.toInstant()?.toString()]
                    if (tipo == "candidate") {
                        item.empresa = [id: result.getInt("id_empresa"),
                                        nome: result.getString("nome"), email: result.getString("email")]
                    } else {
                        item.candidato = [id: result.getInt("id_candidato"),
                                          nome: result.getString("nome"),
                                          sobrenome: result.getString("sobrenome"),
                                          email: result.getString("email"),
                                          descricao: result.getString("descricao")]
                    }
                    matches << item
                }
                return matches
            } finally {
                result.close()
                statement.close()
            }
        } finally {
            connection.close()
        }
    }

    private Connection openConnection() {
        (connectionFactory ?:
                DatabaseConnectionFactory.getInstance().getFactoryDatabaseConnection()).getConnection()
    }
}
