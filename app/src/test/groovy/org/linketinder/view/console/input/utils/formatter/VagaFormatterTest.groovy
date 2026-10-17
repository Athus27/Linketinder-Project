package org.linketinder.view.console.input.utils.formatter

import spock.lang.Specification

class VagaFormatterTest extends Specification {
    private Map vaga

    void setup() {
        vaga = [
                idEmpresa   : 1,
                empresa     : [
                        id       : 1,
                        nome     : "Empresa1",
                        cnpj     : "12.345.678/0001-00",
                        descricao: "descEmpresa1",
                        email    : "empresa1@gmail.com",
                        pais     : "Brasil",
                        cep      : "39660-000"
                ],
                titulo      : "Vaga de Teste 1",
                descricao   : "Descrição1",
                localVaga   : "LocalTeste1",
                competencias: ["Comp1", "Comp2", "Comp3"]
        ]
    }

    void cleanup() {
    }

    def "StringVagaFeed mostra apenas dados não sensíveis"() {
        when: "Dado uma vaga chamamos o formatter dela:"
        String vagaParaFeed = VagaFormatter.stringVagaFeed(vaga)

        then: "Não deve exibir dados sensíveis"
        vagaParaFeed.contains("Vaga de Teste 1") &&
        vagaParaFeed.contains("Descrição1") &&
        vagaParaFeed.contains("Comp1, Comp2, Comp3")
    }

    def "StringVagaFeed  não exibe dados da empresa"() {
        when: "Dado uma vaga chamamos o formatter dela:"
        String vagaParaFeed = VagaFormatter.stringVagaFeed(vaga)

        then: "Não deve exibir dados da empresa"
        !vagaParaFeed.contains("12.345.678/0001-00") &&
        !vagaParaFeed.contains("descEmpresa1") &&
        !vagaParaFeed.contains("empresa1@gmail.com") &&
        !vagaParaFeed.contains("Brasil") &&
        !vagaParaFeed.contains("39660-000")
    }
}
