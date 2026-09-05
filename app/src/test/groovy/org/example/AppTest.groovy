package org.example

import spock.lang.Specification

class AppTest extends Specification {
    def "dados iniciais incluem uma vaga para cada empresa"() {
        given:
        App app = new App()

        when:
        app.carregarDadosIniciais()

        then:
        app.empresas.size() == 5
        app.empresas.every { it.vagas.size() == 1 }
    }

    def "feed de empresas retorna vagas formatadas sem null"() {
        given:
        App app = new App()
        app.carregarDadosIniciais()

        when:
        def feed = app.empresas.first().exibirInformacoesFeed()

        then:
        feed.contains("Título: Desenvolvedor Backend Java")
        feed.contains("Competências: Java, Groovy, Spring Framework")
        !feed.contains("null")
    }

    def "Adiciona candidato na lista de candidatos"() {
        given:
        App app = new App()
        PessoaFisica candidato = new PessoaFisica(nome: "Teste", cpf: "123.456.789-00")

        when:
        app.adicionarCandidato(candidato)

        then:
        app.candidatos.contains(candidato)
    }

    def "Adiciona empresa na lista de empresas"() {
        given:
        App app = new App()
        PessoaJuridica empresa = new PessoaJuridica(nome: "Empresa Teste", CNPJ: "12.345.678/0001-90")

        when:
        app.adicionarEmpresa(empresa)

        then:
        app.empresas.contains(empresa)
    }

}
