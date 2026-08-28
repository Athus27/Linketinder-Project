package org.example

class App {
    ArrayList<PessoaFisica> candidatos = new ArrayList<>()
    ArrayList<PessoaJuridica> empresas = new ArrayList<>()

    Scanner scanner = new Scanner(System.in)

    void exibirMenu() {
        println("=============  Menu  =============")
        println("1. Exibir candidatos")
        println("2. Exibir empresas")
        println("3. Cadastrar candidato")
        println("4. Cadastrar empresa")
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
