package org.linketinder.view.console.input.utils.formatter

import spock.lang.Specification

class CandidateFormatterTest extends Specification {
    def "StringCandidato - Deve exibir todos os campos, exceto a senha"() {
        given: "um candidato com todos os campos"
        Map candidato = [
                id            : 1,
                nome          : "Fulano",
                sobrenome     : "da Silva",
                dataNascimento: "1990-01-15",
                cpf           : "111.111.111-11",
                descricao     : "Desenvolvedor backend",
                email         : "fulano@email.com",
                formacao      : "Ciência da Computação",
                pais          : "Brasil",
                cep           : "01010-000",
                competencias  : ["Groovy", "SQL"],
                senha         : "segredo"
        ]

        when: "formatamos os dados para exibição"
        String resultado = CandidateFormatter.stringCandidato(candidato)

        then: "todos os campos públicos são exibidos"
        resultado.readLines() == [
                "",
                "ID: 1",
                "Nome: Fulano",
                "Sobrenome: da Silva",
                "Data de nascimento: 1990-01-15",
                "CPF: 111.111.111-11",
                "Descrição: Desenvolvedor backend",
                "E-mail: fulano@email.com",
                "Formação: Ciência da Computação",
                "País: Brasil",
                "CEP: 01010-000",
                "Competências: Groovy, SQL"
        ]

        and: "a senha não é exibida"
        !resultado.contains("segredo")
        !resultado.contains("Senha:")
    }
}
