package org.linketinder.view.console.input.types

import spock.lang.Specification
import org.linketinder.view.console.input.LeitorConsole

class CompetenceReaderTest extends Specification {
    def "deve ler competencias ate o usuario informar sair"() {
        given: "uma entrada simulada e um leitor de competencias"
        String entradaSimulada = "Java\nsair\n"
        LeitorConsole leitor = new LeitorConsole(new Scanner(entradaSimulada))
        CompetenceReader reader = new CompetenceReader(leitor)
        reader.competenceController.metaClass.checarCompetenciaExistente = { String nome -> true }

        when: "Ler uma competência"
        List<String> competencias = reader.lerCompetenciasPeloConsole()

        then: "A competência lida deve corresponder à input"
        competencias == ["Java"]
    }
}
