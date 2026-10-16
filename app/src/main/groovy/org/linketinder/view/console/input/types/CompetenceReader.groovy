package org.linketinder.view.console.input.types

import org.linketinder.controller.CompetenceController
import org.linketinder.view.console.input.LeitorConsole
import org.linketinder.view.console.input.utils.texts.InputStrings

class CompetenceReader {
    public final InputStrings inputStrings = new InputStrings()
    public final LeitorConsole leitorConsole

    public final CompetenceController competenceController = new CompetenceController()

    CompetenceReader(LeitorConsole leitorConsole) {
        this.leitorConsole = leitorConsole
    }

    List<String> lerCompetenciasPeloConsole() {
        List<String> competencias = []

        while (true) {
            println(inputStrings.inputCompetenciasString[0])
            println(competencias ? "Competências atuais: ${competencias.join(', ')}" : "Nenhuma competência adicionada ainda.")
            String competencia = leitorConsole.lerLinha()

            if (competencia.equalsIgnoreCase("sair")) {
                break
            }

            if (competenceController.checarCompetenciaExistente(competencia)) {
                println(
                        """
                Competência '${competencia}' já existe no banco de dados...
                \n adicionando a competência à lista.

                        """
                )
            } else {
                println("Adicionando competência '${competencia}' à lista.")

            }

            competencias << competencia
        }
        return competencias
    }
}
