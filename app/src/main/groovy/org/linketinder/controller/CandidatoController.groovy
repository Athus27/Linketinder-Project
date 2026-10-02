package org.linketinder.controller

import org.linketinder.dao.user.CandidateDAO

class CandidatoController {
    private final CandidateDAO candidateDAO

    CandidatoController() {
        this.candidateDAO = new CandidateDAO()
    }

    List<Map> listarCandidatos() {
        return candidateDAO.listarCandidatos().candidatos
    }

    List<Map> feedCandidatos() {
        return candidateDAO.listarCandidatos().candidatos
    }

    boolean cadastrarCandidato(Map candidato) {
        return candidateDAO.adicionarCandidato(candidato)
    }

    boolean atualizarCandidato(Map candidato) {
        return candidateDAO.atualizarCandidato(candidato)
    }

    boolean removerCandidato(int idCandidato) {
        return candidateDAO.removerCandidato(idCandidato)
    }
}
