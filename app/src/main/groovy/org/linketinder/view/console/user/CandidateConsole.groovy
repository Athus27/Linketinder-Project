package org.linketinder.view.console.user

import org.linketinder.controller.CandidatoController
import org.linketinder.controller.CurtidaController
import org.linketinder.view.console.input.LeitorConsole
import org.linketinder.view.console.input.types.CandidateReader
import org.linketinder.view.console.input.types.CompetenceReader
import org.linketinder.view.console.input.types.UserReader
import org.linketinder.view.console.input.utils.ValidationConsole
import org.linketinder.view.console.input.utils.formatter.CandidateFormatter
import org.linketinder.view.console.input.utils.texts.OutputStrings
import org.linketinder.view.console.job.VagaConsole

class CandidateConsole {

    private final LeitorConsole leitorConsole
    private final UserReader userReader
    private final CandidateReader candidateReader
    private final CompetenceReader competenceReader
    private final VagaConsole vagaConsole

    private final CandidatoController candidatoController = new CandidatoController()
    private final CurtidaController curtidaController = new CurtidaController()

    CandidateConsole(LeitorConsole leitorConsole) {
        this.leitorConsole = leitorConsole
        this.userReader = new UserReader(leitorConsole)
        this.candidateReader = new CandidateReader(leitorConsole, this.userReader)
        this.competenceReader = new CompetenceReader(leitorConsole)
        this.vagaConsole = new VagaConsole(leitorConsole)
    }

    private void exibirCandidatos(List<Map> candidatos) {
        if (candidatos.isEmpty()) {
            println("Nenhum candidato encontrado.")
        } else {
            println("============= Candidatos =============")
            candidatos.each { candidato ->
                String candidatoString = CandidateFormatter.stringCandidato(candidato)
                println(candidatoString)
                println("--------------------------------------")
            }
        }
    }

    private void exibirFeedCandidatos(List<Map> candidatos) {
        if (candidatos.isEmpty()) {
            println("Nenhum candidato encontrado.")
            return
        }

        println("========== Feed de Candidatos ==========")

        candidatos.each { candidato ->
            println(CandidateFormatter.stringCandidatoParaFeed(candidato))
            println("----------------------------------------")
        }
    }


    Map selecionarCandidato() {
        List<Map> candidatos = candidatoController.listarCandidatos()

        if (candidatos.isEmpty()) {
            println("Nenhum candidato cadastrado.")
            return null
        }

        println("Selecione um candidato:")
        candidatos.eachWithIndex { Map candidato, int indice ->
            println("${indice + 1}. ${candidato.nome} ${candidato.sobrenome} - ${candidato.cpf}")
        }
        println("0. Cancelar")

        while (true) {
            String entrada = leitorConsole.lerLinha()
            try {
                int opcao = entrada.toInteger()

                if (opcao == 0) {
                    return null
                }
                if (opcao in 1..candidatos.size()) {
                    return candidatos[opcao - 1]
                }

                println("Opção inválida. Tente novamente.")
            } catch (NumberFormatException ignored) {
                println("Digite apenas o número do candidato.")
            }
        }
    }


    private void cadastrarCandidatoPeloConsole() {
        Map dadosCandidato = candidateReader.readInputCandidate()
        List<String> competencias = competenceReader.lerCompetenciasPeloConsole()
        dadosCandidato.competencias = competencias
        candidatoController.cadastrarCandidato(dadosCandidato)
    }

    private void exibirCandidatosPeloConsole() {
        List<Map> candidatos = candidatoController.listarCandidatos()
        exibirCandidatos(candidatos)
    }

    private void editarCandidatoPeloConsole() {
        Map candidatoSelecionado = selecionarCandidato()
        if (candidatoSelecionado == null) {
            return
        }

        Map novosDadosCandidato = readInputCandidate()
        novosDadosCandidato.id = candidatoSelecionado.id
        novosDadosCandidato.competencias = lerCompetenciasPeloConsole()
        candidatoController.atualizarCandidato(novosDadosCandidato)
    }

    private void removerCandidatoPeloConsole() {
        Map candidatoSelecionado = selecionarCandidato()
        if (candidatoSelecionado == null) {
            return
        }

        if (ValidationConsole.confirmarRemocao("o candidato ${candidatoSelecionado.nome} ${candidatoSelecionado.sobrenome}", leitorConsole.scanner)) {
            candidatoController.removerCandidato(candidatoSelecionado.id as int)
        } else {
            println("Remoção cancelada.")
        }
    }

    private void feedCandidatosPeloConsole() {
        List<Map> candidatosFeed = candidatoController.feedCandidatos()
        exibirFeedCandidatos(candidatosFeed)
    }

    private void candidatoCurtirVagaPeloConsole() {
        Map candidatoSelecionado = selecionarCandidato()

        if (candidatoSelecionado == null) {
            return
        }

        Map vagaSelecionada = vagaConsole.selecionarVaga()

        if (vagaSelecionada == null) {
            return
        }

        Map resultadoCurtida = curtidaController.candidatoCurtirVaga(
                candidatoSelecionado.id as int,
                vagaSelecionada.id as int
        )

        String mensagem = OutputStrings.mensagemDaCurtida(
                resultadoCurtida,
                OutputStrings.mensagemDaCurtidaString.mensagemCandidatoJaCurtiu,
                OutputStrings.mensagemDaCurtidaString.mensagemSucessoParaCandidato
        )

        println(mensagem)
    }

    private static void printMenuCandidato() {
        println(
                """
        =============  Menu CANDIDATO =============
            1. Exibir candidatos
            2. Cadastrar candidato
            3. Feed de Candidatos
            4. Candidato Curtir Vaga
            5. Editar candidato
            6. Remover candidato
            0. Voltar ao menu Principal
        ===========================================
        """
        )
    }

    private void menuCandidato() {
        while (true) {
            printMenuCandidato()
            String opcao = this.leitorConsole.lerLinha()

            switch (opcao) {
                case "1":
                    exibirCandidatosPeloConsole()
                    break
                case "2":
                    cadastrarCandidatoPeloConsole()
                    break
                case "3":
                    feedCandidatosPeloConsole()
                    break
                case "4":
                    candidatoCurtirVagaPeloConsole()
                    break
                case "5":
                    editarCandidatoPeloConsole()
                    break
                case "6":
                    removerCandidatoPeloConsole()
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
