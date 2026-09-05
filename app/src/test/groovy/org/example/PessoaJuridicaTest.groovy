package org.example

import spock.lang.Specification

class PessoaJuridicaTest extends Specification {

    private PessoaJuridica criaPessoaJuridicaPadrao() {

        def empresa = new PessoaJuridica(
                nome: "Empresa X",
                descricao: "Empresa de tecnologia",
                email: "empresax@mail.com",
                estado: "SP",
                cep: "12345-678",
                competencias: ["Java", "Groovy", "GitHub"],
                CNPJ: "12.345.678/0001-90",
                pais: "Brasil",
                vagas: []
        )
        Vaga vaga = new Vaga(
                empresa,
                "Desenvolvedor Java",
                "Vaga para desenvolvedor Java com experiência em Spring Boot",
                ["Java", "Spring Boot", "Git"],
        )
        empresa.vagas.add(vaga)

        return empresa

    }

    def "ToString mostra todos os dados"() {
        given: "Uma pessoa juridica com dados"
        PessoaJuridica pessoaJuridica = criaPessoaJuridicaPadrao()

        when: "chama o método toString"
        String texto = pessoaJuridica.toString()

        then: "O texto contém todos os dados da pessoa juridica"
        texto.containsIgnoreCase(dados)

        where: "verifica os dados da pessoa juridica"
        dados << ["Empresa X",
                  "Empresa de tecnologia",
                  "empresax@mail.com",
                  "SP",
                  "12345-678",
                  "Java, Groovy, GitHub",
                  "12.345.678/0001-90",
                  "Brasil"
        ]
    }

    def "Feed mostra dados gerais "() {
        given: "Uma pessoa juridica com dados sensiveis"
        PessoaJuridica pessoaJuridica = criaPessoaJuridicaPadrao()

        when: "chama o método de exibir informações"
        String feedInfo = pessoaJuridica.exibirInformacoesFeed()

        then: "O texto contém todos os dados da pessoa juridica, sem dados sensíveis"
        feedInfo.containsIgnoreCase(dadosGerais)

        where: "verifica os dados da pessoa juridica"
        dadosGerais << ["Desenvolvedor Java",
                        "Vaga para desenvolvedor Java com experiência em Spring Boot",
                        "Java, Spring Boot, Git"
        ]
    }

    def "Feed oculta dados sensiveis "() {
        given: "Uma pessoa juridica com dados sensiveis"
        PessoaJuridica pessoaJuridica = criaPessoaJuridicaPadrao()

        when: "chama o método de exibir informações"
        String feedInfo = pessoaJuridica.exibirInformacoesFeed()

        then: "O texto não contém dados sensíveis da pessoa juridica"
        !feedInfo.containsIgnoreCase(dadosSensiveis)

        where: "dados sensíveis da pessoa juridica"
        dadosSensiveis << ["Empresa X",
                           "empresax@mail.com",
                           "12345-678",
                           "Java, Groovy, GitHub",
                           "Brasil"]

    }

    def "Empresas iguais possuem o mesmo HashCode"() {
        given: "Duas empresas iguais"
        PessoaJuridica empresa1 = criaPessoaJuridicaPadrao()
        PessoaJuridica empresa2 = criaPessoaJuridicaPadrao()

        when: "compara os hashcodes das empresas"
        int hash1 = empresa1.hashCode()
        int hash2 = empresa2.hashCode()

        then: "os hashcodes são iguais"
        hash1 == hash2
    }

}
