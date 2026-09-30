package org.linketinder.controller

import org.linketinder.dao.CandidateDAO

class CandidatoController {
    private final CandidateDAO candidateDAO

    CandidatoController() {
        this.candidateDAO = new CandidateDAO()
    }

    List<Map> listarCandidatos() {
        return candidateDAO.listarCandidatos().candidatos
    }
}
