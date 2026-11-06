package org.linketinder.controller

import org.linketinder.dao.vaga.VagaDAO

class VagaController {
    private final VagaDAO vagaDAO

    VagaController(VagaDAO vagaDAO) {
        this.vagaDAO = vagaDAO
    }

    List<Map> listarVagas() {
        return vagaDAO.listarVagas().vagas
    }

    List<Map> listarVagasPublicas(Integer idEmpresa = null) {
        vagaDAO.listarPublicas(idEmpresa)
    }

    boolean cadastrarVaga(Map vaga, int idEmpresa) {
        vagaDAO.adicionarVaga(vaga, idEmpresa)
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
