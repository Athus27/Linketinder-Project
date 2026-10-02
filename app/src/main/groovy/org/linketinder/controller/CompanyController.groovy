package org.linketinder.controller

import org.linketinder.dao.user.CompanyDAO

class CompanyController {
    private final CompanyDAO companyDAO

    CompanyController() {
        this.companyDAO = new CompanyDAO()
    }

    boolean cadastrarEmpresa(Map empresa) {
        return companyDAO.adicionarEmpresa(empresa)
    }

    List<Map> listarEmpresas() {
        return companyDAO.listarEmpresas().empresas
    }

    List<Map> feedEmpresas() {
        return companyDAO.listarEmpresas().empresas
    }

    boolean atualizarEmpresa(Map empresa) {
        return companyDAO.atualizarEmpresa(empresa)
    }

    boolean removerEmpresa(int idEmpresa) {
        return companyDAO.removerEmpresa(idEmpresa)
    }
}
