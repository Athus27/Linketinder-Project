package org.linketinder.view

import org.linketinder.controller.CandidatoController
import org.linketinder.controller.CompetenceController
import org.linketinder.model.Pessoa.UserCandidate

class MenuConsole {
    CandidatoController candidatoController = new CandidatoController()
    CompetenceController competenceController = new CompetenceController()

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
            6. Feed de Empresas

            7. Candidato Curtir Empresa
            8. Empresa Curtir Candidato

            9. Mostrar Competencias
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
            /*[2] */ "Digite a data de nascimento AAAA/MM/DD : ",
            /*[3] */ "Digite a descrição: ",
            /*[4] */ "Digite a formação: ",]
    String[] inputCompanyString = [
            /*[0] */ "Digite o CNPJ: ",]


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


    //============================================================
    //                  MÉTODOS DE LEITURA DE INPUT
    //============================================================

    Map readInputCandidate() {
        def scanner = new Scanner(System.in)
        println(this.inputUserString[0])
        String nome = scanner.nextLine()

        println(this.inputCandidateString[0])
        String sobrenome = scanner.nextLine()

        println(this.inputCandidateString[2])
        String dataNascimento = scanner.nextLine()

        println(this.inputCandidateString[1])
        String cpf = scanner.nextLine()

        println(this.inputUserString[1])
        String email = scanner.nextLine()

        println(this.inputUserString[2])
        String pais = scanner.nextLine()

        println(this.inputUserString[3])
        String cep = scanner.nextLine()

        println(this.inputUserString[3])
        String descricao = scanner.nextLine()

        println(this.inputCandidateString[4])
        String formacao = scanner.nextLine()

        println(this.inputUserString[4])
        String senha = scanner.nextLine()


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
        def scanner = new Scanner(System.in)
        println(this.inputUserString[0])
        String nome = scanner.nextLine()

        println(this.inputCompanyString[0])
        String cnpj = scanner.nextLine()

        println(this.inputUserString[1])
        String email = scanner.nextLine()

        println(this.inputUserString[2])
        String pais = scanner.nextLine()

        println(this.inputUserString[3])
        String cep = scanner.nextLine()

        println(this.inputUserString[3])
        String descricao = scanner.nextLine()

        println(this.inputUserString[4])
        String senha = scanner.nextLine()


        return [nome     : nome,
                cnpj     : cnpj,
                descricao: descricao,
                email    : email,
                pais     : pais,
                cep      : cep,
                senha    : senha]

    }


    List<String> lerCompetencias() {
        List<String> competencias = []
        def scanner = new Scanner(System.in)

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
                \n adicionando a competência à lista do candidato.
                        """
                )
            } else {
                println("Adicionando competência '${competencia}'  ao bando de dados e à lista do candidato.")
                competenceController.adicionarCompetencia(competencia)
            }

            competencias << competencia
        }
        return competencias
    }


    boolean cadastrarCandidato(Map dadosCandidato) {
        return candidatoController.cadastrarCandidato(dadosCandidato)
    }


    //============================================================
    //                  FLUXO DO PROGRAMA
    //============================================================
    void linketinder() {
        while (true) {
            exibirMenu()
            def scanner = new Scanner(System.in)
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
                    // Aqui você pode criar o objeto UserCompany com os dados lidos
                    break
                case "5":
                    // Feed de Candidatos

                    List<Map> candidatosFeed = candidatoController.feedCandidatos()
                    exibirFeedCandidatos(candidatosFeed)
                    break
                case "6":
                    // Feed de Empresas
                    break
                case "7":
                    // Candidato Curtir Empresa
                    break
                case "8":
                    // Empresa Curtir Candidato
                    break
                case "9":
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
