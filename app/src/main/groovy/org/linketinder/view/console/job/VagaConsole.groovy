package org.linketinder.view.console.job

import org.linketinder.controller.VagaController
import org.linketinder.view.console.input.LeitorConsole
import org.linketinder.view.console.input.types.CompetenceReader
import org.linketinder.view.console.input.types.VagaReader
import org.linketinder.view.console.input.utils.formatter.VagaFormatter
import org.linketinder.view.console.user.CompanyConsole


class VagaConsole {
    private final LeitorConsole leitorConsole

    private final VagaReader vagaReader
    private final CompetenceReader competenceReader

    private CompanyConsole companyConsole

    private final VagaController vagaController = new VagaController()

    VagaConsole(LeitorConsole leitorConsole, CompetenceReader competenceReader) {
        this.leitorConsole = leitorConsole
        this.vagaReader = new VagaReader(leitorConsole)
        this.competenceReader = competenceReader
    }

    private CompanyConsole obterCompanyConsole() {
        // Evita a criação circular entre os consoles de empresa e vaga.
        if (companyConsole == null) {
            companyConsole = new CompanyConsole(leitorConsole, competenceReader)
        }
        return companyConsole
    }

    private void cadastrarVagaPeloConsole() {
        Map empresaSelecionada = obterCompanyConsole().selecionarEmpresa()

        if (empresaSelecionada == null) {
            return
        }

        Map dadosVaga = vagaReader.readInputVaga(empresaSelecionada)
        List<String> competenciasVaga = competenceReader.lerCompetenciasPeloConsole()

        dadosVaga.competencias = competenciasVaga

        vagaController.cadastrarVaga(dadosVaga)
    }

    private void exibirFeedVagas(List<Map> vagas) {
        if (vagas.isEmpty()) {
            println("Nenhuma vaga encontrada.")
            return
        }

        println("========== Feed de Vagas ==========")

        vagas.each { vaga ->
            println(VagaFormatter.stringVagaFeed(vaga))
            println("----------------------------------------")
        }
    }

    private void feedVagasPeloConsole() {
        List<Map> vagasFeed = vagaController.listarVagas()
        exibirFeedVagas(vagasFeed)
    }

    Map selecionarVaga() {
        List<Map> vagas = vagaController.listarVagas()

        if (vagas.isEmpty()) {
            println("Nenhuma vaga cadastrada.")
            return null
        }

        println("Selecione uma vaga:")
        vagas.eachWithIndex { Map vaga, int indice ->
            println("${indice + 1}. ${vaga.titulo} - ${vaga.localVaga}")
        }
        println("0. Cancelar")

        while (true) {
            String entrada = leitorConsole.lerLinha()

            try {
                int opcao = entrada.toInteger()

                if (opcao == 0) {
                    return null
                }
                if (opcao in 1..vagas.size()) {
                    return vagas[opcao - 1]
                }

                println("Opção inválida. Tente novamente.")
            } catch (NumberFormatException ignored) {
                println("Digite apenas o número da vaga.")
            }
        }
    }

    private void editarVagaPeloConsole() {
        Map vagaSelecionada = selecionarVaga()
        if (vagaSelecionada == null) {
            return
        }

        Map empresaDaVaga = obterCompanyConsole().selecionarEmpresa()
        if (empresaDaVaga == null) {
            return
        }

        Map novosDadosVaga = vagaReader.readInputVaga(empresaDaVaga)
        novosDadosVaga.id = vagaSelecionada.id
        novosDadosVaga.competencias = competenceReader.lerCompetenciasPeloConsole()
        vagaController.atualizarVaga(novosDadosVaga)
    }

    private void removerVagaPeloConsole() {
        Map vagaSelecionada = selecionarVaga()
        if (vagaSelecionada == null) {
            return
        }

        if (confirmarRemocao("a vaga ${vagaSelecionada.titulo}")) {
            vagaController.removerVaga(vagaSelecionada.id as int)
        } else {
            println("Remoção cancelada.")
        }
    }


    private boolean confirmarRemocao(String registro) {
        println("Tem certeza que deseja remover ${registro}? Digite SIM para confirmar:")
        return leitorConsole.lerLinha().equalsIgnoreCase("SIM")
    }

    private List<Map> listarVagasDaEmpresa(Map empresa) {
        return vagaController.listarVagas().findAll { Map vaga ->
            (vaga.idEmpresa as int) == (empresa.id as int)
        }
    }

    void exibirVagasDaEmpresa(Map empresa) {
        List<Map> vagas = listarVagasDaEmpresa(empresa)

        if (vagas.isEmpty()) {
            println("A empresa selecionada não possui vagas cadastradas.")
            return
        }

        println("========== Vagas da empresa ${empresa.nome} ==========")
        vagas.eachWithIndex { Map vaga, int indice ->
            println("${indice + 1}. ${vaga.titulo} - ${vaga.localVaga}")
        }
    }

    Map selecionarVagaDaEmpresa(Map empresa) {
        List<Map> vagas = listarVagasDaEmpresa(empresa)

        if (vagas.isEmpty()) {
            println("A empresa selecionada não possui vagas cadastradas.")
            return null
        }

        println("Selecione uma vaga da empresa ${empresa.nome}:")
        vagas.eachWithIndex { Map vaga, int indice ->
            println("${indice + 1}. ${vaga.titulo} - ${vaga.localVaga}")
        }
        println("0. Cancelar")

        while (true) {
            String entrada = leitorConsole.lerLinha()

            try {
                int opcao = entrada.toInteger()

                if (opcao == 0) {
                    return null
                }
                if (opcao in 1..vagas.size()) {
                    return vagas[opcao - 1]
                }

                println("Opção inválida. Tente novamente.")
            } catch (NumberFormatException ignored) {
                println("Digite apenas o número da vaga.")
            }
        }
    }


    private static void printMenuVaga() {
        println(
                """
        =============  Menu VAGA =================
            1. Feed de Vagas
            2. Cadastrar vaga
            3. Editar vaga
            4. Remover vaga

            0. Voltar ao menu Principal
        ===========================================
        """
        )
    }

    void menuVaga() {
        while (true) {
            printMenuVaga()
            String opcao = leitorConsole.lerLinha()

            switch (opcao) {
                case "1":
                    feedVagasPeloConsole()
                    break
                case "2":
                    cadastrarVagaPeloConsole()
                    break
                case "3":
                    editarVagaPeloConsole()
                    break
                case "4":
                    removerVagaPeloConsole()
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
