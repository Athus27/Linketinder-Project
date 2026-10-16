package org.linketinder.controller

import org.linketinder.dao.user.CandidateDAO

class CandidatoController {
    private final CandidateDAO candidateDAO

    CandidatoController(CandidateDAO candidateDAO) {
        this.candidateDAO = candidateDAO
    }

    List<Map> listarCandidatos() {
        return this.candidateDAO.listarCandidatos().candidatos
    }

    List<Map> feedCandidatos() {
        return this.candidateDAO.listarCandidatos().candidatos
    }

    boolean cadastrarCandidato(Map candidato) {
        return this.candidateDAO.adicionarCandidato(candidato)
    }

    boolean atualizarCandidato(Map candidato) {
        return this.candidateDAO.atualizarCandidato(candidato)
    }

    boolean removerCandidato(int idCandidato) {
        return this.candidateDAO.removerCandidato(idCandidato)
    }
}
