package org.linketinder.view.console.input.types

import org.linketinder.view.console.input.LeitorConsole
import org.linketinder.view.console.input.utils.texts.InputStrings
import org.linketinder.view.console.input.utils.ValidationConsole

class UserReader {
    private final LeitorConsole leitor

    UserReader(LeitorConsole leitor) {
        this.leitor = leitor
    }

    String lerCEP() {
        while (true) {
            println(InputStrings.inputUserString[3])
            def cep = leitor.lerLinha()

            if (ValidationConsole.validadeCEP(cep)) {
                return cep
            }

            println("CEP inválido. Use o formato 00000-000.")
        }
    }

    String lerSenha() {
        while (true) {
            println(InputStrings.inputUserString[4])
            def senha = leitor.lerLinha()

            if (ValidationConsole.validadeSenha(senha)) {
                return senha
            }
            println("Senha inválida. Digite pelo menos 6 caracteres.")
        }
    }
}
