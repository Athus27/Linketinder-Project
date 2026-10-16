package org.linketinder.view.console.input.types

import org.linketinder.view.console.input.LeitorConsole
import org.linketinder.view.console.input.utils.texts.InputStrings
import org.linketinder.view.console.input.utils.ValidationConsole



class CompanyReader {
    private final UserReader userReader
    private final LeitorConsole leitorConsole

    CompanyReader(LeitorConsole leitorConsole, UserReader userReader) {
        this.leitorConsole = leitorConsole
        this.userReader = userReader
    }

    private String lerCNPJ() {
        while (true) {
            println(InputStrings.inputCompanyString[0])
            def cnpj = leitorConsole.lerLinha()

            if (ValidationConsole.validadeCNPJ(cnpj)) {
                return cnpj
            }

            println("CNPJ inválido. Use o formato 00.000.000/0000-00.")
        }
    }

    Map readInputCompany() {
        println(InputStrings.inputUserString[0])
        String nome = leitorConsole.lerLinha()

        String cnpj = lerCNPJ()

        println(InputStrings.inputUserString[1])
        String email = leitorConsole.lerLinha()

        println(InputStrings.inputUserString[2])
        String pais = leitorConsole.lerLinha()

        String cep = userReader.lerCEP()

        println(InputStrings.inputCandidateString[3])
        String descricao = leitorConsole.lerLinha()

        String senha = userReader.lerSenha()


        return [nome     : nome,
                cnpj     : cnpj,
                descricao: descricao,
                email    : email,
                pais     : pais,
                cep      : cep,
                senha    : senha]

    }
}
