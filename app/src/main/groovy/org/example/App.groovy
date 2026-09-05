package org.example

class App {
    ArrayList<PessoaFisica> candidatos = new ArrayList<>()
    ArrayList<PessoaJuridica> empresas = new ArrayList<>()
    ArrayList<Curtida> curtidas = new ArrayList<>()

    Scanner scanner = new Scanner(System.in)

    void exibirMenu() {
        println("=============  Menu  =============")
        println("1. Exibir candidatos")
        println("2. Exibir empresas")
        println("3. Cadastrar candidato")
        println("4. Cadastrar empresa")
        println("5. Feed de Candidatos")
        println("6. Feed de Empresas")
        println("7. Candidato Curtir Empresa")
        println("8. Empresa Curtir Candidato")
        println("0. Sair")
        print("Escolha uma opção: ")
    }


    void carregarDadosIniciais() {

        PessoaFisica candidato1 = new PessoaFisica(
                nome: "Fulano",
                descricao: "Desenvolvedor backend junior com interesse em sistemas web.",
                email: "fulano@email.com",
                estado: "SP",
                cep: "01010-000",
                cpf: "111.111.111-11",
                idade: 24,
                competencias: ["Java", "Groovy", "SQL"]
        )

        PessoaFisica candidato2 = new PessoaFisica(
                nome: "Ciclano",
                descricao: "Desenvolvedor frontend com foco em interfaces responsivas.",
                email: "ciclano@email.com",
                estado: "RJ",
                cep: "20020-000",
                cpf: "222.222.222-22",
                idade: 27,
                competencias: ["JavaScript", "Angular", "CSS"]
        )

        PessoaFisica candidato3 = new PessoaFisica(
                nome: "Beltrano",
                descricao: "Analista de dados com experiencia em relatorios e automacao.",
                email: "beltrano@email.com",
                estado: "MG",
                cep: "30130-000",
                cpf: "333.333.333-33",
                idade: 30,
                competencias: ["Python", "SQL", "Power BI"]
        )

        PessoaFisica candidato4 = new PessoaFisica(
                nome: "Mohammed",
                descricao: "Profissional de infraestrutura com foco em cloud e DevOps.",
                email: "mohammed@email.com",
                estado: "PR",
                cep: "80020-000",
                cpf: "444.444.444-44",
                idade: 29,
                competencias: ["AWS", "Docker", "Kubernetes"]
        )

        PessoaFisica candidato5 = new PessoaFisica(
                nome: "Athus",
                descricao: "Desenvolvedor em formacao com interesse em POO e backend.",
                email: "athus@email.com",
                estado: "SC",
                cep: "88020-000",
                cpf: "555.555.555-55",
                idade: 22,
                competencias: ["Groovy", "Java", "Spring Framework"]
        )
        this.candidatos.add(candidato1)
        this.candidatos.add(candidato2)
        this.candidatos.add(candidato3)
        this.candidatos.add(candidato4)
        this.candidatos.add(candidato5)


        PessoaJuridica empresa1 = new PessoaJuridica(
                nome: "Tech Solutions",
                descricao: "Empresa de desenvolvimento de sistemas web e APIs.",
                email: "contato@techsolutions.com",
                estado: "SP",
                cep: "01001-000",
                CNPJ: "12.345.678/0001-90",
                pais: "Brasil",
                competencias: ["Java", "Groovy", "Spring Framework"]
        )

        PessoaJuridica empresa2 = new PessoaJuridica(
                nome: "Data Prime",
                descricao: "Consultoria especializada em dados e automacao de processos.",
                email: "rh@dataprime.com",
                estado: "RJ",
                cep: "20040-020",
                CNPJ: "23.456.789/0001-01",
                pais: "Brasil",
                competencias: ["Python", "SQL", "Power BI"]
        )

        PessoaJuridica empresa3 = new PessoaJuridica(
                nome: "WebCore Digital",
                descricao: "Agencia focada em produtos digitais e interfaces modernas.",
                email: "vagas@webcore.com",
                estado: "MG",
                cep: "30140-071",
                CNPJ: "34.567.890/0001-12",
                pais: "Brasil",
                competencias: ["Angular", "JavaScript", "HTML", "CSS"]
        )

        PessoaJuridica empresa4 = new PessoaJuridica(
                nome: "CloudBridge",
                descricao: "Empresa de infraestrutura cloud e suporte DevOps.",
                email: "talentos@cloudbridge.com",
                estado: "PR",
                cep: "80010-000",
                CNPJ: "45.678.901/0001-23",
                pais: "Brasil",
                competencias: ["AWS", "Docker", "Kubernetes"]
        )

        PessoaJuridica empresa5 = new PessoaJuridica(
                nome: "Mobile Labs",
                descricao: "Software house especializada em aplicativos mobile.",
                email: "recrutamento@mobilelabs.com",
                estado: "SC",
                cep: "88010-400",
                CNPJ: "56.789.012/0001-34",
                pais: "Brasil",
                competencias: ["Kotlin", "Flutter", "Firebase"]
        )

        Vaga vaga1 = new Vaga(
                empresa1,
                "Desenvolvedor Backend Java",
                "Vaga para atuar no desenvolvimento de APIs e sistemas web.",
                ["Java", "Groovy", "Spring Framework"]
        )

        Vaga vaga2 = new Vaga(
                empresa2,
                "Analista de Dados Junior",
                "Vaga para criar relatorios, consultas SQL e automacoes de dados.",
                ["Python", "SQL", "Power BI"]
        )

        Vaga vaga3 = new Vaga(
                empresa3,
                "Desenvolvedor Frontend Angular",
                "Vaga para desenvolver interfaces web responsivas.",
                ["Angular", "JavaScript", "HTML", "CSS"]
        )

        Vaga vaga4 = new Vaga(
                empresa4,
                "DevOps Junior",
                "Vaga para apoiar infraestrutura cloud, containers e deploys.",
                ["AWS", "Docker", "Kubernetes"]
        )

        Vaga vaga5 = new Vaga(
                empresa5,
                "Desenvolvedor Mobile",
                "Vaga para desenvolvimento e manutencao de aplicativos mobile.",
                ["Kotlin", "Flutter", "Firebase"]
        )

        empresa1.vagas.add(vaga1)
        empresa2.vagas.add(vaga2)
        empresa3.vagas.add(vaga3)
        empresa4.vagas.add(vaga4)
        empresa5.vagas.add(vaga5)

        this.empresas.add(empresa1)
        this.empresas.add(empresa2)
        this.empresas.add(empresa3)
        this.empresas.add(empresa4)
        this.empresas.add(empresa5)


    }

    void exibirCandidatos() {
        println("=============  Candidatos  =============")
        candidatos.each { candidato ->
            println candidato
        }
    }

    void exibirEmpresas() {
        println("=============  Empresas  =============")
        empresas.each { empresa ->
            println empresa
        }
    }

    void exibirVagas() {
        int index = 1
        empresas.each { empresa ->
            empresa.vagas.each { vaga ->
                println "[${index}] - ${vaga.titulo}"
                index++
            }
        }

    }

    void adicionarCandidato(PessoaFisica candidato) {
        candidatos.add(candidato)
    }

    void adicionarEmpresa(PessoaJuridica empresa) {
        empresas.add(empresa)
    }


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
            print("Digite o CNPJ da Empresa: ")
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
            print("Digite o CEP: ")
            def cep = scanner.nextLine()

            if (validadeCEP(cep)) {
                return cep
            }

            println("CEP inválido. Use o formato 00000-000.")
        }
    }

    PessoaFisica cadastrarCandidato() {
        PessoaFisica candidato
        print("Digite o nome do Candidato: ")
        def nome = scanner.nextLine()
        print("Digite a descrição do Candidato: ")
        def descricao = scanner.nextLine()
        print("Digite o email do Candidato: ")
        def mail = scanner.nextLine()
        def cpf = lerCPF()
        print("Digite o estado do Candidato: ")
        def estado = scanner.nextLine()
        print("Digite o cep do Candidato: ")
        def cep = lerCEP()
        print("Digite a idade do Candidato: ")
        def idade = lerIdade()

        ArrayList<String> competencias = new ArrayList<>()
        while (true) {
            print("Digite uma competência do Candidato ou digite 0 para voltar: ")
            def competencia = scanner.nextLine()
            if (competencia == "0") {
                break
            }
            competencias.add(competencia)
        }

        candidato = new PessoaFisica(
                nome: nome,
                email: mail,
                cpf: cpf,
                estado: estado,
                cep: cep,
                idade: idade.toInteger(),
                descricao: descricao,
                competencias: competencias
        )


        return candidato
    }

    PessoaJuridica cadastrarEmpresa() {
        PessoaJuridica empresa
        print("Digite o nome da Empresa: ")
        def nome = scanner.nextLine()
        print("Digite a descrição da Empresa: ")
        def descricao = scanner.nextLine()
        print("Digite o email da Empresa: ")
        def mail = scanner.nextLine()
        def cnpj = lerCNPJ()
        print("Digite o estado da Empresa: ")
        def estado = scanner.nextLine()
        print("Digite o cep da Empresa: ")
        def cep = lerCEP()

        empresa = new PessoaJuridica(
                nome: nome,
                email: mail,
                CNPJ: cnpj,
                estado: estado,
                cep: cep,
                descricao: descricao
        )


        return empresa
    }

    int lerInt() {
        while (true) {
            def texto = scanner.nextLine()

            try {
                return texto.toInteger()
            } catch (NumberFormatException e) {
                println("Número inválido. Digite apenas números inteiros.")
            }
        }
    }

    int lerIdade() {
        while (true) {
            print("Digite a idade do Candidato: ")
            def texto = scanner.nextLine()

            try {
                return texto.toInteger()
            } catch (NumberFormatException e) {
                println("Idade inválida. Digite apenas números.")
            }
        }
    }

    void feedCandidatos(ArrayList<PessoaFisica> candidatos) {
        candidatos.each { candidato ->
            def info = candidato.exibirInformacoesFeed()
            if (info) {
                println(info)
            }
        }
    }

    void feedEmpresas(ArrayList<PessoaJuridica> empresas) {
        empresas.each { empresa ->
            def info = empresa.exibirInformacoesFeed()
            if (info) {
                println(info)
            }
        }
    }

    Curtida candidatoCurtirVaga(PessoaFisica candidato, Vaga vaga) {
        Curtida curtida = curtidaEntreCandidatoEVaga(candidato, vaga)
        if (curtida != null && curtida.candidatoCurtiu) {
            println "Erro... candidato: ${candidato.getNome()} já curtiu vaga: ${vaga.getTitulo()}"
            return curtida
        }

        if (curtida == null) {
            curtida = new Curtida(candidato: candidato, vaga: vaga, empresa: vaga.empresa)
            curtidas.add(curtida)
        }

        curtida.candidatoCurtiu = true;
        println "candidato: ${candidato.getNome()} curtiu vaga: ${vaga.getTitulo()}"

        return curtida
    }

    Curtida empresaCurtirCandidato(PessoaFisica candidato, Vaga vaga, PessoaJuridica empresa) {
        Curtida curtida = curtidaEntreCandidatoEVaga(candidato, vaga)
        if (curtida != null && curtida.empresaCurtiu) {
            println "Erro... empresa: ${empresa.getNome()} já curtiu candidato: ${candidato.getNome()} para vaga: ${vaga.getTitulo()}"
            return curtida
        }

        if (curtida == null) {
            curtida = new Curtida(candidato: candidato, vaga: vaga, empresa: vaga.empresa)
            curtidas.add(curtida)
        }

        curtida.empresaCurtiu = true;
        println "empresa: ${empresa.nome} curtiu candidato: ${candidato.nome}"

        return curtida
    }

    PessoaFisica selecionarCandidato() {

        PessoaFisica candidatoSelecionado = null

        println "Selecione o candidato"
        candidatos.eachWithIndex { candidato, i ->
            println "[${i + 1}] - ${candidato.getNome()}"
        }
        println "[0] - VOLTAR/CANCELAR"

        def indice

        while (candidatoSelecionado == null) {
            def entrada = scanner.nextLine()


            try {
                int opcaoCandidato = entrada.toInteger()

                if (opcaoCandidato == 0) {
                    break
                }

                if (opcaoCandidato in 1..candidatos.size()) {
                    indice = opcaoCandidato - 1
                    candidatoSelecionado = candidatos[indice]
                    println "Candidato selecionado: ${candidatoSelecionado.nome}"
                } else {
                    println "Opção inválida!!!"
                }
            } catch (NumberFormatException e) {
                println "formato invalido: tente novamente"

            }

        }
        return candidatoSelecionado

    }

    int quantidadeDeVagasDisponiveis() {
        return vagasDisponiveis().size()
    }

    ArrayList<Vaga> vagasDisponiveis() {
        ArrayList<Vaga> vagas = new ArrayList<>()
        empresas.each { empresa ->
            empresa.vagas.each { vaga ->
                vagas.add(vaga)
            }
        }
        return vagas
    }

    Vaga selecionarVaga() {
        println "Selecione a vaga:"
        exibirVagas()
        println "[0] - VOLTAR/CANCELAR"

        Vaga vagaSelecionada = null


        while (vagaSelecionada == null) {

            def entrada = scanner.nextLine()

            try {
                int opcaoVaga = entrada.toInteger()

                if (opcaoVaga == 0) {
                    break
                }

                ArrayList<Vaga> vagas = vagasDisponiveis()


                if (opcaoVaga in 1..vagas.size()) {
                    vagaSelecionada = vagas[opcaoVaga - 1]
                    println "Vaga selecionada: ${vagaSelecionada.titulo}"
                } else {
                    println "Opção inválida!!!"
                }
            } catch (NumberFormatException e) {
                println "formato invalido: tente novamente"

            }
        }


        return vagaSelecionada
    }

    PessoaJuridica selecionarEmpresa() {
        println "Selecione a empresa:"
        empresas.eachWithIndex { empresa, i ->
            println "[${i + 1}] - ${empresa.getNome()}"
        }
        println "[0] - VOLTAR/CANCELAR"

        PessoaJuridica empresaSelecionada = null

        while (empresaSelecionada == null) {
            def entrada = scanner.nextLine()

            try {
                int opcaoEmpresa = entrada.toInteger()

                if (opcaoEmpresa == 0) {
                    break
                }

                if (opcaoEmpresa in 1..empresas.size()) {
                    empresaSelecionada = empresas[opcaoEmpresa - 1]
                    println "Empresa selecionada: ${empresaSelecionada.nome}"
                } else {
                    println "Opção inválida!!!"
                }
            } catch (NumberFormatException e) {
                println "formato invalido: tente novamente"
            }
        }
        return empresaSelecionada
    }


    Curtida curtidaEntreCandidatoEVaga(PessoaFisica candidato, Vaga vaga) {
        return curtidas.find { curtida ->
            curtida.candidato == candidato && curtida.vaga == vaga
        }
    }


    static void main(String[] args) {

        App app = new App()
        app.carregarDadosIniciais()

        while (true) {
            app.exibirMenu()
            def opcao = app.scanner.nextInt()

            switch (opcao) {
                case 1:
                    app.exibirCandidatos()
                    break
                case 2:
                    app.exibirEmpresas()
                    break
                case 3:
                    app.scanner.nextLine()
                    PessoaFisica candidato = app.cadastrarCandidato()
                    app.candidatos.add(candidato)
                    println("Candidato cadastrado com sucesso!")
                    break
                case 4:
                    app.scanner.nextLine()
                    PessoaJuridica empresa = app.cadastrarEmpresa()
                    app.empresas.add(empresa)
                    println("Empresa cadastrada com sucesso!")
                    break
                case 5:
                    app.feedCandidatos(app.candidatos)
                    break
                case 6:
                    app.feedEmpresas(app.empresas)
                    break
                case 7:
                    app.scanner.nextLine()

                    PessoaFisica candidatoSelecionado = app.selecionarCandidato()
                    if (candidatoSelecionado == null) {
                        break
                    }

                    println candidatoSelecionado

                    def vagaSelecionada = app.selecionarVaga()

                    if (vagaSelecionada == null) {
                        break
                    }

                    Curtida curtida = app.candidatoCurtirVaga(candidatoSelecionado, vagaSelecionada)

                    if (curtida.deuMatch()) {
                        println("A empresa ${vagaSelecionada.empresa.nome} e o candidato ${candidatoSelecionado.nome} deram match na vaga ${vagaSelecionada.titulo}.")
                    }
                    break
                case 8:
                    app.scanner.nextLine()
                    PessoaJuridica empresaSelecionada = app.selecionarEmpresa()
                    if (empresaSelecionada == null) {
                        break
                    }

                    if (empresaSelecionada.vagas.isEmpty()) {
                        println("A empresa selecionada não possui vagas disponíveis.")
                        break
                    }

                    println empresaSelecionada

                    empresaSelecionada.vagas.eachWithIndex { vaga, i ->
                        println "[${i + 1}] - ${vaga.titulo}"
                    }
                    println "[0] - VOLTAR/CANCELAR"

                    def opcaoVaga = app.lerInt()

                    if (opcaoVaga == 0) {
                        break
                    }

                    if (!(opcaoVaga in 1..empresaSelecionada.vagas.size())) {
                        println("Opção inválida. Tente novamente.")
                    }

                    Vaga vagaSelecionada = empresaSelecionada.vagas[opcaoVaga - 1]

                    PessoaFisica candidatoSelecionado = app.selecionarCandidato()
                    if (candidatoSelecionado == null) {
                        break
                    }

                    Curtida curtida = app.empresaCurtirCandidato(candidatoSelecionado, vagaSelecionada, empresaSelecionada)

                    if (curtida.deuMatch()) {
                        println("A empresa ${empresaSelecionada.nome} e o candidato ${candidatoSelecionado.nome} deram match na vaga ${vagaSelecionada.titulo}.")
                    }
                    break;
                case 0:
                    println("Saindo do programa...")
                    return
                default:
                    println("Opção inválida. Tente novamente.")
            }
            println()
        }

    }

}
