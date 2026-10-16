package org.linketinder

import org.linketinder.controller.CompetenceController
import org.linketinder.dao.competence.CompetenceDAO
import org.linketinder.view.console.MenuConsole
import org.linketinder.view.console.input.LeitorConsole
import org.linketinder.view.console.input.types.CompetenceReader
import org.linketinder.view.console.user.CandidateConsole

class App {
    static void main(String[] args) {
        Scanner scanner = new Scanner(System.in)
        LeitorConsole leitorConsole = new LeitorConsole(scanner)
        CompetenceDAO competenceDAO = new CompetenceDAO()
        CompetenceController competenceController = new CompetenceController(competenceDAO)
        CompetenceReader competenceReader = new CompetenceReader(leitorConsole,competenceController)

        CandidateConsole candidateConsole = new CandidateConsole(leitorConsole,competenceReader)
        MenuConsole menu = new MenuConsole(leitorConsole, candidateConsole, competenceReader)

        println("Bem-vindo ao Linketinder!")
        menu.linketinder()

    }

}
