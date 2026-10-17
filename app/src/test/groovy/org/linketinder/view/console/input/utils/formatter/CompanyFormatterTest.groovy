package org.linketinder.view.console.input.utils.formatter

import spock.lang.Specification

class CompanyFormatterTest extends Specification {
    private Map empresa

    def setup(){
        empresa = [
                id: 1,
                nome: "Empresa1",
                cnpj: "12.345.678/0001-00",
                descricao: "desc1",
                email: "empresa1@gmail.com",
                pais: "Brasil",
                cep: "39660-000"
        ]
    }

    def "Ao transformar uma Empresa para string deve manter todos os dados"(){
        when:"aplicamos o formatter em 'empresa'"
        String empresaString = CompanyFormatter.stringEmpresa(empresa)

        then:"deve exibir todos os dados"
        empresaString.contains("Empresa1") &&
        empresaString.contains("12.345.678/0001-00") &&
        empresaString.contains("desc1") &&
        empresaString.contains("empresa1@gmail.com") &&
        empresaString.contains("Brasil") &&
        empresaString.contains("39660-000")
    }

}
