package org.linketinder.view

import spock.lang.Specification

class MenuConsoleTest extends Specification {
    def LerCompetencias() {
        given: "Uma entrada simulada e um menu console"
        // Simula o usuário digitando "Java" e apertando Enter (\n)
        String entradaSimulada = "Java\nsair\n"
        InputStream systemInOriginal = System.in
        System.setIn(new ByteArrayInputStream(entradaSimulada.getBytes()))

        MenuConsole menuConsole = new MenuConsole()

        when: "Ler uma competência"
        List<String> competencias = menuConsole.lerCompetencias()

        then: "A competência lida deve corresponder à entrada"
        competencias == ["Java"]

        cleanup: "Restaurar o System.in original para não afetar outros testes"
        System.setIn(systemInOriginal)
    }

    def "ReadInputCandidate - Deve ler e mapear campos do candidato"() {
        given: "Entradas simuladas na ordem exata solicitada pelo Scanner"
        String entradaSimulada = "Athus\nSilva Souza\n2004/05/27\n11122233344\nathus@email.com\nBrasil\n35930000\nDev Back-End\nEngenharia da Computacao\nsenha123\n"

        InputStream systemInOriginal = System.in
        System.setIn(new ByteArrayInputStream(entradaSimulada.getBytes()))

        MenuConsole menuConsole = new MenuConsole()

        when: "Executar a leitura dos dados"
        Map resultado = menuConsole.readInputCandidate()

        then: "Imprimir o fluxo do dado para a chave do map"
        println "\n========================================"
        println "Mapeamento de Entrada -> Chave do Map:"
        resultado.each { chave, valor ->
            println "Chave [${chave.padRight(15)}] recebeu o valor: ${valor}"
        }
        println "========================================\n"

        and: "Validar se os dados foram inseridos corretamente no map"
        resultado.nome == "Athus"
        resultado.sobrenome == "Silva Souza"
        resultado.dataNascimento == "2004/05/27"
        resultado.cpf == "11122233344"
        resultado.email == "athus@email.com"
        resultado.pais == "Brasil"
        resultado.cep == "35930000"
        resultado.descricao == "Dev Back-End"
        resultado.formacao == "Engenharia da Computacao"
        resultado.senha == "senha123"

        cleanup: "Restaurar o fluxo de entrada padrão"
        System.setIn(systemInOriginal)
    }
    def "ReadInputCompany - Deve ler e mapear campos da empresa"() {
        given: "Entradas simuladas na ordem exata solicitada pelo Scanner"
        String entradaSimulada = "Empresa1\n45.678.901/0001-23\nempresa1@mail.com\nBrasil\n123456-000\nEmpresa de software do brasil\nsenh@123\n"

        InputStream systemInOriginal = System.in
        System.setIn(new ByteArrayInputStream(entradaSimulada.getBytes()))

        MenuConsole menuConsole = new MenuConsole()

        when: "Executar a leitura dos dados"
        Map resultado = menuConsole.readInputCompany()

        then: "Imprimir o fluxo do dado para a chave do map"
        println "\n========================================"
        println "Mapeamento de Entrada -> Chave do Map:"
        resultado.each { chave, valor ->
            println "Chave [${chave.padRight(15)}] recebeu o valor: ${valor}"
        }
        println "========================================\n"

        and: "Validar se os dados foram inseridos corretamente no map"
        resultado.nome == "Empresa1"
        resultado.cnpj == "45.678.901/0001-23"
        resultado.email == "empresa1@mail.com"
        resultado.pais == "Brasil"
        resultado.cep == "123456-000"
        resultado.descricao == "Empresa de software do brasil"
        resultado.senha == "senh@123"

        cleanup: "Restaurar o fluxo de entrada padrão"
        System.setIn(systemInOriginal)
    }
}