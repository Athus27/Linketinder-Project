package org.linketinder.view

import org.linketinder.controller.CandidatoController
import org.linketinder.controller.CompanyController
import org.linketinder.controller.CompetenceController
import org.linketinder.controller.VagaController

class MenuConsole {
    CandidatoController candidatoController = new CandidatoController()
    CompanyController companyController = new CompanyController()
    CompetenceController competenceController = new CompetenceController()
    VagaController vagaController = new VagaController()
    Scanner scanner = new Scanner(System.in)

    //============================================================
    //                  STRINGS DE INPUT
    //============================================================


    String menuString = """
        =============  Menu  =============
            1. Exibir candidatos
            2. Exibir empresas

            3. Cadastrar candidato
            4. Cadastrar empresa

            5. Feed de Candidatos
            6. Feed de Vagas

            7. Cadastrar vaga
            8. Candidato Curtir Vaga
            9. Empresa Curtir Candidato

            10. Mostrar Competencias
            0. Sair
        ==================================
        """

    String[] inputUserString = [
            /*[0] */ "Digite o nome: ",
            /*[1] */ "Digite o email: ",
            /*[2] */ "Digite o País: ",
            /*[3] */ "Digite o CEP: ",
            /*[4] */ "Digite a senha: ",]
    String[] inputCandidateString = [
            /*[0] */ "Digite o sobrenome: ",
            /*[1] */ "Digite o CPF: ",
            /*[2] */ "Digite a data de nascimento DD/MM/AAAA: ",
            /*[3] */ "Digite a descrição: ",
            /*[4] */ "Digite a formação: ",]
    String[] inputCompanyString = [
            /*[0] */ "Digite o CNPJ: ",]

    String[] inputVagaString = [
            /*[0] */ "Digite o título da vaga: ",
            /*[1] */ "Digite a descrição da vaga: ",
            /*[2] */ "Digite o local da vaga: ",]


    String[] inputCompetenciasString = [
            /*[0] */ "Digite uma competência (ou 'sair' para finalizar): ",
            /*[1] */ "Competências atuais: ",
            /*[2] */ "Nenhuma competência adicionada ainda.",]

    //============================================================
    //                  MÉTODOS DE EXIBIÇÃO E STRINGS RELACIONADAS
    //============================================================

    void exibirMenu() {
        println(this.menuString)
    }

    String stringCandidato(Map candidato) {
        String candidatoString =
                """
            ID: ${candidato.id}
            Nome: ${candidato.nome}
            Sobrenome: ${candidato.sobrenome}
            Data de nascimento: ${candidato.dataNascimento}
            CPF: ${candidato.cpf}
            Descrição: ${candidato.descricao}
            E-mail: ${candidato.email}
            Formação: ${candidato.formacao}
            País: ${candidato.pais}
            CEP: ${candidato.cep}
            Competências: ${candidato.competencias.join(', ')}
        """.stripIndent()

        return candidatoString
    }

    void exibirCandidatos(List<Map> candidatos) {
        if (candidatos.isEmpty()) {
            println("Nenhum candidato encontrado.")
        } else {
            println("============= Candidatos =============")
            candidatos.each { candidato ->
                String candidatoString = stringCandidato(candidato)
                println(candidatoString)
                println("--------------------------------------")
            }
        }
    }

    String stringCandidatoFeed(Map candidato) {
        return """
        Descrição: ${candidato.descricao}
        Formação: ${candidato.formacao}
        Competências: ${candidato.competencias.join(', ')}
    """.stripIndent()
    }

    void exibirFeedCandidatos(List<Map> candidatos) {
        if (candidatos.isEmpty()) {
            println("Nenhum candidato encontrado.")
            return
        }

        println("========== Feed de Candidatos ==========")

        candidatos.each { candidato ->
            println(stringCandidatoFeed(candidato))
            println("----------------------------------------")
        }
    }

    void exibirCompetencias(List<Map> competencias) {
        println("========== Competencias Cadastradas ==========")

        competencias.each { competencia ->
            println(
                    "id:${competencia.id}\t value:${competencia.nome}"
            )
        }
        println("----------------------------------------")

    }

    String stringEmpresa(Map empresa) {
        String empresaString =
                """
            ID: ${empresa.id}
            Nome: ${empresa.nome}
            CNPJ: ${empresa.cnpj}
            Descrição: ${empresa.descricao}
            E-mail: ${empresa.email}
            País: ${empresa.pais}
            CEP: ${empresa.cep}
        """.stripIndent()

        return empresaString
    }

    void exibirEmpresas(List<Map> empresas) {
        if (empresas.isEmpty()) {
            println("Nenhuma empresa encontrada.")
        } else {
            println("============= Empresas =============")
            empresas.each { empresa ->
                String empresaString = stringEmpresa(empresa)
                println(empresaString)
                println("--------------------------------------")
            }
        }
    }

    String stringVagaFeed(Map vaga) {
        return """
        Título: ${vaga.titulo}
        Descrição: ${vaga.descricao}
        Local: ${vaga.localVaga}
        Competências: ${vaga.competencias.join(', ')}
    """.stripIndent()
    }

    void exibirFeedVagas(List<Map> vagas) {
        if (vagas.isEmpty()) {
            println("Nenhuma vaga encontrada.")
            return
        }

        println("========== Feed de Vagas ==========")

        vagas.each { vaga ->
            println(stringVagaFeed(vaga))
            println("----------------------------------------")
        }
    }


    //============================================================
    //     MÉTODOS DE LEITURA DE INPUT E VALIDAÇÃO DE ENTRADA
    //============================================================


    boolean validadeCPF(String text) {
        def cpfFormatado = /^\d{3}\.\d{3}\.\d{3}-\d{2}$/
        return text ==~ cpfFormatado
    }

    String lerCPF() {
        while (true) {
            print("Digite o cpf do Candidato: ")
            def cpf = scanner.nextLine()

            if (validadeCPF(cpf)) {
                return cpf
            }

            println("CPF inválido. Use o formato 000.000.000-00.")
        }
    }

    boolean validadeCNPJ(String text) {
        def cnpjFormatado = /^\d{2}\.\d{3}\.\d{3}\/\d{4}-\d{2}$/

        return text ==~ cnpjFormatado
    }

    String lerCNPJ() {
        while (true) {
            println(inputCompanyString[0])
            def cnpj = scanner.nextLine()

            if (validadeCNPJ(cnpj)) {
                return cnpj
            }

            println("CNPJ inválido. Use o formato 00.000.000/0000-00.")
        }
    }

    boolean validadeCEP(String text) {
        def cepFormatado = /^\d{5}-\d{3}$/
        return text ==~ cepFormatado
    }

    String lerCEP() {
        while (true) {
            println(inputUserString[3])
            def cep = scanner.nextLine()

            if (validadeCEP(cep)) {
                return cep
            }

            println("CEP inválido. Use o formato 00000-000.")
        }
    }

    boolean validadeSenha(String senha){
        return senha.length()>=6
    }

    String lerSenha(){
        while (true){
            println(inputUserString[4])
            def senha = scanner.nextLine()

            if (validadeSenha(senha)){
                return senha
            }

            println("CPF inválido. Use o formato 000.000.000-00")
        }
    }


    Map readInputCandidate() {
        println(this.inputUserString[0])
        String nome = scanner.nextLine()

        println(this.inputCandidateString[0])
        String sobrenome = scanner.nextLine()

        println(this.inputCandidateString[2])
        String dataNascimento = scanner.nextLine()

        String cpf = lerCPF()

        println(this.inputUserString[1])
        String email = scanner.nextLine()

        println(this.inputUserString[2])
        String pais = scanner.nextLine()

        String cep = lerCEP()

        println(this.inputCandidateString[3])
        String descricao = scanner.nextLine()

        println(this.inputCandidateString[4])
        String formacao = scanner.nextLine()

        println(this.inputUserString[4])
        String senha = lerSenha()


        return [nome          : nome,
                sobrenome     : sobrenome,
                dataNascimento: dataNascimento,
                cpf           : cpf,
                descricao     : descricao,
                email         : email,
                formacao      : formacao,
                pais          : pais,
                cep           : cep,
                senha         : senha]

    }

    Map readInputCompany() {
        println(this.inputUserString[0])
        String nome = scanner.nextLine()

        String cnpj = lerCNPJ()

        println(this.inputUserString[1])
        String email = scanner.nextLine()

        println(this.inputUserString[2])
        String pais = scanner.nextLine()

        String cep = lerCEP()

        println(this.inputCandidateString[3])
        String descricao = scanner.nextLine()

        String senha = lerSenha()


        return [nome     : nome,
                cnpj     : cnpj,
                descricao: descricao,
                email    : email,
                pais     : pais,
                cep      : cep,
                senha    : senha]

    }

    Map selecionarEmpresa() {
        List<Map> empresas = companyController.listarEmpresas()

        if (empresas.isEmpty()) {
            println("Nenhuma empresa cadastrada.")
            return null
        }

        println("Selecione a empresa responsável pela vaga:")
        empresas.eachWithIndex { Map empresa, int indice ->
            println("${indice + 1}. ${empresa.nome}")
        }
        println("0. Cancelar")

        while (true) {
            String entrada = scanner.nextLine()

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

    Map readInputVaga(Map empresaSelecionada) {
        println(this.inputVagaString[0])
        String titulo = scanner.nextLine()

        println(this.inputVagaString[1])
        String descricao = scanner.nextLine()

        println(this.inputVagaString[2])
        String localVaga = scanner.nextLine()

        return [
                idEmpresa: empresaSelecionada.id,
                titulo   : titulo,
                descricao: descricao,
                localVaga: localVaga
        ]
    }


    List<String> lerCompetencias() {
        List<String> competencias = []

        while (true) {
            println(this.inputCompetenciasString[0])
            println(competencias ? "Competências atuais: ${competencias.join(', ')}" : "Nenhuma competência adicionada ainda.")
            String competencia = scanner.nextLine()

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


    boolean cadastrarCandidato(Map dadosCandidato) {
        return candidatoController.cadastrarCandidato(dadosCandidato)
    }
    boolean cadastrarEmpresa(Map dadosEmpresa) {
        return companyController.cadastrarEmpresa(dadosEmpresa)
    }
    boolean cadastrarVaga(Map dadosVaga) {
        return vagaController.cadastrarVaga(dadosVaga)
    }


    //============================================================
    //                  FLUXO DO PROGRAMA
    //============================================================
    void linketinder() {
        while (true) {
            exibirMenu()
            String opcao = scanner.nextLine()

            switch (opcao) {
                case "1":
                    // Exibir candidatos

                    List<Map> candidatos = candidatoController.listarCandidatos()
                    exibirCandidatos(candidatos)

                    break

                    break
                case "2":
                    // Exibir empresas
                    List<Map> empresas = companyController.listarEmpresas()
                    exibirEmpresas(empresas)
                    break
                case "3":
                    // Cadastrar candidato

                    Map dadosCandidato = readInputCandidate()
                    List<String> competencias = lerCompetencias()
                    dadosCandidato.competencias = competencias

                    cadastrarCandidato(dadosCandidato)
                    break
                case "4":
                    // Cadastrar empresa
                    Map dadosEmpresa = readInputCompany()
                    cadastrarEmpresa(dadosEmpresa)
                    break
                case "5":
                    // Feed de Candidatos

                    List<Map> candidatosFeed = candidatoController.feedCandidatos()
                    exibirFeedCandidatos(candidatosFeed)
                    break
                case "6":
                    // Feed de Vagas
                    List<Map> vagas = vagaController.listarVagas()
                    exibirFeedVagas(vagas)
                    break
                case "7":
                    // Cadastrar vaga
                    Map empresaSelecionada = selecionarEmpresa()

                    if (empresaSelecionada == null) {
                        break
                    }

                    Map dadosVaga = readInputVaga(empresaSelecionada)
                    List<String> competenciasVaga = lerCompetencias()
                    dadosVaga.competencias = competenciasVaga

                    cadastrarVaga(dadosVaga)
                    break
                case "8":
                    // Candidato Curtir Vaga
                    break
                case "9":
                    // Empresa Curtir Candidato
                    break
                case "10":
                    // Mostrar Competencias
                    exibirCompetencias(competenceController.listarCompetencias())
                    break
                case "0":
                    println("Saindo do programa...")
                    return
                default:
                    println("Opção inválida. Tente novamente.")
            }
        }
    }


    //============================================================
    //                  METODO MAIN
    //============================================================
    static void main(String[] args) {
        MenuConsole menu = new MenuConsole()
        println("Bem-vindo ao Linketinder!")
        menu.linketinder()
    }
}
