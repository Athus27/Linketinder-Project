package org.linketinder.view

class MenuConsole {

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
            0. Sair
        ==================================
        """

    String[] inputUserString = [
            /*[0] */"Digite o nome: ",
            /*[1] */"Digite o email: ",
            /*[2] */"Digite o País: ",
            /*[3] */"Digite o CEP: ",
            /*[4] */"Digite a senha: ",
    ]
    String[] inputCandidateString = [
            /*[0] */"Digite o sobrenome: ",
            /*[1] */"Digite o CPF: ",
            /*[2] */"Digite a data de nascimento AAAA/MM/DD : ",
            /*[3] */"Digite a descrição: ",
            /*[4] */"Digite a formação: ",
    ]
    String[] inputCompanyString = [
            /*[0] */"Digite o CNPJ: ",
    ]


    String[] inputCompetenciasString = [
            /*[0] */"Digite uma competência (ou 'sair' para finalizar): ",
            /*[1] */"Competências atuais: ",
            /*[2] */"Nenhuma competência adicionada ainda.",
    ]

    //============================================================
    //                  MÉTODOS DE EXIBIÇÃO 
    //============================================================

    void exibirMenu() {
        println(this.menuString)
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


        return [
                nome: nome,
                sobrenome: sobrenome,
                dataNascimento: dataNascimento,
                cpf: cpf,
                descricao: descricao,
                email: email,
                formacao: formacao,
                pais: pais,
                cep: cep,
                senha: senha
        ]

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


        return [
                nome: nome,
                cnpj: cnpj,
                descricao: descricao,
                email: email,
                pais: pais,
                cep: cep,
                senha: senha
        ]

    }



    List<String> lerCompetencias() {
        List<String> competencias = []
        def scanner = new Scanner(System.in)

        while (true) {
            println(this.inputCompetenciasString[0])
            println(competencias?"Competências atuais: ${competencias.join(', ')}" : "Nenhuma competência adicionada ainda.")
            String competencia = scanner.nextLine()
            if (competencia.equalsIgnoreCase("sair")) {
                break
            }
            competencias << competencia
        }
        return competencias
    }




}
