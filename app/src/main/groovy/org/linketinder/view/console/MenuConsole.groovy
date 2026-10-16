package org.linketinder.view.console

import org.linketinder.view.console.competence.CompetenceConsole
import org.linketinder.view.console.input.LeitorConsole
import org.linketinder.view.console.input.types.CompetenceReader
import org.linketinder.view.console.job.VagaConsole
import org.linketinder.view.console.user.CandidateConsole
import org.linketinder.view.console.user.CompanyConsole

class MenuConsole {
    private final CandidateConsole candidateConsole
    private final CompanyConsole companyConsole
    private final VagaConsole vagaConsole
    private final CompetenceConsole competenceConsole

    private final LeitorConsole leitorConsole

    MenuConsole(LeitorConsole leitorConsole, CandidateConsole candidateConsole, CompetenceReader competenceReader) {
        this.leitorConsole = leitorConsole

        this.candidateConsole = candidateConsole
        this.companyConsole = new CompanyConsole(leitorConsole, competenceReader)
        this.vagaConsole = new VagaConsole(leitorConsole, competenceReader)
        this.competenceConsole = new CompetenceConsole(leitorConsole)

    }

    void linketinder() {
        menuPrincipal()
    }

    void printMenuPrincipal() {
        println(
                """
        =============  Menu PRINCIPAL =============
            [1] - MENU CANDIDATO
            [2] - MENU EMPRESA
            [3] - MENU VAGA
            [4] - MENU COMPETENCIA
            [0] - SAIR

        ===========================================
        """
        )
    }
    void menuPrincipal() {
        while (true) {
            printMenuPrincipal()
            String opcao = leitorConsole.lerLinha()

            switch (opcao) {
                case "1":
                    candidateConsole.menuCandidato()
                    break
                case "2":
                    companyConsole.menuEmpresa()
                    break

                case "3":
                    vagaConsole.menuVaga()
                    break
                case "4":
                    competenceConsole.menuCompetencia()
                    break

                case "0":
                    println("Saindo do Linketinder. Até logo!")
                    return
                default:
                    println("Opção inválida. Tente novamente.")
            }
        }
    }
}
