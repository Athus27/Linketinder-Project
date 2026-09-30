package org.linketinder

import org.linketinder.model.Pessoa.UserCandidate
import spock.lang.Specification

/*
Lembrar antes de escrever
1. Qual regra esse teste protege?
2. Que bug real faria esse teste falhar?
3. Esse teste depende de detalhe bobo ou de comportamento importante?

 */

class UserCandidateTest extends Specification {
    private UserCandidate criarPessoafisicaPadrao(){
        return new UserCandidate(
                nome: "Athus", idade: 22,
                email: "athus@email.com",
                descricao: "Desenvolvedor Full stack graduando em engenharia da computação pela UFOP",
                competencias: ["Java", "Groovy", "GitHub", "Linux", "Scrum", "Kanban", "Gradle"],
                cep: "39660-000",
                estado: "MG",
                cpf: '123.456.789-01'
        )
    }


    def "ToString mostra todos os dados "() {
        given:
        UserCandidate pessoaFisica = criarPessoafisicaPadrao()
        when:
        String texto = pessoaFisica.toString()

        then:
        texto.containsIgnoreCase("Athus")
        texto.containsIgnoreCase("athus@email.com")
        texto.containsIgnoreCase("123.456.789-01")
        texto.containsIgnoreCase("22")
        texto.containsIgnoreCase("Java, Groovy")
        texto.containsIgnoreCase("MG")


    }

    def "Exibe Informacoes no Feed sem dados sensiveis"() {
        given: "Uma pessoa fisica com dados sensiveis"
        UserCandidate pessoaFisica = new UserCandidate(
                nome: "Athus", idade: 22,
                email: "athus@email.com",
                descricao: "Desenvolvedor Full stack graduando em engenharia da computação pela UFOP",
                competencias: ["Java", "Groovy", "GitHub", "Linux", "Scrum", "Kanban", "Gradle"],
                estado: "MG",
                cep: "39660-000",
                cpf: '123.456.789-01'
        )

        when: "chama o método de exibir informações"

        String feedInfo = pessoaFisica.exibirInformacoesFeed()

        then:
        !feedInfo.containsIgnoreCase(valorSensivel)

        where: "Implementa o conceito de testes orientados a dados DDT, permite testar o mesmo comportamento com múltiplas entradas"
            valorSensivel << [    //o operador << alimenta a variável com elementos da lista definida
                    "Athus",
                    "athus@email.com",
                    "123.456.789-01"
            ]

    }

}
