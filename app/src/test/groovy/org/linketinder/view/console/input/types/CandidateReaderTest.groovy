package org.linketinder.view.console.input.types

import spock.lang.Specification
import org.linketinder.view.console.input.LeitorConsole

class CandidateReaderTest extends Specification {
    def "ReadInputCandidate - Deve ler e mapear campos do candidato"() {
        given: "Entradas simuladas na ordem exata solicitada pelo Scanner"
        String entradaSimulada = "Athus\nSilva Souza\n27/05/2004\n111.222.333-44\nathus@email.com\nBrasil\n35930-000\nDev Back-End\nEngenharia da Computacao\nsenha123\n"

        LeitorConsole leitor = new LeitorConsole(new Scanner(entradaSimulada))
        CandidateReader reader = new CandidateReader(leitor, new UserReader(leitor))

        when: "Executar a leitura dos dados"
        Map resultado = reader.readInputCandidate()

        then: "Validar se os dados foram inseridos corretamente no map"
        resultado.nome == "Athus"
        resultado.sobrenome == "Silva Souza"
        resultado.dataNascimento == "27/05/2004"
        resultado.cpf == "111.222.333-44"
        resultado.email == "athus@email.com"
        resultado.pais == "Brasil"
        resultado.cep == "35930-000"
        resultado.descricao == "Dev Back-End"
        resultado.formacao == "Engenharia da Computacao"
        resultado.senha == "senha123"
    }
}
