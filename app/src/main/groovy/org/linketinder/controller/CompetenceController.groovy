package org.linketinder.controller

import org.linketinder.dao.competence.CompetenceDAO

class CompetenceController {
    private final CompetenceDAO competenceDAO

    CompetenceController(CompetenceDAO competenceDAO) {
        this.competenceDAO = competenceDAO
    }

    List <Map> listarCompetencias() {
        return this.competenceDAO.listarCompetencias().competencias
    }

    boolean checarCompetenciaExistente(String nome) {
        return this.competenceDAO.checarCompetenciaExistente(nome)
    }

    boolean adicionarCompetencia(String nome) {
        return this.competenceDAO.adicionarCompetencia(nome)
    }

    Map getCompetenciaPorNome(String nome) {
        return this.competenceDAO.getCompetenciaPorNome(nome)
    }

    boolean removerCompetencia(int idCompetencia) {
        return this.competenceDAO.removerCompetencia(idCompetencia)
    }

    boolean atualizarCompetencia(int idCompetencia, String novoNome) {
        return this.competenceDAO.atualizarCompetencia(idCompetencia, novoNome)
    }
}
