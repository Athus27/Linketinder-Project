package org.linketinder

import org.linketinder.view.console.MenuConsole
import org.linketinder.view.console.input.LeitorConsole

class App {
    static void main(String[] args) {
        Scanner scanner = new Scanner(System.in)
        LeitorConsole leitorConsole = new LeitorConsole(scanner)
        MenuConsole menu = new MenuConsole(leitorConsole)

        println("Bem-vindo ao Linketinder!")
        menu.linketinder()

    }

}
