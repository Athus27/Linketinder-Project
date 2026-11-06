package org.linketinder.controller

import org.linketinder.dao.curtida.CurtidaDAO

class CurtidaController {
    private final CurtidaDAO curtidaDAO

    CurtidaController() {
        this(new CurtidaDAO())
    }

    CurtidaController(CurtidaDAO curtidaDAO) {
        this.curtidaDAO = curtidaDAO
    }

    Map candidatoCurtirVaga(int idCandidato, int idVaga) {
        return curtidaDAO.candidatoCurtirVaga(idCandidato, idVaga)
    }

    Map empresaCurtirCandidato(int idEmpresa, int idVaga, int idCandidato) {
        return curtidaDAO.empresaCurtirCandidato(idEmpresa, idVaga, idCandidato)
    }

    Map empresaCurtirCandidato(int idEmpresa, int idVaga, int idCandidato, Integer recruiterId) {
        curtidaDAO.empresaCurtirCandidato(idEmpresa, idVaga, idCandidato, recruiterId)
    }

    List<Map> listarMatches(int id, Integer idEmpresa, String tipo) {
        curtidaDAO.listarMatches(id, idEmpresa, tipo)
    }
}
