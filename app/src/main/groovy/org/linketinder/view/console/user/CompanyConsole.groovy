package org.linketinder.view.console.user

import org.linketinder.controller.CompanyController
import org.linketinder.controller.CurtidaController
import org.linketinder.view.console.input.types.CompanyReader
import org.linketinder.view.console.input.types.UserReader

import org.linketinder.view.console.input.LeitorConsole
import org.linketinder.view.console.input.utils.formatter.CompanyFormatter
import org.linketinder.view.console.input.utils.texts.OutputStrings
import org.linketinder.view.console.job.VagaConsole


class CompanyConsole {

    private final LeitorConsole leitorConsole
    private final VagaConsole vagaConsole
    private final CandidateConsole candidateConsole

    private final CompanyReader companyReader

    private final CompanyController companyController = new CompanyController()
    private final CurtidaController curtidaController = new CurtidaController()

    CompanyConsole(LeitorConsole leitorConsole) {
        this.leitorConsole = leitorConsole
        this.vagaConsole = new VagaConsole(leitorConsole)
        this.candidateConsole = new CandidateConsole(leitorConsole)
        UserReader userReader = new UserReader(leitorConsole)
        this.companyReader = new CompanyReader(leitorConsole, userReader)
    }

    private void cadastrarEmpresaPeloConsole() {
        Map dadosEmpresa = companyReader.readInputCompany()
        companyController.cadastrarEmpresa(dadosEmpresa)
    }

    private void exibirEmpresasPeloConsole() {
        List<Map> empresas = companyController.listarEmpresas()
        exibirEmpresas(empresas)
    }

    private void exibirVagasDaEmpresaPeloConsole() {
        Map empresaSelecionada = selecionarEmpresa()
        if (empresaSelecionada == null) {
            return
        }

        vagaConsole.exibirVagasDaEmpresa(empresaSelecionada)
    }

    private void editarEmpresaPeloConsole() {
        Map empresaSelecionada = selecionarEmpresa()
        if (empresaSelecionada == null) {
            return
        }

        Map novosDadosEmpresa = companyReader.readInputCompany()
        novosDadosEmpresa.id = empresaSelecionada.id
        companyController.atualizarEmpresa(novosDadosEmpresa)
    }

    private void removerEmpresaPeloConsole() {
        Map empresaSelecionada = selecionarEmpresa()
        if (empresaSelecionada == null) {
            return
        }

        if (confirmarRemocao("a empresa ${empresaSelecionada.nome}")) {
            companyController.removerEmpresa(empresaSelecionada.id as int)
        } else {
            println("Remoção cancelada.")
        }
    }

    private boolean confirmarRemocao(String registro) {
        println("Tem certeza que deseja remover ${registro}? Digite SIM para confirmar:")
        return leitorConsole.lerLinha().equalsIgnoreCase("SIM")
    }

    private void empresaCurtirCandidatoPeloConsole() {
        Map empresaSelecionada = selecionarEmpresa()

        if (empresaSelecionada == null) {
            return
        }

        Map vagaSelecionada = vagaConsole.selecionarVagaDaEmpresa(empresaSelecionada)

        if (vagaSelecionada == null) {
            return
        }

        Map candidatoSelecionado = candidateConsole.selecionarCandidato()

        if (candidatoSelecionado == null) {
            return
        }

        Map resultadoCurtida = curtidaController.empresaCurtirCandidato(
                empresaSelecionada.id as int,
                vagaSelecionada.id as int,
                candidatoSelecionado.id as int
        )

        String mensagem = OutputStrings.mensagemDaCurtida(
                resultadoCurtida,
                OutputStrings.mensagemDaCurtidaString.mensagemEmpresaJaCurtiu,
                OutputStrings.mensagemDaCurtidaString.mensagemSucessoParaEmpresa
        )

        println(mensagem)
    }

    private void exibirEmpresas(List<Map> empresas) {
        if (empresas.isEmpty()) {
            println("Nenhuma empresa encontrada.")
        } else {
            println("============= Empresas =============")
            empresas.each { empresa ->
                String empresaString = CompanyFormatter.stringEmpresa(empresa)
                println(empresaString)
                println("--------------------------------------")
            }
        }
    }

    Map selecionarEmpresa() {
        List<Map> empresas = companyController.listarEmpresas()

        if (empresas.isEmpty()) {
            println("Nenhuma empresa cadastrada.")
            return null
        }

        println("Selecione uma empresa:")
        empresas.eachWithIndex { Map empresa, int indice ->
            println("${indice + 1}. ${empresa.nome} - ${empresa.cnpj}")
        }
        println("0. Cancelar")

        while (true) {
            String entrada = leitorConsole.lerLinha()

            try {
                int opcao = entrada.toInteger()

                if (opcao == 0) {
                    return null
                }

                if (opcao in 1..empresas.size()) {
                    return empresas[opcao - 1]
                }

                println("Opção inválida. Tente novamente.")
            } catch (NumberFormatException ignored) {
                println("Digite apenas o número da empresa.")
            }
        }
    }

    private static void printMenuEmpresa() {
        println(
                """
        =============  Menu EMPRESA =============
            1. Exibir empresas
            2. Cadastrar empresa
            3. Exibir vagas de uma empresa
            4. Empresa Curtir Candidato
            5. Editar empresa
            6. Remover empresa
            0. Voltar ao menu Principal
        ===========================================
        """
        )
    }

    void menuEmpresa() {
        while (true) {
            printMenuEmpresa()
            String opcao = leitorConsole.lerLinha()

            switch (opcao) {
                case "1":
                    exibirEmpresasPeloConsole()
                    break
                case "2":
                    cadastrarEmpresaPeloConsole()
                    break
                case "3":
                    exibirVagasDaEmpresaPeloConsole()
                    break
                case "4":
                    empresaCurtirCandidatoPeloConsole()
                    break
                case "5":
                    editarEmpresaPeloConsole()
                    break
                case "6":
                    removerEmpresaPeloConsole()
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
