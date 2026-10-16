package org.linketinder.view.console.competence

import org.linketinder.controller.CompetenceController
import org.linketinder.view.console.input.LeitorConsole

class CompetenceConsole {
    private final LeitorConsole leitorConsole
    private final CompetenceController competenceController = new CompetenceController()

    CompetenceConsole(LeitorConsole leitorConsole) {
        this.leitorConsole = leitorConsole
    }

    private void exibirCompetencias(List<Map> competencias) {
        println("========== Competencias Cadastradas ==========")
        competencias.each { Map competencia ->
            println("id:${competencia.id}\t value:${competencia.nome}")
        }
        println("----------------------------------------")
    }

    private Map selecionarCompetencia() {
        List<Map> competencias = competenceController.listarCompetencias()

        if (competencias.isEmpty()) {
            println("Nenhuma competência cadastrada.")
            return null
        }

        println("Selecione uma competência:")
        competencias.eachWithIndex { Map competencia, int indice ->
            println("${indice + 1}. ${competencia.nome}")
        }
        println("0. Cancelar")

        while (true) {
            String entrada = leitorConsole.lerLinha()
            try {
                int opcao = entrada.toInteger()
                if (opcao == 0) {
                    return null
                }
                if (opcao in 1..competencias.size()) {
                    return competencias[opcao - 1]
                }
                println("Opção inválida. Tente novamente.")
            } catch (NumberFormatException ignored) {
                println("Digite apenas o número da competência.")
            }
        }
    }

    private void exibirCompetenciasPeloConsole() {
        List<Map> competencias = competenceController.listarCompetencias()
        exibirCompetencias(competencias)
    }

    private void editarCompetenciaPeloConsole() {
        Map competenciaSelecionada = selecionarCompetencia()
        if (competenciaSelecionada == null) {
            return
        }

        println("Digite o novo nome da competência:")
        String novoNomeCompetencia = leitorConsole.lerLinha().trim()

        if (novoNomeCompetencia.isEmpty()) {
            println("O nome da competência não pode ficar vazio.")
            return
        }

        competenceController.atualizarCompetencia(
                competenciaSelecionada.id as int,
                novoNomeCompetencia
        )
    }

    private void removerCompetenciaPeloConsole() {
        Map competenciaSelecionada = selecionarCompetencia()
        if (competenciaSelecionada == null) {
            return
        }

        if (confirmarRemocao("a competência ${competenciaSelecionada.nome}")) {
            competenceController.removerCompetencia(competenciaSelecionada.id as int)
        } else {
            println("Remoção cancelada.")
        }
    }

    private boolean confirmarRemocao(String registro) {
        println("Tem certeza que deseja remover ${registro}? Digite SIM para confirmar:")
        return leitorConsole.lerLinha().equalsIgnoreCase("SIM")
    }

    private static void printMenuCompetencia() {
        println(
                """
        =============  Menu COMPETENCIA =============
            1. Exibir competências
            2. Editar competência
            3. Remover competência
            0. Voltar ao menu Principal
        ===========================================
        """
        )
    }

    void menuCompetencia() {
        while (true) {
            printMenuCompetencia()
            String opcao = leitorConsole.lerLinha()

            switch (opcao) {
                case "1":
                    exibirCompetenciasPeloConsole()
                    break
                case "2":
                    editarCompetenciaPeloConsole()
                    break
                case "3":
                    removerCompetenciaPeloConsole()
                    break
                case "0":
                    println("Voltando ao menu principal...")
                    return
                default:
                    println("Opção inválida. Tente novamente.")
            }
        }
    }
}
