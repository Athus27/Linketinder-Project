package org.linketinder.controller

import org.linketinder.dao.user.CandidateDAO

class CandidatoController {
    private final CandidateDAO candidateDAO
    // TODO: separar interfaces
    CandidatoController(CandidateDAO candidateDAO) {
        this.candidateDAO = candidateDAO
    }

    List<Map> listarCandidatosPublicos(Integer idEmpresa = null) {
        candidateDAO.listarPublicos(idEmpresa)
    }

    Map buscarCandidatoPublico(int id, Integer idEmpresa = null) {
        candidateDAO.buscarPublico(id, idEmpresa)
    }

    List<Map> listarCandidatos() {
        return this.candidateDAO.listarCandidatos().candidatos
    }

    Map buscarCandidatoPorId(int idCandidato) {
        return candidateDAO.buscarPorId(idCandidato)
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
