package org.linketinder.controller

import org.linketinder.dao.competence.CompetenceDAO

class CompetenceController {
    private final CompetenceDAO competenceDAO

    CompetenceController() {
        this.competenceDAO = new CompetenceDAO()
    }

    List <Map> listarCompetencias() {
        return competenceDAO.listarCompetencias().competencias
    }

    boolean checarCompetenciaExistente(String nome) {
        return competenceDAO.checarCompetenciaExistente(nome)
    }

    boolean adicionarCompetencia(String nome) {
        return competenceDAO.adicionarCompetencia(nome)
    }

    Map getCompetenciaPorNome(String nome) {
        return competenceDAO.getCompetenciaPorNome(nome)
    }

    boolean removerCompetencia(int idCompetencia) {
        return competenceDAO.removerCompetencia(idCompetencia)
    }

    boolean atualizarCompetencia(int idCompetencia, String novoNome) {
        return competenceDAO.atualizarCompetencia(idCompetencia, novoNome)
    }
}
