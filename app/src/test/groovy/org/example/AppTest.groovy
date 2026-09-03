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
}
