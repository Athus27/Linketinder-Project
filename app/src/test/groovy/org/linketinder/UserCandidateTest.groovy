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


    def "ToString não expõe identificadores"() {
        given:
        UserCandidate pessoaFisica = criarPessoafisicaPadrao()
        when:
        String texto = pessoaFisica.toString()

        then:
        !texto.containsIgnoreCase("Athus")
        !texto.containsIgnoreCase("athus@email.com")
        !texto.containsIgnoreCase("123.456.789-01")
        !texto.containsIgnoreCase("39660-000")
        texto.containsIgnoreCase("Java, Groovy")
        !texto.containsIgnoreCase("MG")


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
        ["Athus", "athus@email.com", "123.456.789-01", "39660-000"].every {
            !feedInfo.containsIgnoreCase(it)
        }
        !feedInfo.containsIgnoreCase("Desenvolvedor")

    }

}
