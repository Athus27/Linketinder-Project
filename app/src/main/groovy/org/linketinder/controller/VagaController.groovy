package org.linketinder.controller

import org.linketinder.dao.vaga.VagaDAO

class VagaController {
    private final VagaDAO vagaDAO

    VagaController() {
        this.vagaDAO = new VagaDAO()
    }

    List<Map> listarVagas() {
        return vagaDAO.listarVagas().vagas
    }

    boolean cadastrarVaga(Map vaga) {
        return vagaDAO.adicionarVaga(vaga)
    }

    boolean atualizarVaga(Map vaga) {
        return vagaDAO.atualizarVaga(vaga)
    }

    boolean removerVaga(int idVaga) {
        return vagaDAO.removerVaga(idVaga)
    }
}
