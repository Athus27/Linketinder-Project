package org.linketinder.model

class MatchView {
    Integer idCandidato
    Integer idVaga
    String dataMatch
    Map candidato
    Map empresa

    Map asMap() {
        [idCandidato: idCandidato, idVaga: idVaga, dataMatch: dataMatch,
         candidato: candidato, empresa: empresa].findAll { it.value != null }
    }
}
