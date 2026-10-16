package org.linketinder.view.console.input.types

import spock.lang.Specification
import org.linketinder.view.console.input.LeitorConsole

class CompanyReaderTest extends Specification {
    def "ReadInputCompany - Deve ler e mapear campos da empresa"() {
        given: "Entradas simuladas na ordem exata solicitada pelo Scanner"
        String entradaSimulada = "Empresa1\n45.678.901/0001-23\nempresa1@mail.com\nBrasil\n12345-000\nEmpresa de software do brasil\nsenh@123\n"

        LeitorConsole leitor = new LeitorConsole(new Scanner(entradaSimulada))
        CompanyReader reader = new CompanyReader(leitor, new UserReader(leitor))

        when: "Executar a leitura dos dados"
        Map resultado = reader.readInputCompany()

        then: "Validar se os dados foram inseridos corretamente no map"
        resultado.nome == "Empresa1"
        resultado.cnpj == "45.678.901/0001-23"
        resultado.email == "empresa1@mail.com"
        resultado.pais == "Brasil"
        resultado.cep == "12345-000"
        resultado.descricao == "Empresa de software do brasil"
        resultado.senha == "senh@123"
    }
}
