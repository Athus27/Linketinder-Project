package org.linketinder.view.console.input.types

import spock.lang.Specification
import org.linketinder.view.console.input.LeitorConsole
import org.linketinder.controller.CompetenceController
import org.linketinder.dao.competence.CompetenceDAO

class CompetenceReaderTest extends Specification {
    def "deve ler competencias ate o usuario informar sair"() {
        given: "uma entrada simulada e um leitor de competencias"
        String entradaSimulada = "Java\nsair\n"
        LeitorConsole leitor = new LeitorConsole(new Scanner(entradaSimulada))
        CompetenceController controller = new CompetenceController(new CompetenceDAO())
        controller.metaClass.checarCompetenciaExistente = { String nome -> true }
        CompetenceReader reader = new CompetenceReader(leitor, controller)

        when: "Ler uma competência"
        List<String> competencias = reader.lerCompetenciasPeloConsole()

        then: "A competência lida deve corresponder à input"
        competencias == ["Java"]
    }
}
