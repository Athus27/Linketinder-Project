package org.linketinder.view.console.input.types

import org.linketinder.view.console.input.LeitorConsole
import org.linketinder.view.console.input.utils.texts.InputStrings

class VagaReader {
    private final LeitorConsole leitorConsole

    VagaReader(LeitorConsole leitorConsole) {
        this.leitorConsole = leitorConsole
    }

    Map readInputVaga(Map empresaSelecionada) {
        println(InputStrings.inputVagaString[0])
        String titulo = leitorConsole.lerLinha()

        println(InputStrings.inputVagaString[1])
        String descricao = leitorConsole.lerLinha()

        println(InputStrings.inputVagaString[2])
        String localVaga = leitorConsole.lerLinha()

        return [
                idEmpresa: empresaSelecionada.id,
                titulo   : titulo,
                descricao: descricao,
                localVaga: localVaga
        ]
    }

}
