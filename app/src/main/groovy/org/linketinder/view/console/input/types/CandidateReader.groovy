package org.linketinder.view.console.input.types

import org.linketinder.view.console.input.LeitorConsole
import org.linketinder.view.console.input.utils.texts.InputStrings
import org.linketinder.view.console.input.utils.ValidationConsole

class CandidateReader {
    private final LeitorConsole leitorConsole
    private final UserReader userReader

    CandidateReader(LeitorConsole leitorConsole, UserReader userReader) {
        this.leitorConsole = leitorConsole
        this.userReader = userReader
    }

    String lerCPF() {
        while (true) {
            print("Digite o cpf do Candidato: ")
            def cpf = leitorConsole.lerLinha()

            if (ValidationConsole.validadeCPF(cpf)) {
                return cpf
            }
            println("CPF inválido. Use o formato 000.000.000-00.")
        }
    }

    Map readInputCandidate() {
        println(InputStrings.inputUserString[0])
        String nome = leitorConsole.lerLinha()

        println(InputStrings.inputCandidateString[0])
        String sobrenome = leitorConsole.lerLinha()

        println(InputStrings.inputCandidateString[2])
        String dataNascimento = leitorConsole.lerLinha()

        String cpf = lerCPF()

        println(InputStrings.inputUserString[1])
        String email = leitorConsole.lerLinha()

        println(InputStrings.inputUserString[2])
        String pais = leitorConsole.lerLinha()

        String cep = userReader.lerCEP()

        println(InputStrings.inputCandidateString[3])
        String descricao = leitorConsole.lerLinha()

        println(InputStrings.inputCandidateString[4])
        String formacao = leitorConsole.lerLinha()

        String senha = userReader.lerSenha()


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


}
