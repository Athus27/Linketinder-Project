package org.linketinder.controller

import org.linketinder.dao.curtida.CurtidaDAO

class CurtidaController {
    private final CurtidaDAO curtidaDAO

    CurtidaController() {
        this.curtidaDAO = new CurtidaDAO()
    }

    Map candidatoCurtirVaga(int idCandidato, int idVaga) {
        return curtidaDAO.candidatoCurtirVaga(idCandidato, idVaga)
    }

    Map empresaCurtirCandidato(int idEmpresa, int idVaga, int idCandidato) {
        return curtidaDAO.empresaCurtirCandidato(idEmpresa, idVaga, idCandidato)
    }
}
